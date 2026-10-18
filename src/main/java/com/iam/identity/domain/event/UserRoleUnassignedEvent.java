package com.iam.identity.domain.event;

/**
 * User role unassigned event.
 *
 * @param userId IAM user id
 * @param roleId IAM role id
 */
public record UserRoleUnassignedEvent(String userId, String roleId) {}
