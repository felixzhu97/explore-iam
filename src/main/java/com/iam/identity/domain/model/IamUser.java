package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractEntity;
import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Long-lived IAM User identity used for local form login and OIDC subject mapping. */
@Entity
@Table(name = "iam_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class IamUser extends AbstractEntity {

  @Column(nullable = false, unique = true, length = 128)
  private String username;

  @Column(length = 320)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false)
  private boolean enabled;

  private IamUser(
      String id,
      String username,
      String email,
      String passwordHash,
      boolean enabled,
      Instant createdAt,
      Instant updatedAt) {
    super(id, createdAt, updatedAt);
    this.username = DomainStrings.requireNonBlank(username, "username");
    this.email = email;
    this.passwordHash = requirePasswordHash(passwordHash);
    this.enabled = enabled;
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
    return new IamUser(
        UUID.randomUUID().toString(), username, email, passwordHash, true, now, now);
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
    touch();
  }

  /** Re-enables the user for form login. */
  public void enable() {
    this.enabled = true;
    touch();
  }

  /**
   * Updates the user's email address.
   *
   * @param email new email
   */
  public void changeEmail(String email) {
    this.email = email;
    touch();
  }

  private static String requirePasswordHash(String passwordHash) {
    return DomainStrings.requireNonBlank(passwordHash, "passwordHash");
  }
}
