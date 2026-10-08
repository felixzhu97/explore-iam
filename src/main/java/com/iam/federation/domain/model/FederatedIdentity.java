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
public class FederatedIdentity extends AbstractImmutable {

  @Getter(AccessLevel.NONE)
  @NotBlank
  @Size(max = 36)
  @Column(nullable = false, length = 36)
  private String userId;

  @NotBlank
  @Size(max = 64)
  @Column(nullable = false, length = 64)
  private String provider;

  @NotBlank
  @Size(max = 512)
  @Column(nullable = false, length = 512)
  private String externalSubject;

  private FederatedIdentity(
      String id, String userId, String provider, String externalSubject, Instant createdAt) {
    super(id, createdAt);
    this.userId = userId;
    this.provider = requireProvider(provider);
    this.externalSubject = externalSubject;
  }

  /** Creates a new federated identity link. */
  public static FederatedIdentity create(
      String userId, String provider, String externalSubject) {
    return new FederatedIdentity(
        UUID.randomUUID().toString(), userId, provider, externalSubject, Instant.now());
  }

  /** Returns the linked local IAM user id. */
  public String linkedUserId() {
    return userId;
  }

  private static String requireProvider(String provider) {
    return DomainStrings.requireNonBlank(provider, "provider");
  }
}
