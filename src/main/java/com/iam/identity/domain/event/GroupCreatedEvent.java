package com.iam.identity.domain.event;

/**
 * Group created event.
 *
 * @param groupId IAM group id
 */
public record GroupCreatedEvent(String groupId) {}
