package com.iam.audit.controller;

import com.iam.audit.domain.model.AuthorizationDecisionLog;
import com.iam.audit.domain.model.ManagementEvent;
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
@RequestMapping("/api/audit")
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
   * @param limit maximum events per category (capped at 200)
   * @return combined audit events
   */
  @GetMapping("/events")
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public AuditEventsResponse listEvents(@RequestParam(defaultValue = "50") int limit) {
    AuditQueryResult result = auditService.query(Math.min(limit, 200));
    return new AuditEventsResponse(
        result.managementEvents().stream().map(ManagementEventResponse::from).toList(),
        result.authorizationDecisions().stream().map(AuthorizationDecisionResponse::from).toList());
  }

  /** Combined management and authorization audit events. */
  public record AuditEventsResponse(
      List<ManagementEventResponse> managementEvents,
      List<AuthorizationDecisionResponse> authorizationDecisions) {}

  /** Management-plane audit event. */
  public record ManagementEventResponse(
      String id,
      String actor,
      String action,
      String targetType,
      String targetId,
      String outcome,
      String occurredAt) {
    static ManagementEventResponse from(ManagementEvent event) {
      return new ManagementEventResponse(
          event.getId(),
          event.getActor().getValue(),
          event.getAction(),
          event.getTarget().getType(),
          event.getTarget().getId(),
          event.getOutcome().name(),
          event.getOccurredAt().toString());
    }
  }

  /** Authorization decision audit event. */
  public record AuthorizationDecisionResponse(
      String id,
      String principalId,
      String action,
      String resource,
      String effect,
      String reasonCode,
      String occurredAt) {
    static AuthorizationDecisionResponse from(AuthorizationDecisionLog log) {
      return new AuthorizationDecisionResponse(
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
