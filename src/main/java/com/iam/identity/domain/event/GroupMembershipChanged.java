package com.iam.identity.domain.event;

import com.iam.common.domain.base.DomainStrings;
import java.util.Set;

/**
 * Domain event: user ids were added to or removed from a group.
 *
 * @param groupId IAM group id
 * @param addedUserIds user ids that joined
 * @param removedUserIds user ids that left
 */
public record GroupMembershipChanged(
    String groupId, Set<String> addedUserIds, Set<String> removedUserIds) {

  /** Validates the group id and copies the id sets. */
  public GroupMembershipChanged {
    groupId = DomainStrings.requireNonBlank(groupId, "groupId");
    addedUserIds = Set.copyOf(addedUserIds);
    removedUserIds = Set.copyOf(removedUserIds);
  }
}
