package com.iam.policy.domain.repository;

import com.iam.common.domain.model.ResourceName;
import com.iam.policy.domain.model.AllowPolicy;
import com.iam.policy.domain.model.PolicyBinding;
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
  AllowPolicy save(AllowPolicy policy);

  /**
   * Finds a policy by id.
   *
   * @param id policy id
   * @return matching policy when present
   */
  Optional<AllowPolicy> findById(String id);

  /**
   * Lists all policy documents.
   *
   * @return all policies
   */
  List<AllowPolicy> findAll();

  /**
   * Persists a policy attachment.
   *
   * @param attachment attachment to store
   * @return stored attachment
   */
  PolicyBinding saveAttachment(PolicyBinding attachment);

  /**
   * Returns policies attached to the given principal ARN.
   *
   * @param principalArn principal ARN
   * @return attached policies
   */
  List<AllowPolicy> findAttachedToPrincipal(ResourceName principalArn);
}
