package com.iam.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Long-lived IAM User identity used for local form login and OIDC subject mapping. */
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

  /**
   * Creates a new enabled IAM user.
   *
   * @param username unique login name
   * @param email contact email
   * @param passwordHash encoded password
   * @return new aggregate
   */
  public static IamUser create(String username, String email, String passwordHash) {
    Instant now = Instant.now();
    return new IamUser(UUID.randomUUID().toString(), username, email, passwordHash, true, now, now);
  }

  /**
   * Rebuilds an aggregate from persistence.
   *
   * @param id internal id
   * @param username unique login name
   * @param email contact email
   * @param passwordHash encoded password
   * @param enabled whether login is allowed
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @return reconstituted aggregate
   */
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

  /** Disables the user so form login is rejected. */
  public void disable() {
    this.enabled = false;
    this.updatedAt = Instant.now();
  }

  /** Re-enables the user for form login. */
  public void enable() {
    this.enabled = true;
    this.updatedAt = Instant.now();
  }

  /**
   * Updates the user's email address.
   *
   * @param email new email
   */
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
