package com.iam.audit.service;

import com.iam.audit.domain.model.AdminActivity;
import com.iam.audit.domain.model.DataAccessLog;
import com.iam.audit.domain.repository.AuditRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for persisting and querying audit aggregates. */
@Service
public class AuditService {

  private final AuditRepository auditRepository;

  /**
   * Creates the audit service.
   *
   * @param auditRepository audit repository
   */
  public AuditService(AuditRepository auditRepository) {
    this.auditRepository = auditRepository;
  }

  /**
   * Persists a management-plane audit aggregate.
   *
   * @param event management event aggregate
   * @return stored aggregate
   */
  @Transactional
  public AdminActivity save(AdminActivity event) {
    return auditRepository.saveAdminActivity(event);
  }

  /**
   * Persists an authorization decision audit aggregate.
   *
   * @param log authorization decision aggregate
   * @return stored aggregate
   */
  @Transactional
  public DataAccessLog save(DataAccessLog log) {
    return auditRepository.saveAccessDecision(log);
  }

  /**
   * Queries recent audit aggregates.
   *
   * @param limit maximum events per category
   * @return management and authorization aggregates
   */
  @Transactional(readOnly = true)
  public AuditQueryResult query(int limit) {
    return new AuditQueryResult(
        auditRepository.findAdminActivities(limit),
        auditRepository.findAccessDecisions(limit));
  }

  /** Combined audit query result. */
  public record AuditQueryResult(
      List<AdminActivity> managementEvents,
      List<DataAccessLog> authorizationDecisions) {}
}
