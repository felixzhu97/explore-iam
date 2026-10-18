package com.iam.identity.domain.event;

/**
 * User password reset event.
 *
 * @param userId IAM user id
 */
public record UserPasswordResetEvent(String userId) {}
