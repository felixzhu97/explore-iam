package com.iam.audit.infra;

import com.iam.audit.domain.vo.AuditActor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Resolves the current principal for management audit events. */
public final class SecurityAuditSupport {

  private static final String UNKNOWN_ACTOR = "unknown";

  private SecurityAuditSupport() {}

  /**
   * Returns an audit actor for the authenticated principal.
   *
   * @return audit actor
   */
  public static AuditActor currentActor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return new AuditActor(UNKNOWN_ACTOR);
    }
    String name = authentication.getName();
    if (name == null || name.isBlank()) {
      return new AuditActor(UNKNOWN_ACTOR);
    }
    return new AuditActor(name);
  }
}
