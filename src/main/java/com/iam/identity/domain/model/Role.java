package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractNamedEntity;
import com.iam.common.domain.converter.ArnAttributeConverter;
import com.iam.common.domain.vo.Arn;
import com.iam.identity.domain.converter.TrustPolicyDocumentConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** IAM role with an optional trust policy for STS assume-role. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Role extends AbstractNamedEntity {

  @Getter(AccessLevel.NONE)
  @Column(nullable = false, unique = true, length = 512)
  @Convert(converter = ArnAttributeConverter.class)
  private Arn arn;

  @Getter(AccessLevel.NONE)
  @Column(name = "trust_policy_json", columnDefinition = "clob")
  @Convert(converter = TrustPolicyDocumentConverter.class)
  private TrustPolicyDocument trustPolicy;

  private Role(
      String id,
      String name,
      Arn arn,
      TrustPolicyDocument trustPolicy,
      Instant createdAt,
      Instant updatedAt) {
    super(id, name, createdAt, updatedAt);
    this.arn = arn;
    this.trustPolicy = trustPolicy;
  }

  /**
   * Creates a new IAM role.
   *
   * @param name unique role name (used in ARN)
   * @param trustPolicy optional trust policy JSON
   * @return new aggregate
   */
  public static Role create(String name, TrustPolicyDocument trustPolicy) {
    Instant now = Instant.now();
    String slug = name.trim().toLowerCase().replace(' ', '-');
    return new Role(
        UUID.randomUUID().toString(), name, Arn.role(slug), trustPolicy, now, now);
  }

  /**
   * Creates a role using optional trust policy JSON.
   *
   * @param name role name
   * @param trustPolicyJson trust policy JSON; blank uses allow-all default
   * @return new aggregate
   */
  public static Role create(String name, String trustPolicyJson) {
    TrustPolicyDocument trust =
        trustPolicyJson == null || trustPolicyJson.isBlank()
            ? TrustPolicyDocument.allowAll()
            : new TrustPolicyDocument(trustPolicyJson);
    return create(name, trust);
  }

  /**
   * Rebuilds a role from persistence.
   *
   * @param id internal id
   * @param name role name
   * @param arn role ARN
   * @param trustPolicy trust policy document
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @return reconstituted aggregate
   */
  public static Role reconstitute(
      String id,
      String name,
      Arn arn,
      TrustPolicyDocument trustPolicy,
      Instant createdAt,
      Instant updatedAt) {
    return new Role(id, name, arn, trustPolicy, createdAt, updatedAt);
  }

  /** Returns this role's ARN. */
  public Arn arn() {
    return arn;
  }

  /** Returns the trust policy governing who may assume this role. */
  public TrustPolicyDocument trustPolicy() {
    return trustPolicy;
  }

  /** Returns the Spring Security authority for this role. */
  public String authority() {
    return "ROLE_" + getName();
  }
}
