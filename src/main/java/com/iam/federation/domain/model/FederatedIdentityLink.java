package com.iam.federation.domain.model;

import com.iam.common.domain.base.AbstractImmutable;
import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Maps an external IdP subject to a local IAM user. */
@Entity
@Table(name = "iam_federated_identity_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class FederatedIdentityLink extends AbstractImmutable {

  @Column(name = "iam_user_id", nullable = false, length = 36)
  private String iamUserId;

  @Column(nullable = false, length = 64)
  private String provider;

  @Column(name = "external_subject", nullable = false, length = 512)
  private String externalSubject;

  private FederatedIdentityLink(
      String id, String iamUserId, String provider, String externalSubject, Instant createdAt) {
    super(id, createdAt);
    this.iamUserId = iamUserId;
    this.provider = requireProvider(provider);
    this.externalSubject = externalSubject;
  }

  /** Creates a new federated identity link. */
  public static FederatedIdentityLink create(
      String iamUserId, String provider, String externalSubject) {
    return new FederatedIdentityLink(
        UUID.randomUUID().toString(), iamUserId, provider, externalSubject, Instant.now());
  }

  /** Rebuilds from persistence. */
  public static FederatedIdentityLink reconstitute(
      String id, String iamUserId, String provider, String externalSubject, Instant createdAt) {
    return new FederatedIdentityLink(id, iamUserId, provider, externalSubject, createdAt);
  }

  private static String requireProvider(String provider) {
    return DomainStrings.requireNonBlank(provider, "provider");
  }
}
