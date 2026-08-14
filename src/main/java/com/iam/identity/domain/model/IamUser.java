package com.iam.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Long-lived IAM User identity used for local form login and OIDC subject mapping.
 */
public class IamUser {

    private final String id;
    private final String username;
    private String email;
    private String passwordHash;
    private boolean enabled;
    private final Instant createdAt;
    private Instant updatedAt;

    private IamUser(
            String id,
            String username,
            String email,
            String passwordHash,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.username = Objects.requireNonNull(username, "username");
        this.email = email;
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.enabled = enabled;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static IamUser create(String username, String email, String passwordHash) {
        Instant now = Instant.now();
        return new IamUser(UUID.randomUUID().toString(), username, email, passwordHash, true, now, now);
    }

    public static IamUser reconstitute(
            String id,
            String username,
            String email,
            String passwordHash,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt) {
        return new IamUser(id, username, email, passwordHash, enabled, createdAt, updatedAt);
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    public void changeEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
