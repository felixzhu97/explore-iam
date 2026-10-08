package com.iam.policy.domain.model;

import com.iam.common.domain.model.Effect;
import com.iam.common.domain.model.ReasonCode;
import java.util.Objects;

/** Result of policy evaluation with machine-readable reason. */
public record AccessDecision(Effect effect, ReasonCode reasonCode) {

  /** Validates authorization decision fields. */
  public AccessDecision {
    Objects.requireNonNull(effect, "effect");
    Objects.requireNonNull(reasonCode, "reasonCode");
  }

  /** Returns true when access is granted. */
  public boolean isAllowed() {
    return effect == Effect.ALLOW;
  }
}
