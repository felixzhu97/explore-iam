package com.iam.sts.domain.model;

import com.iam.common.domain.base.AbstractImmutable;
import com.iam.common.domain.base.DomainStrings;
import com.iam.common.domain.converter.ArnAttributeConverter;
import com.iam.common.domain.vo.Arn;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Temporary credentials issued by AssumeRole. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AssumedRoleSession extends AbstractImmutable {

  @Getter(AccessLevel.NONE)
  @Column(name = "role_arn", nullable = false, length = 512)
  @Convert(converter = ArnAttributeConverter.class)
  private Arn roleArn;

  @Getter(AccessLevel.NONE)
  @Column(name = "session_name", nullable = false, length = 128)
  private String sessionName;

  @Getter(AccessLevel.NONE)
  @Column(name = "caller_principal", nullable = false, length = 512)
  private String callerPrincipal;

  @Getter(AccessLevel.NONE)
  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  private AssumedRoleSession(
      String id,
      Arn roleArn,
      String sessionName,
      String callerPrincipal,
      Instant expiresAt,
      Instant createdAt) {
    super(id, createdAt);
    this.roleArn = roleArn;
    this.sessionName = requireSessionName(sessionName);
    this.callerPrincipal = callerPrincipal;
    this.expiresAt = expiresAt;
  }

  /** Creates a new assumed-role session. */
  public static AssumedRoleSession create(
      Arn roleArn, String sessionName, String callerPrincipal, Instant expiresAt) {
    return new AssumedRoleSession(
        UUID.randomUUID().toString(),
        roleArn,
        sessionName,
        callerPrincipal,
        expiresAt,
        Instant.now());
  }

  /** Rebuilds from persistence. */
  public static AssumedRoleSession reconstitute(
      String id,
      Arn roleArn,
      String sessionName,
      String callerPrincipal,
      Instant expiresAt,
      Instant createdAt) {
    return new AssumedRoleSession(
        id, roleArn, sessionName, callerPrincipal, expiresAt, createdAt);
  }

  /** Returns true when the session has expired. */
  public boolean isExpired(Instant now) {
    return !expiresAt.isAfter(now);
  }

  /** Returns the assumed role ARN. */
  public Arn roleArn() {
    return roleArn;
  }

  /** Returns the session name supplied by the caller. */
  public String sessionName() {
    return sessionName;
  }

  /** Returns the principal that initiated AssumeRole. */
  public String callerPrincipal() {
    return callerPrincipal;
  }

  /** Returns when temporary credentials expire. */
  public Instant expiresAt() {
    return expiresAt;
  }

  private static String requireSessionName(String sessionName) {
    return DomainStrings.requireNonBlank(sessionName, "session name");
  }
}
