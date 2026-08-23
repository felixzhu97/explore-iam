package com.iam.policy.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Arn;
import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.PrincipalId;
import com.iam.common.domain.vo.ReasonCode;
import com.iam.common.domain.vo.Resource;
import com.iam.policy.domain.model.AuthorizationDecision;
import com.iam.policy.domain.model.EvaluationContext;
import com.iam.policy.domain.model.PolicyDocument;
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
    EvaluationContext context =
        new EvaluationContext(
            new PrincipalId("user-1"),
            new Action("s3:GetObject"),
            new Resource("arn:aws:s3:::bucket/key"));
    AuthorizationDecision decision = engine.evaluate(context, List.of());
    assertThat(decision.isAllowed()).isFalse();
    assertThat(decision.reasonCode()).isEqualTo(ReasonCode.IMPLICIT_DENY);
  }

  @Test
  @DisplayName("should allow when explicit allow matches")
  void shouldAllowWhenExplicitAllowMatches() {
    PolicyDocument policy =
        PolicyDocument.create(
            "read-bucket",
            List.of(
                PolicyStatement.of(
                    Effect.ALLOW,
                    Set.of(new Action("s3:GetObject")),
                    Set.of(new Resource("*")))));
    EvaluationContext context =
        new EvaluationContext(
            new PrincipalId("user-1"),
            new Action("s3:GetObject"),
            new Resource("arn:aws:s3:::bucket/key"));
    AuthorizationDecision decision = engine.evaluate(context, List.of(policy));
    assertThat(decision.isAllowed()).isTrue();
    assertThat(decision.reasonCode()).isEqualTo(ReasonCode.EXPLICIT_ALLOW);
  }

  @Test
  @DisplayName("should deny over allow when explicit deny matches")
  void shouldDenyOverAllowWhenExplicitDenyMatches() {
    PolicyDocument allowPolicy =
        PolicyDocument.create(
            "allow-all",
            List.of(
                PolicyStatement.of(
                    Effect.ALLOW,
                    Set.of(new Action("*")),
                    Set.of(new Resource("*")))));
    PolicyDocument denyPolicy =
        PolicyDocument.create(
            "deny-delete",
            List.of(
                PolicyStatement.of(
                    Effect.DENY,
                    Set.of(new Action("s3:DeleteObject")),
                    Set.of(new Resource("*")))));
    EvaluationContext context =
        new EvaluationContext(
            new PrincipalId("user-1"),
            new Action("s3:DeleteObject"),
            new Resource("arn:aws:s3:::bucket/key"));
    AuthorizationDecision decision =
        engine.evaluate(context, List.of(allowPolicy, denyPolicy));
    assertThat(decision.isAllowed()).isFalse();
    assertThat(decision.reasonCode()).isEqualTo(ReasonCode.EXPLICIT_DENY);
  }
}
