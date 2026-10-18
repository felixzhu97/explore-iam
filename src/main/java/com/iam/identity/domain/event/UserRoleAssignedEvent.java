package com.iam.identity.domain.event;

/**
 * User role assigned event.
 *
 * @param userId IAM user id
 * @param roleId IAM role id
 */
public record UserRoleAssignedEvent(String userId, String roleId) {}
