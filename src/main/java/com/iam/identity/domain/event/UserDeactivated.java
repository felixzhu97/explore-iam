package com.iam.identity.domain.event;

import com.iam.common.domain.base.DomainStrings;

/**
 * Domain event: a user was disabled.
 *
 * @param userId IAM user id
 */
public record UserDeactivated(String userId) {

  /** Validates the user id. */
  public UserDeactivated {
    userId = DomainStrings.requireNonBlank(userId, "userId");
  }
}
