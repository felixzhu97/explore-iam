package com.iam.identity.domain.model;

import java.util.Set;

/**
 * User ids that joined and left a group in one membership replacement.
 *
 * @param addedUserIds user ids that joined
 * @param removedUserIds user ids that left
 */
public record MembershipChange(Set<String> addedUserIds, Set<String> removedUserIds) {

  /** Copies the id sets. */
  public MembershipChange {
    addedUserIds = Set.copyOf(addedUserIds);
    removedUserIds = Set.copyOf(removedUserIds);
  }

  /** Returns true when nobody joined or left. */
  public boolean isEmpty() {
    return addedUserIds.isEmpty() && removedUserIds.isEmpty();
  }
}
