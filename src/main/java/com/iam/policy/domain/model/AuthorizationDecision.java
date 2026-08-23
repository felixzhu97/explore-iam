package com.iam.policy.domain.model;

import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.ReasonCode;
import java.util.Objects;

/** Result of policy evaluation with machine-readable reason. */
public record AuthorizationDecision(Effect effect, ReasonCode reasonCode) {

  /** Validates authorization decision fields. */
  public AuthorizationDecision {
    Objects.requireNonNull(effect, "effect");
    Objects.requireNonNull(reasonCode, "reasonCode");
  }

  /** Returns true when access is granted. */
  public boolean isAllowed() {
    return effect == Effect.ALLOW;
  }
}
