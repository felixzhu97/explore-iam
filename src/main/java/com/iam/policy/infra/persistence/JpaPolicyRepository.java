package com.iam.policy.infra.persistence;

import com.iam.common.domain.model.ResourceName;
import com.iam.policy.domain.model.AllowPolicy;
import com.iam.policy.domain.model.PolicyBinding;
import com.iam.policy.domain.repository.PolicyRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaPolicyRepository implements PolicyRepository {

  private final SpringDataAllowPolicyRepository documentRepository;
  private final SpringDataPolicyBindingRepository attachmentRepository;

  JpaPolicyRepository(
      SpringDataAllowPolicyRepository documentRepository,
      SpringDataPolicyBindingRepository attachmentRepository) {
    this.documentRepository = documentRepository;
    this.attachmentRepository = attachmentRepository;
  }

  @Override
  public AllowPolicy save(AllowPolicy policy) {
    return documentRepository.save(policy);
  }

  @Override
  public Optional<AllowPolicy> findById(String id) {
    return documentRepository.findById(id);
  }

  @Override
  public List<AllowPolicy> findAll() {
    return documentRepository.findAll();
  }

  @Override
  public PolicyBinding saveAttachment(PolicyBinding attachment) {
    return attachmentRepository.save(attachment);
  }

  @Override
  public List<AllowPolicy> findAttachedToPrincipal(ResourceName principalArn) {
    List<AllowPolicy> policies = new ArrayList<>();
    for (PolicyBinding attachment : attachmentRepository.findByPrincipalArn(principalArn)) {
      findById(attachment.getPolicyId()).ifPresent(policies::add);
    }
    return policies;
  }
}
