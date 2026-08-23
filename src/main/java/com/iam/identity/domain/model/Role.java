package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractNamedEntity;
import com.iam.common.domain.converter.ArnAttributeConverter;
import com.iam.common.domain.vo.Arn;
import com.iam.identity.domain.converter.TrustPolicyDocumentConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** IAM role with an optional trust policy for STS assume-role. */
@Entity
@Table(name = "iam_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Role extends AbstractNamedEntity {

  @Column(nullable = false, unique = true, length = 512)
  @Convert(converter = ArnAttributeConverter.class)
  private Arn arn;

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

  /** Returns the Spring Security authority for this role. */
  public String authority() {
    return "ROLE_" + getName();
  }
}
