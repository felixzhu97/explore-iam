package com.iam.policy.domain.model;

import com.iam.common.domain.base.AbstractImmutable;
import com.iam.common.domain.model.ResourceName;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Links a Policy Document to a principal or resource ARN. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PolicyBinding extends AbstractImmutable {

  @Getter(AccessLevel.NONE)
  @NotBlank
  @Size(max = 36)
  @Column(nullable = false, length = 36)
  private String policyId;

  @Getter(AccessLevel.NONE)
  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "principal_arn", nullable = false, length = 512))
  @Valid
  private ResourceName principalArn;

  private PolicyBinding(String id, String policyId, ResourceName principalArn, Instant createdAt) {
    super(id, createdAt);
    this.policyId = policyId;
    this.principalArn = principalArn;
  }

  /**
   * Attaches a policy to a principal ARN.
   *
   * @param policyId policy document id
   * @param principalArn target principal
   * @return new attachment
   */
  public static PolicyBinding attach(String policyId, ResourceName principalArn) {
    return new PolicyBinding(
        UUID.randomUUID().toString(), policyId, principalArn, Instant.now());
  }

  /** Returns the attached policy document id. */
  public String policyId() {
    return policyId;
  }

  /** Returns the principal ARN this policy is attached to. */
  public ResourceName principalArn() {
    return principalArn;
  }
}
