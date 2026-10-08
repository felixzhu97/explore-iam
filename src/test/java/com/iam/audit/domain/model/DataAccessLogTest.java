package com.iam.audit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.common.domain.model.Effect;
import com.iam.common.domain.model.Permission;
import com.iam.common.domain.model.PrincipalId;
import com.iam.common.domain.model.ReasonCode;
import com.iam.common.domain.model.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataAccessLogTest {

  private static final PrincipalId PRINCIPAL = new PrincipalId("user:demo");
  private static final Permission ACTION = new Permission("iam:ListUsers");
  private static final Resource RESOURCE = new Resource("arn:iam::user/*");

  @Test
  @DisplayName("should report allowed when effect is ALLOW")
  void shouldReportAllowedWhenEffectIsAllow() {
    DataAccessLog log =
        DataAccessLog.capture(
            PRINCIPAL, ACTION, RESOURCE, Effect.ALLOW, ReasonCode.EXPLICIT_ALLOW);

    assertThat(log.isAllowed()).isTrue();
    assertThat(log.isDenied()).isFalse();
    assertThat(log.wasExplicitDeny()).isFalse();
    assertThat(log.wasImplicitDeny()).isFalse();
  }

  @Test
  @DisplayName("should report explicit deny when reason code is EXPLICIT_DENY")
  void shouldReportExplicitDenyWhenReasonCodeIsExplicitDeny() {
    DataAccessLog log =
        DataAccessLog.capture(
            PRINCIPAL, ACTION, RESOURCE, Effect.DENY, ReasonCode.EXPLICIT_DENY);

    assertThat(log.isDenied()).isTrue();
    assertThat(log.isAllowed()).isFalse();
    assertThat(log.wasExplicitDeny()).isTrue();
    assertThat(log.wasImplicitDeny()).isFalse();
  }

  @Test
  @DisplayName("should report implicit deny when reason code is IMPLICIT_DENY")
  void shouldReportImplicitDenyWhenReasonCodeIsImplicitDeny() {
    DataAccessLog log =
        DataAccessLog.capture(
            PRINCIPAL, ACTION, RESOURCE, Effect.DENY, ReasonCode.IMPLICIT_DENY);

    assertThat(log.isDenied()).isTrue();
    assertThat(log.wasImplicitDeny()).isTrue();
    assertThat(log.wasExplicitDeny()).isFalse();
  }
}
