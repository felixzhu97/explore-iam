package com.iam.policy.domain.model;

import com.iam.common.domain.base.AbstractImmutable;
import com.iam.common.domain.converter.ArnAttributeConverter;
import com.iam.common.domain.vo.Arn;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Links a Policy Document to a principal or resource ARN. */
@Entity
@Table(name = "iam_policy_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PolicyAttachment extends AbstractImmutable {

  @Column(name = "policy_id", nullable = false, length = 36)
  private String policyId;

  @Column(name = "principal_arn", nullable = false, length = 512)
  @Convert(converter = ArnAttributeConverter.class)
  private Arn principalArn;

  private PolicyAttachment(String id, String policyId, Arn principalArn, Instant createdAt) {
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
  public static PolicyAttachment attach(String policyId, Arn principalArn) {
    return new PolicyAttachment(
        UUID.randomUUID().toString(), policyId, principalArn, Instant.now());
  }

  /**
   * Rebuilds from persistence.
   *
   * @param id attachment id
   * @param policyId policy document id
   * @param principalArn target principal
   * @param createdAt creation timestamp
   * @return reconstituted attachment
   */
  public static PolicyAttachment reconstitute(
      String id, String policyId, Arn principalArn, Instant createdAt) {
    return new PolicyAttachment(id, policyId, principalArn, createdAt);
  }
}
