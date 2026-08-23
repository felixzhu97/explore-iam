package com.iam.audit.service;

import com.iam.audit.domain.model.AuditOutcome;
import com.iam.audit.domain.model.ManagementEvent;
import com.iam.audit.domain.vo.AuditActor;
import com.iam.audit.domain.vo.AuditTarget;
import com.iam.audit.infra.SecurityAuditSupport;
import org.springframework.stereotype.Service;

/** Records management-plane audit aggregates. */
@Service
public class ManagementAuditRecorder {

  private final AuditService auditService;

  /**
   * Creates the recorder.
   *
   * @param auditService audit application service
   */
  public ManagementAuditRecorder(AuditService auditService) {
    this.auditService = auditService;
  }

  /**
   * Records a successful management action.
   *
   * @param action action name
   * @param targetType target resource type
   * @param targetId target identifier
   */
  public void recordSuccess(String action, String targetType, String targetId) {
    record(action, targetType, targetId, AuditOutcome.SUCCESS);
  }

  /**
   * Records a management action with the given outcome.
   *
   * @param action action name
   * @param targetType target resource type
   * @param targetId target identifier
   * @param outcome success or failure
   */
  public void record(String action, String targetType, String targetId, AuditOutcome outcome) {
    AuditActor actor = SecurityAuditSupport.currentActor();
    auditService.save(
        ManagementEvent.logManagementAction(
            actor, action, new AuditTarget(targetType, targetId), outcome));
  }
}
