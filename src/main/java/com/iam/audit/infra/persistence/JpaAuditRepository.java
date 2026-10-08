package com.iam.audit.infra.persistence;

import com.iam.audit.domain.model.AdminActivity;
import com.iam.audit.domain.model.DataAccessLog;
import com.iam.audit.domain.repository.AuditRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
class JpaAuditRepository implements AuditRepository {

  private final SpringDataAdminActivityRepository managementRepository;
  private final SpringDataDataAccessLogRepository decisionRepository;

  JpaAuditRepository(
      SpringDataAdminActivityRepository managementRepository,
      SpringDataDataAccessLogRepository decisionRepository) {
    this.managementRepository = managementRepository;
    this.decisionRepository = decisionRepository;
  }

  @Override
  public AdminActivity saveAdminActivity(AdminActivity event) {
    return managementRepository.save(event);
  }

  @Override
  public DataAccessLog saveAccessDecision(DataAccessLog log) {
    return decisionRepository.save(log);
  }

  @Override
  public List<AdminActivity> findAdminActivities(int limit) {
    return managementRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
  }

  @Override
  public List<DataAccessLog> findAccessDecisions(int limit) {
    return decisionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
  }
}
