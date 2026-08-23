package com.iam.policy.infra.persistence;

import com.iam.common.domain.vo.Arn;
import com.iam.policy.domain.model.PolicyAttachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPolicyAttachmentRepository extends JpaRepository<PolicyAttachment, String> {

  List<PolicyAttachment> findByPrincipalArn(Arn principalArn);
}
