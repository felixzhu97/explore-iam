package com.iam.policy.domain.service;

import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.ReasonCode;
import com.iam.policy.domain.model.AuthorizationDecision;
import com.iam.policy.domain.model.EvaluationContext;
import com.iam.policy.domain.model.PolicyDocument;
import com.iam.policy.domain.model.PolicyStatement;
import java.util.List;

/** Evaluates policy documents with Deny &gt; Allow &gt; implicit Deny semantics. */
public class PolicyEngine {

  /**
   * Evaluates attached policies for the given context.
   *
   * @param context evaluation input
   * @param policies policies attached to the principal
   * @return authorization decision
   */
  public AuthorizationDecision evaluate(EvaluationContext context, List<PolicyDocument> policies) {
    boolean allowMatched = false;
    for (PolicyDocument policy : policies) {
      for (PolicyStatement statement : policy.getStatements()) {
        if (!statement.matches(context.action(), context.resource())) {
          continue;
        }
        if (statement.getEffect() == Effect.DENY) {
          return new AuthorizationDecision(Effect.DENY, ReasonCode.EXPLICIT_DENY);
        }
        if (statement.getEffect() == Effect.ALLOW) {
          allowMatched = true;
        }
      }
    }
    if (allowMatched) {
      return new AuthorizationDecision(Effect.ALLOW, ReasonCode.EXPLICIT_ALLOW);
    }
    return new AuthorizationDecision(Effect.DENY, ReasonCode.IMPLICIT_DENY);
  }
}
