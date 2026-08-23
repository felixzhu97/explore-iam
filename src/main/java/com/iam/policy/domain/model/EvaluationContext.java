package com.iam.policy.domain.model;

import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.PrincipalId;
import com.iam.common.domain.vo.Resource;
import java.util.Objects;

/** Input to policy evaluation: principal, action, and resource. */
public record EvaluationContext(PrincipalId principalId, Action action, Resource resource) {

  /** Validates evaluation context fields. */
  public EvaluationContext {
    Objects.requireNonNull(principalId, "principalId");
    Objects.requireNonNull(action, "action");
    Objects.requireNonNull(resource, "resource");
  }
}
