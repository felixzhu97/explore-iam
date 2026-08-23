package com.iam.audit.infra.persistence;

import com.iam.audit.domain.model.AuthorizationDecisionLog;
import com.iam.audit.domain.model.ManagementEvent;
import com.iam.audit.domain.repository.AuditRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
class JpaAuditRepository implements AuditRepository {

  private final SpringDataManagementEventRepository managementRepository;
  private final SpringDataAuthorizationDecisionLogRepository decisionRepository;

  JpaAuditRepository(
      SpringDataManagementEventRepository managementRepository,
      SpringDataAuthorizationDecisionLogRepository decisionRepository) {
    this.managementRepository = managementRepository;
    this.decisionRepository = decisionRepository;
  }

  @Override
  public ManagementEvent saveManagementEvent(ManagementEvent event) {
    return managementRepository.save(event);
  }

  @Override
  public AuthorizationDecisionLog saveAuthorizationDecision(AuthorizationDecisionLog log) {
    return decisionRepository.save(log);
  }

  @Override
  public List<ManagementEvent> findManagementEvents(int limit) {
    return managementRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
  }

  @Override
  public List<AuthorizationDecisionLog> findAuthorizationDecisions(int limit) {
    return decisionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
  }
}
