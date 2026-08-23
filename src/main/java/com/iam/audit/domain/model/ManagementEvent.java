package com.iam.audit.domain.model;

import com.iam.common.domain.base.AbstractAuditEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Immutable record of a management-plane API action. */
@Entity
@Table(name = "iam_management_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ManagementEvent extends AbstractAuditEvent {

  @Column(nullable = false, length = 256)
  private String actor;

  @Column(nullable = false, length = 128)
  private String action;

  @Column(name = "target_type", nullable = false, length = 64)
  private String targetType;

  @Column(name = "target_id", nullable = false, length = 256)
  private String targetId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private AuditOutcome outcome;

  private ManagementEvent(
      String id,
      String actor,
      String action,
      String targetType,
      String targetId,
      AuditOutcome outcome,
      Instant occurredAt) {
    super(id, occurredAt);
    this.actor = actor;
    this.action = action;
    this.targetType = targetType;
    this.targetId = targetId;
    this.outcome = outcome;
  }

  /** Records a new management event. */
  public static ManagementEvent record(
      String actor,
      String action,
      String targetType,
      String targetId,
      AuditOutcome outcome) {
    return new ManagementEvent(
        UUID.randomUUID().toString(),
        actor,
        action,
        targetType,
        targetId,
        outcome,
        Instant.now());
  }

  /** Rebuilds from persistence. */
  public static ManagementEvent reconstitute(
      String id,
      String actor,
      String action,
      String targetType,
      String targetId,
      AuditOutcome outcome,
      Instant occurredAt) {
    return new ManagementEvent(
        id, actor, action, targetType, targetId, outcome, occurredAt);
  }
}
