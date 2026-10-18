package com.iam.identity.service;

import com.iam.federation.domain.repository.FederatedIdentityRepository;
import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.GroupRepository;
import com.iam.identity.domain.repository.TotpCredentialRepository;
import com.iam.identity.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Creates, replaces, and deletes users on behalf of a provisioning client. */
@Service
@Transactional(readOnly = true)
public class UserProvisioningService {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final TotpCredentialRepository totpCredentialRepository;
  private final FederatedIdentityRepository federatedIdentityRepository;
  private final PublicKeyCredentialUserEntityRepository passkeyUserRepository;
  private final UserCredentialRepository passkeyCredentialRepository;

  /**
   * Creates the user provisioning service.
   *
   * @param userRepository IAM user repository
   * @param groupRepository IAM group repository
   * @param totpCredentialRepository TOTP credential repository
   * @param federatedIdentityRepository federated identity repository
   * @param passkeyUserRepository WebAuthn user entity repository
   * @param passkeyCredentialRepository WebAuthn credential repository
   */
  public UserProvisioningService(
      UserRepository userRepository,
      GroupRepository groupRepository,
      TotpCredentialRepository totpCredentialRepository,
      FederatedIdentityRepository federatedIdentityRepository,
      PublicKeyCredentialUserEntityRepository passkeyUserRepository,
      UserCredentialRepository passkeyCredentialRepository) {
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.totpCredentialRepository = totpCredentialRepository;
    this.federatedIdentityRepository = federatedIdentityRepository;
    this.passkeyUserRepository = passkeyUserRepository;
    this.passkeyCredentialRepository = passkeyCredentialRepository;
  }

  /**
   * Returns one user by id.
   *
   * @param userId IAM user id
   * @return user
   */
  public User get(String userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
  }

  /**
   * Finds a user by username.
   *
   * @param username login name
   * @return matching user when present
   */
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  /**
   * Finds a user by External ID.
   *
   * @param externalId identifier from the provisioning client
   * @return matching user when present
   */
  public Optional<User> findByExternalId(String externalId) {
    return userRepository.findByExternalId(externalId);
  }

  /**
   * Lists all users.
   *
   * @return users
   */
  public List<User> findAll() {
    return userRepository.findAll();
  }

  /**
   * Creates a password-less user.
   *
   * @param profile desired state
   * @return stored user
   */
  @Transactional
  public User provisionUser(UserProfile profile) {
    requireUsernameAvailable(profile.username(), null);
    User user =
        User.provisionUser(
            profile.username(), profile.email(), profile.displayName(), profile.externalId());
    if (!profile.active()) {
      user.disableUser();
    }
    return userRepository.save(user);
  }

  /**
   * Replaces a user's provisioned attributes.
   *
   * @param userId IAM user id
   * @param profile desired state
   * @param expectedVersion version from {@code If-Match}, or {@code null} to skip the check
   * @return stored user
   */
  @Transactional
  public User replaceUser(String userId, UserProfile profile, Long expectedVersion) {
    User user = get(userId);
    requireVersion(user.getVersion(), expectedVersion);
    requireUsernameAvailable(profile.username(), userId);
    final String previousUsername = user.getUsername();
    user.updateProfile(profile.username(), profile.email(), profile.displayName());
    user.changeExternalId(profile.externalId());
    if (profile.active()) {
      user.enableUser();
    } else {
      user.disableUser();
    }
    User saved = userRepository.save(user);
    renamePasskeyUser(previousUsername, saved.getUsername());
    return saved;
  }

  /**
   * Deletes a user with memberships, role bindings, federated links, TOTP, and passkeys.
   *
   * @param userId IAM user id
   * @param expectedVersion version from {@code If-Match}, or {@code null} to skip the check
   */
  @Transactional
  public void deleteUser(String userId, Long expectedVersion) {
    User user = get(userId);
    requireVersion(user.getVersion(), expectedVersion);
    for (Group group : groupRepository.findByMemberUserId(userId)) {
      group.removeMember(userId);
      groupRepository.save(group);
    }
    federatedIdentityRepository.deleteByUserId(userId);
    totpCredentialRepository.deleteByUserId(userId);
    deletePasskeys(user.getUsername());
    user.removeUser();
    userRepository.delete(user);
  }

  private void requireUsernameAvailable(String username, String currentUserId) {
    userRepository
        .findByUsername(username)
        .filter(existing -> !existing.getId().equals(currentUserId))
        .ifPresent(
            existing -> {
              throw new ResponseStatusException(HttpStatus.CONFLICT, "username exists");
            });
  }

  private void renamePasskeyUser(String previousUsername, String username) {
    if (previousUsername.equals(username)) {
      return;
    }
    PublicKeyCredentialUserEntity entity = passkeyUserRepository.findByUsername(previousUsername);
    if (entity == null) {
      return;
    }
    passkeyUserRepository.save(
        ImmutablePublicKeyCredentialUserEntity.builder()
            .id(entity.getId())
            .name(username)
            .displayName(entity.getDisplayName())
            .build());
  }

  private void deletePasskeys(String username) {
    PublicKeyCredentialUserEntity entity = passkeyUserRepository.findByUsername(username);
    if (entity == null) {
      return;
    }
    for (CredentialRecord credential : passkeyCredentialRepository.findByUserId(entity.getId())) {
      passkeyCredentialRepository.delete(credential.getCredentialId());
    }
    passkeyUserRepository.delete(entity.getId());
  }

  static void requireVersion(long actual, Long expected) {
    if (expected != null && expected != actual) {
      throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "version mismatch");
    }
  }
}
