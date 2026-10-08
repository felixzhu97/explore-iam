package com.iam.policy.infra.persistence;

import com.iam.common.domain.model.ResourceName;
import com.iam.policy.domain.model.PolicyBinding;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPolicyBindingRepository extends JpaRepository<PolicyBinding, String> {

  List<PolicyBinding> findByPrincipalArn(ResourceName principalArn);
}
