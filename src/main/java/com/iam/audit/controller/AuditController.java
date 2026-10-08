package com.iam.audit.controller;

import com.iam.audit.domain.model.AdminActivity;
import com.iam.audit.domain.model.DataAccessLog;
import com.iam.audit.service.AuditService;
import com.iam.audit.service.AuditService.AuditQueryResult;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Query API for management and authorization audit events. */
@RestController
@RequestMapping("/api/v1/auditEvents")
public class AuditController {

  private final AuditService auditService;

  /**
   * Creates the audit API controller.
   *
   * @param auditService audit application service
   */
  public AuditController(AuditService auditService) {
    this.auditService = auditService;
  }

  /**
   * Lists recent management and authorization audit events.
   *
   * @param pageSize maximum events per category ({@code page_size}, capped at 200)
   * @return combined audit events
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public AuditEventsResponse listEvents(
      @RequestParam(name = "page_size", defaultValue = "50") int pageSize) {
    AuditQueryResult result = auditService.query(Math.min(pageSize, 200));
    return new AuditEventsResponse(
        result.managementEvents().stream().map(AdminActivityResponse::from).toList(),
        result.authorizationDecisions().stream().map(AccessDecisionResponse::from).toList());
  }

  /** Combined management and authorization audit events. */
  public record AuditEventsResponse(
      List<AdminActivityResponse> managementEvents,
      List<AccessDecisionResponse> authorizationDecisions) {}

  /** Management-plane audit event. */
  public record AdminActivityResponse(
      String id,
      String actor,
      String action,
      String targetType,
      String targetId,
      String outcome,
      String occurredAt) {
    static AdminActivityResponse from(AdminActivity event) {
      return new AdminActivityResponse(
          event.getId(),
          event.getActor().getValue(),
          event.getAction(),
          event.getTarget().getType(),
          event.getTarget().getTargetId(),
          event.getOutcome().name(),
          event.getOccurredAt().toString());
    }
  }

  /** Authorization decision audit event. */
  public record AccessDecisionResponse(
      String id,
      String principalId,
      String action,
      String resource,
      String effect,
      String reasonCode,
      String occurredAt) {
    static AccessDecisionResponse from(DataAccessLog log) {
      return new AccessDecisionResponse(
          log.getId(),
          log.getPrincipalId().value(),
          log.getAction().value(),
          log.getResource().value(),
          log.getEffect().name(),
          log.getReasonCode().value(),
          log.getOccurredAt().toString());
    }
  }
}
