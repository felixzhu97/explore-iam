package com.iam.audit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iam.audit.domain.vo.AuditActor;
import com.iam.audit.domain.vo.AuditTarget;
import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.PrincipalId;
import com.iam.common.domain.vo.ReasonCode;
import com.iam.common.domain.vo.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ManagementEventTest {

  @Test
  @DisplayName("should reject blank action when logging management action")
  void shouldRejectBlankActionWhenLoggingManagementAction() {
    AuditActor actor = new AuditActor("demo");
    AuditTarget target = new AuditTarget("User", "demo");

    assertThatThrownBy(
            () -> ManagementEvent.logManagementAction(actor, "  ", target, AuditOutcome.SUCCESS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("action");
  }

  @Test
  @DisplayName("should record authentication login with user target when logging authentication")
  void shouldRecordAuthenticationLoginWithUserTargetWhenLoggingAuthentication() {
    AuditActor actor = new AuditActor("demo");

    ManagementEvent event = ManagementEvent.logAuthentication(actor, AuditOutcome.SUCCESS);

    assertThat(event.getAction()).isEqualTo("auth:login");
    assertThat(event.getTarget().getType()).isEqualTo("User");
    assertThat(event.getTarget().getId()).isEqualTo("demo");
    assertThat(event.wasSuccessful()).isTrue();
    assertThat(event.involvesActor(actor)).isTrue();
    assertThat(event.targets(new AuditTarget("User", "demo"))).isTrue();
  }

  @Test
  @DisplayName("should report failure when authentication outcome is failure")
  void shouldReportFailureWhenAuthenticationOutcomeIsFailure() {
    ManagementEvent event =
        ManagementEvent.logAuthentication(new AuditActor("unknown"), AuditOutcome.FAILURE);

    assertThat(event.wasFailure()).isTrue();
    assertThat(event.wasSuccessful()).isFalse();
  }
}
