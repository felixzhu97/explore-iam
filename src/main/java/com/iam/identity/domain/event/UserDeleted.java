package com.iam.identity.domain.event;

import com.iam.common.domain.base.DomainStrings;

/**
 * Domain event: a user was removed.
 *
 * @param userId IAM user id
 */
public record UserDeleted(String userId) {

  /** Validates the user id. */
  public UserDeleted {
    userId = DomainStrings.requireNonBlank(userId, "userId");
  }
}
