package com.iam.common.domain.base;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;

/**
 * Mutable aggregate root that registers domain events, published by Spring Data when the
 * repository saves or deletes the aggregate.
 */
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public abstract class AbstractEventEntity extends AbstractEntity {

  @Transient
  @Getter(AccessLevel.NONE)
  private final List<Object> domainEvents = new ArrayList<>();

  /** Subclass constructor supplying identity and timestamps. */
  protected AbstractEventEntity(String id, Instant createdAt, Instant updatedAt) {
    super(id, createdAt, updatedAt);
  }

  /**
   * Registers a domain event published on the next repository save or delete.
   *
   * @param event domain event carrying ids only
   */
  protected void registerEvent(Object event) {
    domainEvents.add(Objects.requireNonNull(event, "event"));
  }

  /** Returns events registered since the last repository save or delete. */
  @DomainEvents
  public List<Object> domainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  /** Clears events after Spring Data has published them. */
  @AfterDomainEventPublication
  public void clearDomainEvents() {
    domainEvents.clear();
  }
}
