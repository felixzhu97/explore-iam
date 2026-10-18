package com.iam.identity.domain.event;

/**
 * User deleted event.
 *
 * @param userId id of the deleted IAM user
 */
public record UserDeletedEvent(String userId) {}
