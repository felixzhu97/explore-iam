package com.iam.policy.infra.persistence;

import com.iam.common.domain.vo.Arn;
import com.iam.policy.domain.model.PolicyAttachment;
import com.iam.policy.domain.model.PolicyDocument;
import com.iam.policy.domain.repository.PolicyRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaPolicyRepository implements PolicyRepository {

  private final SpringDataPolicyDocumentRepository documentRepository;
  private final SpringDataPolicyAttachmentRepository attachmentRepository;

  JpaPolicyRepository(
      SpringDataPolicyDocumentRepository documentRepository,
      SpringDataPolicyAttachmentRepository attachmentRepository) {
    this.documentRepository = documentRepository;
    this.attachmentRepository = attachmentRepository;
  }

  @Override
  public PolicyDocument save(PolicyDocument policy) {
    return documentRepository.save(policy);
  }

  @Override
  public Optional<PolicyDocument> findById(String id) {
    return documentRepository.findById(id);
  }

  @Override
  public List<PolicyDocument> findAll() {
    return documentRepository.findAll();
  }

  @Override
  public PolicyAttachment saveAttachment(PolicyAttachment attachment) {
    return attachmentRepository.save(attachment);
  }

  @Override
  public List<PolicyDocument> findAttachedToPrincipal(Arn principalArn) {
    List<PolicyDocument> policies = new ArrayList<>();
    for (PolicyAttachment attachment : attachmentRepository.findByPrincipalArn(principalArn)) {
      findById(attachment.getPolicyId()).ifPresent(policies::add);
    }
    return policies;
  }
}
