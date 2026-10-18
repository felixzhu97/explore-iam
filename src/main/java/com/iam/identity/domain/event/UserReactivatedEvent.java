package com.iam.identity.domain.event;

/**
 * User reactivated event.
 *
 * @param userId IAM user id
 */
public record UserReactivatedEvent(String userId) {}
