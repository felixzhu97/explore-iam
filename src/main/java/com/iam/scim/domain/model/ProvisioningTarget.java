package com.iam.scim.domain.model;

import com.iam.common.domain.base.AbstractNamedEntity;
import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OptimisticLock;

/** Downstream SCIM Service Provider that IAM pushes users to. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ProvisioningTarget extends AbstractNamedEntity {

  @NotBlank
  @Size(max = 512)
  @Column(nullable = false, length = 512)
  private String baseUrl;

  @NotBlank
  @Size(max = 128)
  @Column(nullable = false, length = 128)
  private String registrationId;

  @Column(nullable = false)
  private boolean enabled;

  @Size(max = 36)
  @Column(length = 36)
  private String assignmentGroupId;

  @Getter(AccessLevel.NONE)
  @OptimisticLock(excluded = true)
  @OneToMany(mappedBy = "target", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<ProvisionedAccount> accounts = new ArrayList<>();

  private ProvisioningTarget(
      String id,
      String name,
      String baseUrl,
      String registrationId,
      String assignmentGroupId,
      Instant now) {
    super(id, name, now, now);
    this.baseUrl = requireBaseUrl(baseUrl);
    this.registrationId = DomainStrings.requireNonBlank(registrationId, "registrationId");
    this.assignmentGroupId = blankToNull(assignmentGroupId);
    this.enabled = true;
  }

  /**
   * Creates an enabled target.
   *
   * @param name unique target name, e.g. {@code explore-ai}
   * @param baseUrl SCIM base URL ending in {@code /scim/v2}
   * @param registrationId OAuth client registration used for pushes
   * @param assignmentGroupId optional group limiting which users are pushed
   * @return new aggregate
   */
  public static ProvisioningTarget createProvisioningTarget(
      String name, String baseUrl, String registrationId, String assignmentGroupId) {
    return new ProvisioningTarget(
        UUID.randomUUID().toString(),
        name,
        baseUrl,
        registrationId,
        assignmentGroupId,
        Instant.now());
  }

  /**
   * Updates connection settings from configuration.
   *
   * @param baseUrl SCIM base URL
   * @param registrationId OAuth client registration
   * @param assignmentGroupId optional assignment group
   */
  public void reconfigure(String baseUrl, String registrationId, String assignmentGroupId) {
    this.baseUrl = requireBaseUrl(baseUrl);
    this.registrationId = DomainStrings.requireNonBlank(registrationId, "registrationId");
    this.assignmentGroupId = blankToNull(assignmentGroupId);
    touch();
  }

  /** Enables pushes to this target. */
  public void enableTarget() {
    if (enabled) {
      return;
    }
    this.enabled = true;
    touch();
  }

  /** Stops pushes to this target. */
  public void disableTarget() {
    if (!enabled) {
      return;
    }
    this.enabled = false;
    touch();
  }

  /** Returns true when only members of the assignment group are pushed. */
  public boolean requiresAssignment() {
    return assignmentGroupId != null;
  }

  /**
   * Returns true when the group is this target's assignment group.
   *
   * @param groupId IAM group id
   * @return whether membership in the group controls provisioning
   */
  public boolean isAssignmentGroup(String groupId) {
    return assignmentGroupId != null && assignmentGroupId.equals(groupId);
  }

  /**
   * Records that a user exists on the target at a given IAM version.
   *
   * @param iamUserId IAM user id
   * @param remoteId id assigned by the target
   * @param version IAM user version that was pushed
   */
  public void recordProvisionedAccount(String iamUserId, String remoteId, long version) {
    Optional<ProvisionedAccount> existing = findProvisionedAccount(iamUserId);
    if (existing.isPresent()) {
      existing.get().markSynced(remoteId, version);
      return;
    }
    accounts.add(new ProvisionedAccount(this, iamUserId, remoteId, version));
  }

  /**
   * Marks the account as needing a full push, e.g. after it was deactivated because the user left
   * the assignment group.
   *
   * @param iamUserId IAM user id
   */
  public void markProvisionedAccountStale(String iamUserId) {
    findProvisionedAccount(iamUserId).ifPresent(ProvisionedAccount::markStale);
  }

  /**
   * Forgets the mapping after the user was deleted on the target.
   *
   * @param iamUserId IAM user id
   */
  public void forgetProvisionedAccount(String iamUserId) {
    accounts.removeIf(account -> account.iamUserId().equals(iamUserId));
  }

  /**
   * Finds the mapping for a user.
   *
   * @param iamUserId IAM user id
   * @return mapping when the user was pushed before
   */
  public Optional<ProvisionedAccount> findProvisionedAccount(String iamUserId) {
    return accounts.stream().filter(account -> account.iamUserId().equals(iamUserId)).findFirst();
  }

  private static String requireBaseUrl(String baseUrl) {
    String value = DomainStrings.requireNonBlank(baseUrl, "baseUrl");
    URI uri = URI.create(value);
    boolean loopback = "localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost());
    if (!"https".equals(uri.getScheme()) && !("http".equals(uri.getScheme()) && loopback)) {
      throw new IllegalArgumentException("baseUrl must be https outside localhost");
    }
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
