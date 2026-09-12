package com.iam.audit.domain.model;

import com.iam.audit.domain.vo.AuditActor;
import com.iam.audit.domain.vo.AuditTarget;
import com.iam.common.domain.base.AbstractAuditEvent;
import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Immutable aggregate recording a management-plane API action. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ManagementEvent extends AbstractAuditEvent {

  private static final String AUTH_LOGIN_ACTION = "auth:login";
  private static final String USER_TARGET_TYPE = "User";

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "actor", nullable = false, length = 256))
  @Valid
  private AuditActor actor;

  @NotBlank
  @Size(max = 128)
  @Column(nullable = false, length = 128)
  private String action;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(
        name = "type",
        column = @Column(name = "target_type", nullable = false, length = 64)),
    @AttributeOverride(
        name = "targetId",
        column = @Column(name = "target_id", nullable = false, length = 256))
  })
  @Valid
  private AuditTarget target;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private AuditOutcome outcome;

  private ManagementEvent(
      String id,
      AuditActor actor,
      String action,
      AuditTarget target,
      AuditOutcome outcome,
      Instant occurredAt) {
    super(id, occurredAt);
    this.actor = Objects.requireNonNull(actor, "actor");
    this.action = DomainStrings.requireNonBlank(action, "action");
    this.target = Objects.requireNonNull(target, "target");
    this.outcome = Objects.requireNonNull(outcome, "outcome");
  }

  /**
   * Records a management-plane action.
   *
   * @param actor principal performing the action
   * @param action action name
   * @param target affected resource
   * @param outcome success or failure
   * @return new aggregate
   */
  public static ManagementEvent logManagementAction(
      AuditActor actor, String action, AuditTarget target, AuditOutcome outcome) {
    return new ManagementEvent(
        UUID.randomUUID().toString(), actor, action, target, outcome, Instant.now());
  }

  /**
   * Records a form-login authentication attempt.
   *
   * @param actor login principal
   * @param outcome success or failure
   * @return new aggregate
   */
  public static ManagementEvent logAuthentication(AuditActor actor, AuditOutcome outcome) {
    return logManagementAction(
        actor, AUTH_LOGIN_ACTION, new AuditTarget(USER_TARGET_TYPE, actor.getValue()), outcome);
  }

  /** Returns true when the management action succeeded. */
  public boolean wasSuccessful() {
    return outcome == AuditOutcome.SUCCESS;
  }

  /** Returns true when the management action failed. */
  public boolean wasFailure() {
    return outcome == AuditOutcome.FAILURE;
  }

  /**
   * Returns true when the given actor performed this action.
   *
   * @param candidate actor to check
   * @return whether this event involves the actor
   */
  public boolean involvesActor(AuditActor candidate) {
    return actor.equals(candidate);
  }

  /**
   * Returns true when this event targets the given resource.
   *
   * @param candidate target to check
   * @return whether this event affects the target
   */
  public boolean targets(AuditTarget candidate) {
    return target.equals(candidate);
  }
}
