package com.iam.scim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iam.identity.domain.model.ExternalId;
import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.GroupRepository;
import com.iam.identity.domain.repository.UserRepository;
import com.iam.identity.service.GroupService;
import com.iam.identity.service.UserProfile;
import com.iam.identity.service.UserProvisioningService;
import com.iam.scim.domain.model.ProvisionedAccount;
import com.iam.scim.domain.model.ProvisioningTarget;
import com.iam.scim.domain.repository.ProvisioningTargetRepository;
import com.iam.scim.domain.service.ScimTargetGateway;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableScenarios
@DisplayName("SCIM push listener")
class ScimPushListenerTest {

  private static final long WAIT_MS = 5000;

  @MockitoBean private ScimTargetGateway gateway;

  @Autowired private ProvisioningTargetRepository targetRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private UserProvisioningService userProvisioningService;

  @Autowired private GroupService groupService;

  @Autowired private TransactionTemplate tx;

  private String targetName;

  private String lastUserId;

  @AfterEach
  void disableTarget() {
    if (targetName != null) {
      tx.executeWithoutResult(
          status ->
              targetRepository
                  .findByName(targetName)
                  .ifPresent(
                      target -> {
                        target.disableTarget();
                        targetRepository.save(target);
                      }));
    }
  }

  @Test
  @DisplayName("should create account on target when user provisioned")
  void shouldCreateAccountOnTargetWhenUserProvisioned(Scenario scenario) {
    givenTarget(null);
    when(gateway.findRemoteId(any(), any())).thenReturn(Optional.empty());
    when(gateway.createAccount(any(), any())).thenReturn("remote-1");

    scenario
        .stimulate(this::provisionUser)
        .andWaitForStateChange(() -> Optional.ofNullable(latestAccount()))
        .andVerify(
            account ->
                assertThat(account)
                    .get()
                    .extracting(ProvisionedAccount::getRemoteId)
                    .isEqualTo("remote-1"));
  }

  @Test
  @DisplayName("should replace account when user deactivated")
  void shouldReplaceAccountWhenUserDeactivated() {
    givenTarget(null);
    when(gateway.findRemoteId(any(), any())).thenReturn(Optional.empty());
    when(gateway.createAccount(any(), any())).thenReturn("remote-2");
    when(gateway.replaceAccount(any(), any(), any())).thenReturn(true);
    String userId = provisionUser();
    verify(gateway, timeout(WAIT_MS)).createAccount(any(), any());

    User user = userRepository.findById(userId).orElseThrow();
    userProvisioningService.replaceUser(
        userId,
        new UserProfile(
            user.getUsername(),
            user.getEmail(),
            user.getDisplayName(),
            user.getExternalId(),
            false),
        null);

    verify(gateway, timeout(WAIT_MS))
        .replaceAccount(any(), eq("remote-2"), any(User.class));
  }

  @Test
  @DisplayName("should delete account on target when user deleted")
  void shouldDeleteAccountOnTargetWhenUserDeleted() {
    givenTarget(null);
    when(gateway.findRemoteId(any(), any())).thenReturn(Optional.empty());
    when(gateway.createAccount(any(), any())).thenReturn("remote-3");
    String userId = provisionUser();
    verify(gateway, timeout(WAIT_MS)).createAccount(any(), any());

    userProvisioningService.deleteUser(userId, null);

    verify(gateway, timeout(WAIT_MS)).deleteAccount(any(), eq("remote-3"));
  }

  @Test
  @DisplayName("should delete account on disabled target when user deleted")
  void shouldDeleteAccountOnDisabledTargetWhenUserDeleted() {
    givenTarget(null);
    when(gateway.findRemoteId(any(), any())).thenReturn(Optional.empty());
    when(gateway.createAccount(any(), any())).thenReturn("remote-5");
    String userId = provisionUser();
    verify(gateway, timeout(WAIT_MS)).createAccount(any(), any());
    disableTarget();

    userProvisioningService.deleteUser(userId, null);

    verify(gateway, timeout(WAIT_MS)).deleteAccount(any(), eq("remote-5"));
  }

  @Test
  @DisplayName("should provision only assignment group members")
  void shouldProvisionOnlyAssignmentGroupMembers() {
    String groupId =
        tx.execute(status -> groupRepository.save(Group.createGroup(unique("ai-users"))).getId());
    givenTarget(groupId);
    when(gateway.findRemoteId(any(), any())).thenReturn(Optional.empty());
    when(gateway.createAccount(any(), any())).thenReturn("remote-4");
    when(gateway.setAccountActive(any(), any(), eq(false))).thenReturn(true);
    String userId = provisionUser();
    verify(gateway, timeout(WAIT_MS).times(0)).createAccount(any(), any());

    groupService.addMember(groupId, userId);
    verify(gateway, timeout(WAIT_MS)).createAccount(any(), any());

    groupService.removeMember(groupId, userId);
    verify(gateway, timeout(WAIT_MS)).setAccountActive(any(), eq("remote-4"), eq(false));
    verify(gateway, never()).deleteAccount(any(), any());
  }

  private void givenTarget(String assignmentGroupId) {
    targetName = unique("target");
    tx.executeWithoutResult(
        status ->
            targetRepository.save(
                ProvisioningTarget.createProvisioningTarget(
                    targetName,
                    "https://ai.example/scim/v2",
                    "iam-scim-outbound",
                    assignmentGroupId)));
  }

  private ProvisionedAccount latestAccount() {
    return tx.execute(
        status ->
            targetRepository
                .findByName(targetName)
                .orElseThrow()
                .findProvisionedAccount(lastUserId)
                .orElse(null));
  }

  private String provisionUser() {
    User user =
        userProvisioningService.provisionUser(
            new UserProfile(
                unique("push"), null, "Push User", new ExternalId(unique("x")), true));
    lastUserId = user.getId();
    return lastUserId;
  }

  private static String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }
}
