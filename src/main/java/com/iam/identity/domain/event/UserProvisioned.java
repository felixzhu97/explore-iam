package com.iam.identity.domain.event;

import com.iam.common.domain.base.DomainStrings;

/**
 * Domain event: a user was created.
 *
 * @param userId IAM user id
 */
public record UserProvisioned(String userId) {

  /** Validates the user id. */
  public UserProvisioned {
    userId = DomainStrings.requireNonBlank(userId, "userId");
  }
}
