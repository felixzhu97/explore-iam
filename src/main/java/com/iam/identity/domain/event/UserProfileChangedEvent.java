package com.iam.identity.domain.event;

/**
 * User profile changed event.
 *
 * @param userId IAM user id
 */
public record UserProfileChangedEvent(String userId) {}
