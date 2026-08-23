package com.iam.audit.service;

import com.iam.audit.domain.model.AuditOutcome;
import com.iam.audit.domain.model.AuthorizationDecisionLog;
import com.iam.audit.domain.model.ManagementEvent;
import com.iam.audit.domain.repository.AuditRepository;
import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.ReasonCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for recording and querying audit events. */
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
   * Records a management-plane event.
   *
   * @param actor principal performing the action
   * @param action action name
   * @param targetType target resource type
   * @param targetId target identifier
   * @param outcome success or failure
   */
  @Transactional
  public void recordManagement(
      String actor, String action, String targetType, String targetId, AuditOutcome outcome) {
    auditRepository.saveManagementEvent(
        ManagementEvent.record(actor, action, targetType, targetId, outcome));
  }

  /**
   * Records a policy authorization decision.
   *
   * @param principalId evaluated principal
   * @param action requested action
   * @param resource requested resource
   * @param effect decision effect
   * @param reasonCode machine-readable reason
   */
  @Transactional
  public void recordAuthorizationDecision(
      String principalId,
      String action,
      String resource,
      Effect effect,
      ReasonCode reasonCode) {
    auditRepository.saveAuthorizationDecision(
        AuthorizationDecisionLog.record(principalId, action, resource, effect, reasonCode));
  }

  /**
   * Queries recent audit events.
   *
   * @param limit maximum events per category
   * @return management and authorization events
   */
  @Transactional(readOnly = true)
  public AuditQueryResult query(int limit) {
    return new AuditQueryResult(
        auditRepository.findManagementEvents(limit),
        auditRepository.findAuthorizationDecisions(limit));
  }

  /** Combined audit query result. */
  public record AuditQueryResult(
      List<ManagementEvent> managementEvents,
      List<AuthorizationDecisionLog> authorizationDecisions) {}
}
