package com.iam.policy.domain.event;

/**
 * Policy attached event.
 *
 * @param policyBindingId policy binding id
 */
public record PolicyAttachedEvent(String policyBindingId) {}
