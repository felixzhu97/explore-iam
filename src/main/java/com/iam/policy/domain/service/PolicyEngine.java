package com.iam.policy.domain.service;

import com.iam.common.domain.model.Effect;
import com.iam.common.domain.model.ReasonCode;
import com.iam.policy.domain.model.AccessDecision;
import com.iam.policy.domain.model.AccessTuple;
import com.iam.policy.domain.model.AllowPolicy;
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
  public AccessDecision evaluate(AccessTuple context, List<AllowPolicy> policies) {
    boolean allowMatched = false;
    for (AllowPolicy policy : policies) {
      for (PolicyStatement statement : policy.statements()) {
        if (!statement.matches(context.action(), context.resource())) {
          continue;
        }
        if (statement.effect() == Effect.DENY) {
          return new AccessDecision(Effect.DENY, ReasonCode.EXPLICIT_DENY);
        }
        if (statement.effect() == Effect.ALLOW) {
          allowMatched = true;
        }
      }
    }
    if (allowMatched) {
      return new AccessDecision(Effect.ALLOW, ReasonCode.EXPLICIT_ALLOW);
    }
    return new AccessDecision(Effect.DENY, ReasonCode.IMPLICIT_DENY);
  }
}
