package com.iam.policy.domain.repository;

import com.iam.common.domain.vo.Arn;
import com.iam.policy.domain.model.PolicyAttachment;
import com.iam.policy.domain.model.PolicyDocument;
import java.util.List;
import java.util.Optional;

/** Persistence port for policy documents and attachments. */
public interface PolicyRepository {

  /**
   * Persists a policy document.
   *
   * @param policy policy to store
   * @return stored policy
   */
  PolicyDocument save(PolicyDocument policy);

  /**
   * Finds a policy by id.
   *
   * @param id policy id
   * @return matching policy when present
   */
  Optional<PolicyDocument> findById(String id);

  /**
   * Lists all policy documents.
   *
   * @return all policies
   */
  List<PolicyDocument> findAll();

  /**
   * Persists a policy attachment.
   *
   * @param attachment attachment to store
   * @return stored attachment
   */
  PolicyAttachment saveAttachment(PolicyAttachment attachment);

  /**
   * Returns policies attached to the given principal ARN.
   *
   * @param principalArn principal ARN
   * @return attached policies
   */
  List<PolicyDocument> findAttachedToPrincipal(Arn principalArn);
}
