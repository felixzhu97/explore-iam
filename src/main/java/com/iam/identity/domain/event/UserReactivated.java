package com.iam.identity.domain.event;

import com.iam.common.domain.base.DomainStrings;

/**
 * Domain event: a user was re-enabled.
 *
 * @param userId IAM user id
 */
public record UserReactivated(String userId) {

  /** Validates the user id. */
  public UserReactivated {
    userId = DomainStrings.requireNonBlank(userId, "userId");
  }
}
