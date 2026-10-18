package com.iam.identity.domain.event;

import com.iam.common.domain.base.DomainStrings;

/**
 * Domain event: a user's username, email, display name, or External ID changed.
 *
 * @param userId IAM user id
 */
public record UserProfileChanged(String userId) {

  /** Validates the user id. */
  public UserProfileChanged {
    userId = DomainStrings.requireNonBlank(userId, "userId");
  }
}
