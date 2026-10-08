package com.iam.policy.domain.model;

import com.iam.common.domain.model.Permission;
import com.iam.common.domain.model.PrincipalId;
import com.iam.common.domain.model.Resource;
import java.util.Objects;

/** Input to policy evaluation: principal, action, and resource. */
public record AccessTuple(PrincipalId principalId, Permission action, Resource resource) {

  /** Validates evaluation context fields. */
  public AccessTuple {
    Objects.requireNonNull(principalId, "principalId");
    Objects.requireNonNull(action, "action");
    Objects.requireNonNull(resource, "resource");
  }
}
