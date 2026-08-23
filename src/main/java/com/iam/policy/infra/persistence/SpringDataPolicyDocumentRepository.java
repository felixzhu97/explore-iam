package com.iam.policy.infra.persistence;

import com.iam.policy.domain.model.PolicyDocument;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPolicyDocumentRepository extends JpaRepository<PolicyDocument, String> {}
