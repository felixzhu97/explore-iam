package com.iam.policy.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.common.domain.model.Effect;
import com.iam.common.domain.model.Permission;
import com.iam.common.domain.model.PrincipalId;
import com.iam.common.domain.model.ReasonCode;
import com.iam.common.domain.model.Resource;
import com.iam.common.domain.model.ResourceName;
import com.iam.policy.domain.model.AccessDecision;
import com.iam.policy.domain.model.AccessTuple;
import com.iam.policy.domain.model.AllowPolicy;
import com.iam.policy.domain.model.PolicyStatement;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PolicyEngine")
class PolicyEngineTest {

  private final PolicyEngine engine = new PolicyEngine();

  @Test
  @DisplayName("should deny when no policies match")
  void shouldDenyWhenNoPoliciesMatch() {
    AccessTuple context =
        new AccessTuple(
            new PrincipalId("user-1"),
            new Permission("s3:GetObject"),
            new Resource("arn:aws:s3:::bucket/key"));
    AccessDecision decision = engine.evaluate(context, List.of());
    assertThat(decision.isAllowed()).isFalse();
    assertThat(decision.reasonCode()).isEqualTo(ReasonCode.IMPLICIT_DENY);
  }

  @Test
  @DisplayName("should allow when explicit allow matches")
  void shouldAllowWhenExplicitAllowMatches() {
    AllowPolicy policy =
        AllowPolicy.create(
            "read-bucket",
            List.of(
                PolicyStatement.of(
                    Effect.ALLOW,
                    Set.of(new Permission("s3:GetObject")),
                    Set.of(new Resource("*")))));
    AccessTuple context =
        new AccessTuple(
            new PrincipalId("user-1"),
            new Permission("s3:GetObject"),
            new Resource("arn:aws:s3:::bucket/key"));
    AccessDecision decision = engine.evaluate(context, List.of(policy));
    assertThat(decision.isAllowed()).isTrue();
    assertThat(decision.reasonCode()).isEqualTo(ReasonCode.EXPLICIT_ALLOW);
  }

  @Test
  @DisplayName("should deny over allow when explicit deny matches")
  void shouldDenyOverAllowWhenExplicitDenyMatches() {
    AllowPolicy allowPolicy =
        AllowPolicy.create(
            "allow-all",
            List.of(
                PolicyStatement.of(
                    Effect.ALLOW,
                    Set.of(new Permission("*")),
                    Set.of(new Resource("*")))));
    AllowPolicy denyPolicy =
        AllowPolicy.create(
            "deny-delete",
            List.of(
                PolicyStatement.of(
                    Effect.DENY,
                    Set.of(new Permission("s3:DeleteObject")),
                    Set.of(new Resource("*")))));
    AccessTuple context =
        new AccessTuple(
            new PrincipalId("user-1"),
            new Permission("s3:DeleteObject"),
            new Resource("arn:aws:s3:::bucket/key"));
    AccessDecision decision =
        engine.evaluate(context, List.of(allowPolicy, denyPolicy));
    assertThat(decision.isAllowed()).isFalse();
    assertThat(decision.reasonCode()).isEqualTo(ReasonCode.EXPLICIT_DENY);
  }
}
