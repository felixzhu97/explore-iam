package com.iam.common.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Mutable IAM aggregate identified by a unique human-readable name. */
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public abstract class AbstractNamedEntity extends AbstractEntity {

  @Column(nullable = false, unique = true, length = 256)
  private String name;

  /**
   * Subclass constructor supplying identity, name, and timestamps.
   *
   * @param id internal id
   * @param name unique display name
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   */
  protected AbstractNamedEntity(
      String id, String name, Instant createdAt, Instant updatedAt) {
    super(id, createdAt, updatedAt);
    this.name = DomainStrings.requireNonBlank(name, "name");
  }
}
