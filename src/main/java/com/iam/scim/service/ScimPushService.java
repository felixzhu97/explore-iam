package com.iam.scim.service;

import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.GroupRepository;
import com.iam.identity.domain.repository.UserRepository;
import com.iam.scim.domain.model.ProvisionedAccount;
import com.iam.scim.domain.model.ProvisioningTarget;
import com.iam.scim.domain.repository.ProvisioningTargetRepository;
import com.iam.scim.domain.service.ScimTargetGateway;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pushes IAM users to enabled Provisioning Targets. Every push is idempotent: the target account
 * is found by recorded remote id or by {@code externalId}, and versions already synced are skipped.
 */
@Service
public class ScimPushService {

  private static final Logger log = LoggerFactory.getLogger(ScimPushService.class);

  private final ProvisioningTargetRepository targetRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final ScimTargetGateway targetGateway;

  /**
   * Creates the service.
   *
   * @param targetRepository Provisioning Targets
   * @param userRepository IAM users
   * @param groupRepository IAM groups (assignment groups)
   * @param targetGateway SCIM calls to targets
   */
  public ScimPushService(
      ProvisioningTargetRepository targetRepository,
      UserRepository userRepository,
      GroupRepository groupRepository,
      ScimTargetGateway targetGateway) {
    this.targetRepository = targetRepository;
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.targetGateway = targetGateway;
  }

  /**
   * Brings every enabled target in line with the user's current IAM state.
   *
   * @param userId IAM user id
   */
  @Transactional
  public void syncUser(String userId) {
    for (ProvisioningTarget target : targetRepository.findEnabled()) {
      syncUserOnTarget(target, userId);
      targetRepository.save(target);
    }
  }

  /**
   * Re-evaluates users whose membership in a group changed; only assignment groups matter.
   *
   * @param groupId IAM group id
   * @param userIds users added to or removed from the group
   */
  @Transactional
  public void syncAssignment(String groupId, Iterable<String> userIds) {
    for (ProvisioningTarget target : targetRepository.findEnabled()) {
      if (!target.isAssignmentGroup(groupId)) {
        continue;
      }
      for (String userId : userIds) {
        syncUserOnTarget(target, userId);
      }
      targetRepository.save(target);
    }
  }

  /**
   * Deletes the user's account on every enabled target, and on disabled targets that still hold
   * a provisioned account, so erasure is never skipped while a target is paused.
   *
   * @param userId IAM user id
   */
  @Transactional
  public void deleteUser(String userId) {
    for (ProvisioningTarget target : targetRepository.findAll()) {
      Optional<ProvisionedAccount> account = target.findProvisionedAccount(userId);
      if (!target.isEnabled() && account.isEmpty()) {
        continue;
      }
      Optional<String> remoteId =
          account
              .map(ProvisionedAccount::getRemoteId)
              .or(() -> targetGateway.findRemoteId(target, userId));
      remoteId.ifPresent(id -> targetGateway.deleteAccount(target, id));
      target.forgetProvisionedAccount(userId);
      targetRepository.save(target);
      log.info("SCIM push: deleted user on target '{}'", target.getName());
    }
  }

  private void syncUserOnTarget(ProvisioningTarget target, String userId) {
    Optional<User> user = userRepository.findById(userId);
    Optional<ProvisionedAccount> account = target.findProvisionedAccount(userId);
    if (user.isEmpty() || !isAssigned(target, userId)) {
      account.ifPresent(existing -> deactivate(target, existing));
      return;
    }
    long version = user.get().getVersion();
    if (account.isPresent() && account.get().isSyncedAt(version)) {
      return;
    }
    String remoteId = upsert(target, account.map(ProvisionedAccount::getRemoteId), user.get());
    target.recordProvisionedAccount(userId, remoteId, version);
    log.info("SCIM push: synced user on target '{}'", target.getName());
  }

  private String upsert(ProvisioningTarget target, Optional<String> known, User user) {
    Optional<String> remoteId = known.or(() -> targetGateway.findRemoteId(target, user.getId()));
    if (remoteId.isPresent() && targetGateway.replaceAccount(target, remoteId.get(), user)) {
      return remoteId.get();
    }
    return targetGateway.createAccount(target, user);
  }

  private void deactivate(ProvisioningTarget target, ProvisionedAccount account) {
    if (targetGateway.setAccountActive(target, account.getRemoteId(), false)) {
      target.markProvisionedAccountStale(account.iamUserId());
      log.info("SCIM push: deactivated unassigned user on target '{}'", target.getName());
    } else {
      target.forgetProvisionedAccount(account.iamUserId());
    }
  }

  private boolean isAssigned(ProvisioningTarget target, String userId) {
    if (!target.requiresAssignment()) {
      return true;
    }
    return groupRepository
        .findById(target.getAssignmentGroupId())
        .map(group -> group.hasMember(userId))
        .orElse(false);
  }
}
