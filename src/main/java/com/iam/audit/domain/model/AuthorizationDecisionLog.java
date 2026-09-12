package com.iam.audit.domain.model;

import com.iam.common.domain.base.AbstractAuditEvent;
import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.PrincipalId;
import com.iam.common.domain.vo.ReasonCode;
import com.iam.common.domain.vo.Resource;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Immutable aggregate recording a policy authorization decision. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuthorizationDecisionLog extends AbstractAuditEvent {

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "principal_id", nullable = false, length = 256))
  @Valid
  private PrincipalId principalId;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "action", nullable = false, length = 128))
  @Valid
  private Action action;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "resource", nullable = false, length = 512))
  @Valid
  private Resource resource;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 8)
  private Effect effect;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "reason_code", nullable = false, length = 64))
  @Valid
  private ReasonCode reasonCode;

  private AuthorizationDecisionLog(
      PrincipalId principalId,
      Action action,
      Resource resource,
      Effect effect,
      ReasonCode reasonCode,
      String id,
      Instant occurredAt) {
    super(id, occurredAt);
    this.principalId = Objects.requireNonNull(principalId, "principalId");
    this.action = Objects.requireNonNull(action, "action");
    this.resource = Objects.requireNonNull(resource, "resource");
    this.effect = Objects.requireNonNull(effect, "effect");
    this.reasonCode = Objects.requireNonNull(reasonCode, "reasonCode");
  }

  /** Captures an authorization decision for persistence. */
  public static AuthorizationDecisionLog capture(
      PrincipalId principalId,
      Action action,
      Resource resource,
      Effect effect,
      ReasonCode reasonCode) {
    return new AuthorizationDecisionLog(
        principalId,
        action,
        resource,
        effect,
        reasonCode,
        UUID.randomUUID().toString(),
        Instant.now());
  }

  /**
   * Captures a policy evaluation outcome for persistence.
   *
   * @param principalId evaluated principal
   * @param action requested action
   * @param resource requested resource
   * @param effect decision effect
   * @param reasonCode machine-readable reason
   * @return new aggregate
   */
  public static AuthorizationDecisionLog fromEvaluation(
      PrincipalId principalId,
      Action action,
      Resource resource,
      Effect effect,
      ReasonCode reasonCode) {
    return capture(principalId, action, resource, effect, reasonCode);
  }

  public boolean isAllowed() {
    return effect == Effect.ALLOW;
  }

  public boolean isDenied() {
    return effect == Effect.DENY;
  }

  /** Returns true when an explicit deny statement matched. */
  public boolean wasExplicitDeny() {
    return ReasonCode.EXPLICIT_DENY.equals(reasonCode);
  }

  /** Returns true when no allow matched (implicit deny). */
  public boolean wasImplicitDeny() {
    return ReasonCode.IMPLICIT_DENY.equals(reasonCode);
  }
}
