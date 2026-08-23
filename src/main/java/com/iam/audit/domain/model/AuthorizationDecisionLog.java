package com.iam.audit.domain.model;

import com.iam.audit.domain.converter.ReasonCodeConverter;
import com.iam.common.domain.base.AbstractAuditEvent;
import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.ReasonCode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Immutable record of a policy authorization decision. */
@Entity
@Table(name = "iam_authorization_decision_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuthorizationDecisionLog extends AbstractAuditEvent {

  @Column(name = "principal_id", nullable = false, length = 256)
  private String principalId;

  @Column(nullable = false, length = 128)
  private String action;

  @Column(nullable = false, length = 512)
  private String resource;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 8)
  private Effect effect;

  @Column(name = "reason_code", nullable = false, length = 64)
  @Convert(converter = ReasonCodeConverter.class)
  private ReasonCode reasonCode;

  private AuthorizationDecisionLog(
      String id,
      String principalId,
      String action,
      String resource,
      Effect effect,
      ReasonCode reasonCode,
      Instant occurredAt) {
    super(id, occurredAt);
    this.principalId = principalId;
    this.action = action;
    this.resource = resource;
    this.effect = effect;
    this.reasonCode = reasonCode;
  }

  /** Records a new authorization decision. */
  public static AuthorizationDecisionLog record(
      String principalId,
      String action,
      String resource,
      Effect effect,
      ReasonCode reasonCode) {
    return new AuthorizationDecisionLog(
        UUID.randomUUID().toString(),
        principalId,
        action,
        resource,
        effect,
        reasonCode,
        Instant.now());
  }

  /** Rebuilds from persistence. */
  public static AuthorizationDecisionLog reconstitute(
      String id,
      String principalId,
      String action,
      String resource,
      Effect effect,
      ReasonCode reasonCode,
      Instant occurredAt) {
    return new AuthorizationDecisionLog(
        id, principalId, action, resource, effect, reasonCode, occurredAt);
  }
}
