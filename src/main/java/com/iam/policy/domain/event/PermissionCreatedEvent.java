package com.iam.policy.domain.event;

/**
 * Permission created event.
 *
 * @param code permission catalog code
 */
public record PermissionCreatedEvent(String code) {}
