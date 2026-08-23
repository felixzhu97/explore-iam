package com.iam.common.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Immutable aggregate root base with identity and creation timestamp. */
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public abstract class AbstractImmutable {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /** Subclass constructor supplying identity and timestamps. */
  protected AbstractImmutable(String id, Instant createdAt) {
    this.id = Objects.requireNonNull(id, "id");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
  }
}
