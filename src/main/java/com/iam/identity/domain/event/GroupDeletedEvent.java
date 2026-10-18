package com.iam.identity.domain.event;

/**
 * Group deleted event.
 *
 * @param groupId id of the deleted IAM group
 */
public record GroupDeletedEvent(String groupId) {}
