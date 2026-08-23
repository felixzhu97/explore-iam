package com.iam.common.domain.base;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Immutable audit record with {@code occurred_at} persisted instead of {@code created_at}. */
@MappedSuperclass
@AttributeOverride(
    name = "createdAt",
    column = @Column(name = "occurred_at", nullable = false, updatable = false))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public abstract class AbstractAuditEvent extends AbstractImmutable {

  /**
   * Subclass constructor supplying identity and occurrence timestamp.
   *
   * @param id event id
   * @param occurredAt when the event occurred
   */
  protected AbstractAuditEvent(String id, Instant occurredAt) {
    super(id, occurredAt);
  }

  /** Returns when the audit event occurred (alias for {@link #getCreatedAt()}). */
  public Instant getOccurredAt() {
    return getCreatedAt();
  }
}
