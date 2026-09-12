package com.iam.federation.domain.model;

import com.iam.common.domain.base.AbstractImmutable;
import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Maps an external IdP subject to a local IAM user. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class FederatedIdentityLink extends AbstractImmutable {

  @Getter(AccessLevel.NONE)
  @NotBlank
  @Size(max = 36)
  @Column(nullable = false, length = 36)
  private String iamUserId;

  @NotBlank
  @Size(max = 64)
  @Column(nullable = false, length = 64)
  private String provider;

  @NotBlank
  @Size(max = 512)
  @Column(nullable = false, length = 512)
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

  /** Returns the linked local IAM user id. */
  public String linkedUserId() {
    return iamUserId;
  }

  private static String requireProvider(String provider) {
    return DomainStrings.requireNonBlank(provider, "provider");
  }
}
