package com.iam.sts.domain.event;

/**
 * Role assumed event.
 *
 * @param roleId assumed IAM role id
 */
public record RoleAssumedEvent(String roleId) {}
