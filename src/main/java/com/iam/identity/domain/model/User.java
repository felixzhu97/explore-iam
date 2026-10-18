package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractEntity;
import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Long-lived IAM User identity used for local form login and OIDC subject mapping.
 *
 * <p>Table name is explicit because {@code user} is a reserved SQL word.
 */
@Entity
@Table(name = "directory_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class User extends AbstractEntity {

  private static final String FEDERATED_NO_PASSWORD = "{noop}federated-no-password";

  @NotBlank
  @Size(max = 128)
  @Column(nullable = false, unique = true, length = 128)
  private String username;

  @Email
  @Size(max = 320)
  @Column(length = 320)
  private String email;

  @Size(max = 256)
  @Column(length = 256)
  private String displayName;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "external_id", length = 256))
  @Valid
  private ExternalId externalId;

  @NotBlank
  @Size(max = 255)
  @Column(nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false)
  private boolean enabled;

  @Getter(AccessLevel.NONE)
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<RoleBinding> roleAssignments = new ArrayList<>();

  private User(
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
  public static User createUser(String username, String email, String passwordHash) {
    Instant now = Instant.now();
    return new User(
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
  public static User createFederatedUser(
      String provider, String subject, String email) {
    String username =
        DomainStrings.requireNonBlank(provider, "provider")
            + ":"
            + DomainStrings.requireNonBlank(subject, "subject");
    return createUser(username, email, FEDERATED_NO_PASSWORD);
  }

  /**
   * Creates a password-less user pushed by a SCIM Client; sign-in happens through federation.
   *
   * @param username unique login name
   * @param email contact email
   * @param displayName optional display name
   * @param externalId optional identifier from the provisioning client
   * @return new aggregate
   */
  public static User provisionUser(
      String username, String email, String displayName, ExternalId externalId) {
    User user = createUser(username, email, FEDERATED_NO_PASSWORD);
    user.displayName = normalizeDisplayName(displayName);
    user.externalId = externalId;
    return user;
  }

  /**
   * Disables the user so form login is rejected.
   *
   * @return whether the user was enabled before
   */
  public boolean disableUser() {
    if (!enabled) {
      return false;
    }
    this.enabled = false;
    touch();
    return true;
  }

  /**
   * Re-enables the user for form login.
   *
   * @return whether the user was disabled before
   */
  public boolean enableUser() {
    if (enabled) {
      return false;
    }
    this.enabled = true;
    touch();
    return true;
  }

  /**
   * Replaces username, email, and display name.
   *
   * @param username unique login name
   * @param email contact email
   * @param displayName optional display name
   * @return whether any field changed
   */
  public boolean updateProfile(String username, String email, String displayName) {
    String normalizedUsername = DomainStrings.requireNonBlank(username, "username");
    String normalizedDisplayName = normalizeDisplayName(displayName);
    if (normalizedUsername.equals(this.username)
        && Objects.equals(email, this.email)
        && Objects.equals(normalizedDisplayName, this.displayName)) {
      return false;
    }
    this.username = normalizedUsername;
    this.email = email;
    this.displayName = normalizedDisplayName;
    touch();
    return true;
  }

  /**
   * Sets or clears the External ID.
   *
   * @param externalId new identifier, or {@code null} to clear
   * @return whether the External ID changed
   */
  public boolean changeExternalId(ExternalId externalId) {
    if (Objects.equals(externalId, getExternalId())) {
      return false;
    }
    this.externalId = externalId;
    touch();
    return true;
  }

  /** Returns the External ID, or {@code null} when none is set. */
  public ExternalId getExternalId() {
    return externalId == null || externalId.value() == null ? null : externalId;
  }

  /** Returns true when the user has no local password and signs in through federation. */
  public boolean isPasswordless() {
    return FEDERATED_NO_PASSWORD.equals(passwordHash);
  }

  /**
   * Replaces the encoded password hash (operator reset).
   *
   * @param passwordHash new encoded password
   */
  public void resetPassword(String passwordHash) {
    this.passwordHash = requirePasswordHash(passwordHash);
    touch();
  }

  /**
   * Removes a role assignment when present.
   *
   * @param roleId role id
   * @return whether the role was assigned before
   */
  public boolean unassignRole(String roleId) {
    String normalized = DomainStrings.requireNonBlank(roleId, "roleId");
    if (!roleAssignments.removeIf(assignment -> assignment.roleId().equals(normalized))) {
      return false;
    }
    touch();
    return true;
  }

  /**
   * Updates the user's email address.
   *
   * @param email new email
   * @return whether the email changed
   */
  public boolean changeEmail(String email) {
    if (Objects.equals(email, this.email)) {
      return false;
    }
    this.email = email;
    touch();
    return true;
  }

  /**
   * Assigns a role when not already assigned.
   *
   * @param roleId role id
   * @return whether the role was newly assigned
   */
  public boolean assignRole(String roleId) {
    String normalized = DomainStrings.requireNonBlank(roleId, "roleId");
    if (hasRole(normalized)) {
      return false;
    }
    roleAssignments.add(new RoleBinding(this, normalized));
    touch();
    return true;
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
        roleAssignments.stream().map(RoleBinding::roleId).toList());
  }

  private static String normalizeDisplayName(String displayName) {
    if (displayName == null || displayName.isBlank()) {
      return null;
    }
    String trimmed = displayName.trim();
    if (trimmed.length() > 256) {
      throw new IllegalArgumentException("displayName exceeds 256 characters");
    }
    return trimmed;
  }

  private static String requirePasswordHash(String passwordHash) {
    return DomainStrings.requireNonBlank(passwordHash, "passwordHash");
  }
}
