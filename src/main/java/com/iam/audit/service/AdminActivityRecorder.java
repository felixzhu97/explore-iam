package com.iam.audit.service;

import com.iam.audit.domain.model.AdminActivity;
import com.iam.audit.domain.model.AuditActor;
import com.iam.audit.domain.model.AuditOutcome;
import com.iam.audit.domain.model.AuditTarget;
import com.iam.audit.domain.model.DataAccessLog;
import com.iam.audit.infra.SecurityAuditSupport;
import com.iam.common.domain.model.Permission;
import com.iam.common.domain.model.PrincipalId;
import com.iam.common.domain.model.ReasonCode;
import com.iam.common.domain.model.Resource;
import com.iam.federation.domain.event.OAuthClientRegisteredEvent;
import com.iam.federation.domain.event.OAuthClientScopesChangedEvent;
import com.iam.identity.domain.event.GroupCreatedEvent;
import com.iam.identity.domain.event.GroupDeletedEvent;
import com.iam.identity.domain.event.GroupMembershipChangedEvent;
import com.iam.identity.domain.event.GroupProfileChangedEvent;
import com.iam.identity.domain.event.RoleCreatedEvent;
import com.iam.identity.domain.event.UserDeactivatedEvent;
import com.iam.identity.domain.event.UserDeletedEvent;
import com.iam.identity.domain.event.UserPasswordResetEvent;
import com.iam.identity.domain.event.UserProfileChangedEvent;
import com.iam.identity.domain.event.UserProvisionedEvent;
import com.iam.identity.domain.event.UserReactivatedEvent;
import com.iam.identity.domain.event.UserRoleAssignedEvent;
import com.iam.identity.domain.event.UserRoleUnassignedEvent;
import com.iam.policy.domain.event.AccessEvaluatedEvent;
import com.iam.policy.domain.event.AllowPolicyCreatedEvent;
import com.iam.policy.domain.event.PermissionCreatedEvent;
import com.iam.policy.domain.event.PolicyAttachedEvent;
import com.iam.sts.domain.event.RoleAssumedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Records management-plane audit aggregates from Domain Events. Handlers run synchronously in the
 * publisher's transaction, so the audit row commits or rolls back with the change it describes.
 */
@Service
public class AdminActivityRecorder {

  private static final String USER = "User";
  private static final String GROUP = "Group";

  private final AuditService auditService;

  /**
   * Creates the recorder.
   *
   * @param auditService audit application service
   */
  public AdminActivityRecorder(AuditService auditService) {
    this.auditService = auditService;
  }

  @EventListener
  void handleUserProvisionedEvent(UserProvisionedEvent event) {
    recordSuccess("identity:CreateUser", USER, event.userId());
  }

  @EventListener
  void handleUserProfileChangedEvent(UserProfileChangedEvent event) {
    recordSuccess("identity:UpdateUser", USER, event.userId());
  }

  @EventListener
  void handleUserDeactivatedEvent(UserDeactivatedEvent event) {
    recordSuccess("identity:DisableUser", USER, event.userId());
  }

  @EventListener
  void handleUserReactivatedEvent(UserReactivatedEvent event) {
    recordSuccess("identity:EnableUser", USER, event.userId());
  }

  @EventListener
  void handleUserPasswordResetEvent(UserPasswordResetEvent event) {
    recordSuccess("identity:ResetPassword", USER, event.userId());
  }

  @EventListener
  void handleUserDeletedEvent(UserDeletedEvent event) {
    recordSuccess("identity:DeleteUser", USER, event.userId());
  }

  @EventListener
  void handleUserRoleAssignedEvent(UserRoleAssignedEvent event) {
    recordSuccess("identity:AssignRole", USER, event.userId());
  }

  @EventListener
  void handleUserRoleUnassignedEvent(UserRoleUnassignedEvent event) {
    recordSuccess("identity:UnassignRole", USER, event.userId());
  }

  @EventListener
  void handleGroupCreatedEvent(GroupCreatedEvent event) {
    recordSuccess("identity:CreateGroup", GROUP, event.groupId());
  }

  @EventListener
  void handleGroupProfileChangedEvent(GroupProfileChangedEvent event) {
    recordSuccess("identity:UpdateGroup", GROUP, event.groupId());
  }

  @EventListener
  void handleGroupMembershipChangedEvent(GroupMembershipChangedEvent event) {
    if (!event.addedUserIds().isEmpty()) {
      recordSuccess("identity:AddGroupMember", GROUP, event.groupId());
    }
    if (!event.removedUserIds().isEmpty()) {
      recordSuccess("identity:RemoveGroupMember", GROUP, event.groupId());
    }
  }

  @EventListener
  void handleGroupDeletedEvent(GroupDeletedEvent event) {
    recordSuccess("identity:DeleteGroup", GROUP, event.groupId());
  }

  @EventListener
  void handleRoleCreatedEvent(RoleCreatedEvent event) {
    recordSuccess("identity:CreateRole", "Role", event.roleId());
  }

  @EventListener
  void handleOAuthClientRegisteredEvent(OAuthClientRegisteredEvent event) {
    recordSuccess("federation:RegisterClient", "OAuthClient", event.clientId());
  }

  @EventListener
  void handleOAuthClientScopesChangedEvent(OAuthClientScopesChangedEvent event) {
    recordSuccess("federation:UpdateClientScopes", "OAuthClient", event.clientId());
  }

  @EventListener
  void handleAllowPolicyCreatedEvent(AllowPolicyCreatedEvent event) {
    recordSuccess("policy:CreatePolicy", "AllowPolicy", event.policyId());
  }

  @EventListener
  void handlePolicyAttachedEvent(PolicyAttachedEvent event) {
    recordSuccess("policy:AttachPolicy", "PolicyBinding", event.policyBindingId());
  }

  @EventListener
  void handlePermissionCreatedEvent(PermissionCreatedEvent event) {
    recordSuccess("policy:CreatePermission", "Permission", event.code());
  }

  @EventListener
  void handleRoleAssumedEvent(RoleAssumedEvent event) {
    recordSuccess("sts:AssumeRole", "Role", event.roleId());
  }

  @EventListener
  void handleAccessEvaluatedEvent(AccessEvaluatedEvent event) {
    auditService.save(
        DataAccessLog.recordAccessDecisionLog(
            new PrincipalId(event.principalId()),
            new Permission(event.action()),
            new Resource(event.resource()),
            event.effect(),
            new ReasonCode(event.reasonCode())));
  }

  private void recordSuccess(String action, String targetType, String targetId) {
    AuditActor actor = SecurityAuditSupport.currentActor();
    auditService.save(
        AdminActivity.recordAdminActivity(
            actor, action, new AuditTarget(targetType, targetId), AuditOutcome.SUCCESS));
  }
}
