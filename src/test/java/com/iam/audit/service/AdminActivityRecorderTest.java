package com.iam.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.iam.audit.domain.model.AdminActivity;
import com.iam.audit.domain.model.DataAccessLog;
import com.iam.common.domain.model.Effect;
import com.iam.identity.domain.event.GroupMembershipChangedEvent;
import com.iam.identity.domain.event.UserDeactivatedEvent;
import com.iam.identity.domain.event.UserRoleAssignedEvent;
import com.iam.policy.domain.event.AccessEvaluatedEvent;
import com.iam.sts.domain.event.RoleAssumedEvent;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("AdminActivityRecorder")
class AdminActivityRecorderTest {

  private final AuditService auditService = mock(AuditService.class);
  private final AdminActivityRecorder recorder = new AdminActivityRecorder(auditService);

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("should record disable user with the signed-in actor")
  void shouldRecordDisableUserWithTheSignedInActor() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated("admin", null, List.of()));

    recorder.handleUserDeactivatedEvent(new UserDeactivatedEvent("user-1"));

    AdminActivity activity = savedActivities(1).get(0);
    assertThat(activity.getAction()).isEqualTo("identity:DisableUser");
    assertThat(activity.getActor().getValue()).isEqualTo("admin");
    assertThat(activity.getTarget().getType()).isEqualTo("User");
    assertThat(activity.getTarget().getTargetId()).isEqualTo("user-1");
  }

  @Test
  @DisplayName("should record role assignment against the user")
  void shouldRecordRoleAssignmentAgainstTheUser() {
    recorder.handleUserRoleAssignedEvent(new UserRoleAssignedEvent("user-1", "role-1"));

    AdminActivity activity = savedActivities(1).get(0);
    assertThat(activity.getAction()).isEqualTo("identity:AssignRole");
    assertThat(activity.getTarget().getTargetId()).isEqualTo("user-1");
  }

  @Test
  @DisplayName("should record add and remove when membership change has both")
  void shouldRecordAddAndRemoveWhenMembershipChangeHasBoth() {
    recorder.handleGroupMembershipChangedEvent(
        new GroupMembershipChangedEvent("group-1", Set.of("user-2"), Set.of("user-1")));

    assertThat(savedActivities(2))
        .extracting(AdminActivity::getAction)
        .containsExactly("identity:AddGroupMember", "identity:RemoveGroupMember");
  }

  @Test
  @DisplayName("should record assume role under sts")
  void shouldRecordAssumeRoleUnderSts() {
    recorder.handleRoleAssumedEvent(new RoleAssumedEvent("role-1"));

    assertThat(savedActivities(1).get(0).getAction()).isEqualTo("sts:AssumeRole");
  }

  @Test
  @DisplayName("should record data access log when access is evaluated")
  void shouldRecordDataAccessLogWhenAccessIsEvaluated() {
    recorder.handleAccessEvaluatedEvent(
        new AccessEvaluatedEvent("p-1", "s3:GetObject", "arn:doc", Effect.DENY, "IMPLICIT_DENY"));

    ArgumentCaptor<DataAccessLog> captor = ArgumentCaptor.forClass(DataAccessLog.class);
    verify(auditService).save(captor.capture());
    assertThat(captor.getValue().getPrincipalId().value()).isEqualTo("p-1");
    assertThat(captor.getValue().getEffect()).isEqualTo(Effect.DENY);
    assertThat(captor.getValue().getReasonCode().value()).isEqualTo("IMPLICIT_DENY");
  }

  private List<AdminActivity> savedActivities(int count) {
    ArgumentCaptor<AdminActivity> captor = ArgumentCaptor.forClass(AdminActivity.class);
    verify(auditService, times(count)).save(captor.capture());
    return captor.getAllValues();
  }
}
