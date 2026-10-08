package com.iam.policy.infra.persistence;

import com.iam.policy.domain.model.AllowPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAllowPolicyRepository extends JpaRepository<AllowPolicy, String> {}
