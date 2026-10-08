package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractNamedEntity;
import com.iam.common.domain.model.ResourceName;
import com.iam.identity.domain.converter.ImpersonationPolicyConverter;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.Valid;
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

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "arn", nullable = false, unique = true, length = 512))
  @Valid
  private ResourceName resourceName;

  @Column(columnDefinition = "clob")
  @Convert(converter = ImpersonationPolicyConverter.class)
  private ImpersonationPolicy trustPolicy;

  private Role(
      String id,
      String name,
      ResourceName resourceName,
      ImpersonationPolicy trustPolicy,
      Instant createdAt,
      Instant updatedAt) {
    super(id, name, createdAt, updatedAt);
    this.resourceName = resourceName;
    this.trustPolicy = trustPolicy;
  }

  /**
   * Creates a new IAM role.
   *
   * @param name unique role name (used in ARN)
   * @param trustPolicy optional trust policy JSON
   * @return new aggregate
   */
  public static Role createRole(String name, ImpersonationPolicy trustPolicy) {
    Instant now = Instant.now();
    String slug = name.trim().toLowerCase().replace(' ', '-');
    return new Role(
        UUID.randomUUID().toString(),
        name,
        ResourceName.createRoleResourceName(slug),
        trustPolicy,
        now,
        now);
  }

  /**
   * Creates a role using optional trust policy JSON.
   *
   * @param name role name
   * @param trustPolicyJson trust policy JSON; blank uses allow-all default
   * @return new aggregate
   */
  public static Role createRole(String name, String trustPolicyJson) {
    ImpersonationPolicy trust =
        trustPolicyJson == null || trustPolicyJson.isBlank()
            ? ImpersonationPolicy.createPermissiveImpersonationPolicy()
            : new ImpersonationPolicy(trustPolicyJson);
    return createRole(name, trust);
  }

  /** Returns the Spring Security authority for this role. */
  public String authority() {
    return "ROLE_" + getName();
  }
}
