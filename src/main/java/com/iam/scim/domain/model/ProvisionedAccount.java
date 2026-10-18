package com.iam.scim.domain.model;

import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Maps an IAM user to the id a Provisioning Target assigned, with the last synced version. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ProvisionedAccount {

  private static final long NEVER_SYNCED = -1;

  @EmbeddedId private Pk id;

  @Getter(AccessLevel.PACKAGE)
  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("targetId")
  @JoinColumn(nullable = false)
  private ProvisioningTarget target;

  @Column(nullable = false, length = 256)
  private String remoteId;

  @Column(nullable = false)
  private long lastSyncedVersion;

  @Column(nullable = false)
  private Instant lastSyncedAt;

  ProvisionedAccount(
      ProvisioningTarget target, String iamUserId, String remoteId, long lastSyncedVersion) {
    this.target = Objects.requireNonNull(target, "target");
    this.id = new Pk(target.getId(), DomainStrings.requireNonBlank(iamUserId, "iamUserId"));
    markSynced(remoteId, lastSyncedVersion);
  }

  /** Returns the IAM user id. */
  public String iamUserId() {
    return id.iamUserId;
  }

  /**
   * Returns true when this version was already pushed.
   *
   * @param version IAM user version
   * @return whether a push can be skipped
   */
  public boolean isSyncedAt(long version) {
    return lastSyncedVersion >= version;
  }

  void markStale() {
    this.lastSyncedVersion = NEVER_SYNCED;
  }

  void markSynced(String remoteId, long version) {
    this.remoteId = DomainStrings.requireNonBlank(remoteId, "remoteId");
    this.lastSyncedVersion = version;
    this.lastSyncedAt = Instant.now();
  }

  /** Composite key: target id + IAM user id. */
  @Embeddable
  @Getter
  @EqualsAndHashCode
  @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static class Pk implements Serializable {
    private String targetId;
    private String iamUserId;
  }
}
