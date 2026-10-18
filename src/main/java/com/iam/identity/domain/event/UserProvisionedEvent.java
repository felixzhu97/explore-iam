package com.iam.identity.domain.event;

/**
 * User provisioned event.
 *
 * @param userId IAM user id
 */
public record UserProvisionedEvent(String userId) {}
