package com.iam.sts.domain.model;

import com.iam.common.domain.base.AbstractImmutable;
import com.iam.common.domain.base.DomainStrings;
import com.iam.common.domain.model.ResourceName;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Temporary credentials issued by AssumeRole. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ShortLivedCredential extends AbstractImmutable {

  @Getter(AccessLevel.NONE)
  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "role_arn", nullable = false, length = 512))
  @Valid
  private ResourceName roleArn;

  @Getter(AccessLevel.NONE)
  @NotBlank
  @Size(max = 128)
  @Column(nullable = false, length = 128)
  private String sessionName;

  @Getter(AccessLevel.NONE)
  @NotBlank
  @Size(max = 512)
  @Column(nullable = false, length = 512)
  private String callerPrincipal;

  @Getter(AccessLevel.NONE)
  @Column(nullable = false)
  private Instant expiresAt;

  private ShortLivedCredential(
      String id,
      ResourceName roleArn,
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
  public static ShortLivedCredential create(
      ResourceName roleArn, String sessionName, String callerPrincipal, Instant expiresAt) {
    return new ShortLivedCredential(
        UUID.randomUUID().toString(),
        roleArn,
        sessionName,
        callerPrincipal,
        expiresAt,
        Instant.now());
  }

  /** Returns true when the session has expired. */
  public boolean isExpired(Instant now) {
    return !expiresAt.isAfter(now);
  }

  /** Returns the assumed role ARN. */
  public ResourceName roleArn() {
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
