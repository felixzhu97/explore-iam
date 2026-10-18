package com.iam.identity.service;

import com.iam.identity.domain.model.ExternalId;

/**
 * Desired state of a provisioned user.
 *
 * @param username unique login name
 * @param email contact email, may be {@code null}
 * @param displayName display name, may be {@code null}
 * @param externalId identifier from the provisioning client, may be {@code null}
 * @param active whether the user may sign in
 */
public record UserProfile(
    String username, String email, String displayName, ExternalId externalId, boolean active) {}
