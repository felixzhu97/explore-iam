package com.iam.identity.domain.event;

/**
 * User deactivated event.
 *
 * @param userId IAM user id
 */
public record UserDeactivatedEvent(String userId) {}
