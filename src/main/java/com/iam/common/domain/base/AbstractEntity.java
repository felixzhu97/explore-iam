package com.iam.common.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Mutable aggregate root base with optimistic locking and last-modified timestamp. */
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public abstract class AbstractEntity extends AbstractImmutable {

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  private long version;

  /** Subclass constructor supplying identity and timestamps. */
  protected AbstractEntity(String id, Instant createdAt, Instant updatedAt) {
    super(id, createdAt);
    this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
  }

  /** Updates the last-modified timestamp after a domain mutation. */
  protected void touch() {
    this.updatedAt = Instant.now();
  }
}
