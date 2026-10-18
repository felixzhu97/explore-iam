package com.iam.identity.domain.event;

/**
 * Role created event.
 *
 * @param roleId IAM role id
 */
public record RoleCreatedEvent(String roleId) {}
