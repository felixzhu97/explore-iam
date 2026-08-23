package com.iam.common.domain.vo;

import java.util.Objects;
import java.util.UUID;

/** Stable identifier for an IAM principal (user, role session, etc.). */
public record PrincipalId(String value) {

  /**
   * Validates the principal identifier.
   *
   * @param value raw id
   */
  public PrincipalId {
    Objects.requireNonNull(value, "value");
    if (value.isBlank()) {
      throw new IllegalArgumentException("principal id must not be blank");
    }
    value = value.trim();
  }

  /** Generates a new random principal id. */
  public static PrincipalId generate() {
    return new PrincipalId(UUID.randomUUID().toString());
  }

  @Override
  public String toString() {
    return value;
  }
}
