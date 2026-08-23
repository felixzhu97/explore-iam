package com.iam.common.domain.base;

import jakarta.persistence.MappedSuperclass;

/** Base for embeddable value objects so Hibernate never maps an empty composite as null. */
@MappedSuperclass
public abstract class AbstractEmbeddable {

  @SuppressWarnings("unused")
  private String reserved = "";
}
