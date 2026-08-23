package com.iam.audit.infra;

import com.iam.audit.domain.model.AuditOutcome;
import com.iam.audit.domain.model.ManagementEvent;
import com.iam.audit.domain.vo.AuditActor;
import com.iam.audit.service.AuditService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/** Records authentication events into the audit log. */
@Component
public class AuthenticationAuditListener {

  private final AuditService auditService;

  /**
   * Creates the authentication audit listener.
   *
   * @param auditService audit service
   */
  public AuthenticationAuditListener(AuditService auditService) {
    this.auditService = auditService;
  }

  /** Records successful login events. */
  @EventListener
  public void onSuccess(AuthenticationSuccessEvent event) {
    auditService.save(
        ManagementEvent.logAuthentication(
            new AuditActor(event.getAuthentication().getName()), AuditOutcome.SUCCESS));
  }

  /** Records failed login events. */
  @EventListener
  public void onFailure(AbstractAuthenticationFailureEvent event) {
    String actorName =
        event.getAuthentication() == null ? "unknown" : event.getAuthentication().getName();
    auditService.save(
        ManagementEvent.logAuthentication(new AuditActor(actorName), AuditOutcome.FAILURE));
  }
}
