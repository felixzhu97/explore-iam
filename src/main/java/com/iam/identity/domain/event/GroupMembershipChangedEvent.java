package com.iam.identity.domain.event;

import java.util.Set;

/**
 * Group membership changed event.
 *
 * @param groupId IAM group id
 * @param addedUserIds user ids that joined
 * @param removedUserIds user ids that left
 */
public record GroupMembershipChangedEvent(
    String groupId, Set<String> addedUserIds, Set<String> removedUserIds) {}
