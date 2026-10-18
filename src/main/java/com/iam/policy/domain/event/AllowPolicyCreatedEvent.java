package com.iam.policy.domain.event;

/**
 * Allow policy created event.
 *
 * @param policyId allow policy id
 */
public record AllowPolicyCreatedEvent(String policyId) {}
