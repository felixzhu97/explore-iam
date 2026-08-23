package com.iam.audit.infra.persistence;

import com.iam.audit.domain.model.AuthorizationDecisionLog;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAuthorizationDecisionLogRepository
    extends JpaRepository<AuthorizationDecisionLog, String> {

  List<AuthorizationDecisionLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
