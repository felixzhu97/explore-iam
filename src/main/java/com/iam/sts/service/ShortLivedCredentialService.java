package com.iam.sts.service;

import com.iam.audit.service.AdminActivityRecorder;
import com.iam.common.domain.model.ResourceName;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.repository.RoleRepository;
import com.iam.sts.domain.model.ShortLivedCredential;
import com.iam.sts.domain.repository.ShortLivedCredentialRepository;
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
public class ShortLivedCredentialService {

  private final RoleRepository roleRepository;
  private final ShortLivedCredentialRepository sessionRepository;
  private final JwtEncoder jwtEncoder;
  private final AdminActivityRecorder adminActivityRecorder;
  private final Duration sessionTtl;

  /**
   * Creates the assume-role service.
   *
   * @param roleRepository role repository
   * @param sessionRepository assumed-role session repository
   * @param jwtEncoder JWT encoder
   * @param adminActivityRecorder management audit recorder
   * @param sessionTtl session time-to-live
   */
  public ShortLivedCredentialService(
      RoleRepository roleRepository,
      ShortLivedCredentialRepository sessionRepository,
      JwtEncoder jwtEncoder,
      AdminActivityRecorder adminActivityRecorder,
      @Value("${app.security.token.assume-role-ttl:PT1H}") Duration sessionTtl) {
    this.roleRepository = roleRepository;
    this.sessionRepository = sessionRepository;
    this.jwtEncoder = jwtEncoder;
    this.adminActivityRecorder = adminActivityRecorder;
    this.sessionTtl = sessionTtl;
  }

  /**
   * Assumes a role and returns temporary credentials.
   *
   * @param command assume-role input
   * @return access token and session metadata
   */
  @Transactional
  public AssumeRoleResult assumeRole(AssumeRoleCommand command) {
    Objects.requireNonNull(command, "command");
    ResourceName roleArn = new ResourceName(command.roleArn());
    Role role =
        roleRepository
            .findByArn(roleArn.value())
            .orElseThrow(() -> new IllegalArgumentException("role not found: " + roleArn));
    String caller = currentPrincipal();
    Instant expiresAt = Instant.now().plus(sessionTtl);
    ShortLivedCredential session =
        sessionRepository.save(
            ShortLivedCredential.create(roleArn, command.sessionName(), caller, expiresAt));
    String accessToken = encodeToken(session, role);
    adminActivityRecorder.recordSuccess("sts:AssumeRole", "Role", role.getId());
    return new AssumeRoleResult(accessToken, session.expiresAt(), session.getId());
  }

  private String encodeToken(ShortLivedCredential session, Role role) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("explore-iam-sts")
            .subject(session.callerPrincipal())
            .issuedAt(now)
            .expiresAt(session.expiresAt())
            .claim("role_arn", session.roleArn().value())
            .claim("session_name", session.sessionName())
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
