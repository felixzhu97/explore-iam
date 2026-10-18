package com.iam.identity.domain.event;

/**
 * User deletion requested event, published before the user row is deleted.
 *
 * @param userId IAM user id
 */
public record UserDeletionRequestedEvent(String userId) {}
