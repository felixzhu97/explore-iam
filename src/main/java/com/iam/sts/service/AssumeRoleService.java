package com.iam.sts.service;

import com.iam.audit.domain.model.AuditOutcome;
import com.iam.audit.service.AuditService;
import com.iam.common.domain.vo.Arn;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.repository.RoleRepository;
import com.iam.sts.domain.model.AssumedRoleSession;
import com.iam.sts.domain.repository.AssumedRoleSessionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Issues temporary JWT credentials for an assumed IAM role. */
@Service
public class AssumeRoleService {

  private final RoleRepository roleRepository;
  private final AssumedRoleSessionRepository sessionRepository;
  private final JwtEncoder jwtEncoder;
  private final AuditService auditService;
  private final Duration sessionTtl;

  /**
   * Creates the assume-role use case.
   *
   * @param roleRepository role repository
   * @param sessionRepository assumed-role session repository
   * @param jwtEncoder JWT encoder
   * @param auditService audit application service
   * @param sessionTtl session time-to-live
   */
  public AssumeRoleService(
      RoleRepository roleRepository,
      AssumedRoleSessionRepository sessionRepository,
      JwtEncoder jwtEncoder,
      AuditService auditService,
      @Value("${app.security.token.assume-role-ttl:PT1H}") Duration sessionTtl) {
    this.roleRepository = roleRepository;
    this.sessionRepository = sessionRepository;
    this.jwtEncoder = jwtEncoder;
    this.auditService = auditService;
    this.sessionTtl = sessionTtl;
  }

  /**
   * Assumes a role and returns temporary credentials.
   *
   * @param command assume-role input
   * @return access token and session metadata
   */
  @Transactional
  public AssumeRoleResult execute(AssumeRoleCommand command) {
    Objects.requireNonNull(command, "command");
    Arn roleArn = new Arn(command.roleArn());
    Role role =
        roleRepository
            .findByArn(roleArn.value())
            .orElseThrow(() -> new IllegalArgumentException("role not found: " + roleArn));
    String caller = currentPrincipal();
    Instant expiresAt = Instant.now().plus(sessionTtl);
    AssumedRoleSession session =
        sessionRepository.save(
            AssumedRoleSession.create(roleArn, command.sessionName(), caller, expiresAt));
    String accessToken = encodeToken(session, role);
    auditService.recordManagement(
        caller, "sts:AssumeRole", "Role", role.getId(), AuditOutcome.SUCCESS);
    return new AssumeRoleResult(accessToken, session.getExpiresAt(), session.getId());
  }

  private String encodeToken(AssumedRoleSession session, Role role) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("explore-iam-sts")
            .subject(session.getCallerPrincipal())
            .issuedAt(now)
            .expiresAt(session.getExpiresAt())
            .claim("role_arn", session.getRoleArn().value())
            .claim("session_name", session.getSessionName())
            .claim("session_id", session.getId())
            .claim("role_name", role.getName())
            .build();
    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  private static String currentPrincipal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new IllegalStateException("caller must be authenticated");
    }
    return auth.getName();
  }

  /** Input for AssumeRole. */
  public record AssumeRoleCommand(String roleArn, String sessionName) {}

  /** Temporary credentials returned by AssumeRole. */
  public record AssumeRoleResult(String accessToken, Instant expiration, String sessionId) {}
}
