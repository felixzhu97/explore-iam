package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractEntity;
import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Long-lived IAM User identity used for local form login and OIDC subject mapping. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class IamUser extends AbstractEntity {

  private static final String FEDERATED_NO_PASSWORD = "{noop}federated-no-password";

  @Column(nullable = false, unique = true, length = 128)
  private String username;

  @Column(length = 320)
  private String email;

  @Getter(AccessLevel.NONE)
  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Getter(AccessLevel.NONE)
  @Column(nullable = false)
  private boolean enabled;

  @Getter(AccessLevel.NONE)
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<UserRoleAssignment> roleAssignments = new ArrayList<>();

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
   * Creates a federated-login user with a placeholder password hash.
   *
   * @param provider external identity provider id
   * @param subject external subject identifier
   * @param email optional email from the IdP
   * @return new aggregate
   */
  public static IamUser createForFederatedLogin(
      String provider, String subject, String email) {
    String username =
        DomainStrings.requireNonBlank(provider, "provider")
            + ":"
            + DomainStrings.requireNonBlank(subject, "subject");
    return create(username, email, FEDERATED_NO_PASSWORD);
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

  /**
   * Assigns a role when not already assigned.
   *
   * @param roleId role id
   */
  public void assignRole(String roleId) {
    String normalized = DomainStrings.requireNonBlank(roleId, "roleId");
    if (hasRole(normalized)) {
      return;
    }
    roleAssignments.add(new UserRoleAssignment(this, normalized));
    touch();
  }

  /**
   * Returns true when the role is assigned.
   *
   * @param roleId role id
   * @return whether the role is assigned
   */
  public boolean hasRole(String roleId) {
    return roleAssignments.stream().anyMatch(assignment -> assignment.roleId().equals(roleId));
  }

  /** Returns assigned role ids. */
  public List<String> assignedRoleIds() {
    return Collections.unmodifiableList(
        roleAssignments.stream().map(UserRoleAssignment::roleId).toList());
  }

  /**
   * Returns the encoded credential for Spring Security authentication only.
   *
   * @return password hash suitable for {@code UserDetails#getPassword()}
   */
  public String encodedPasswordHash() {
    return passwordHash;
  }

  /** Returns true when form login is permitted for this user. */
  public boolean isLoginEnabled() {
    return enabled;
  }

  private static String requirePasswordHash(String passwordHash) {
    return DomainStrings.requireNonBlank(passwordHash, "passwordHash");
  }
}
