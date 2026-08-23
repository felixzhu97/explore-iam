package com.iam.common.domain.vo;

import java.util.Objects;

/** Machine-readable reason for an authorization decision. */
public record ReasonCode(String value) {

  public static final ReasonCode EXPLICIT_DENY = new ReasonCode("EXPLICIT_DENY");
  public static final ReasonCode EXPLICIT_ALLOW = new ReasonCode("EXPLICIT_ALLOW");
  public static final ReasonCode IMPLICIT_DENY = new ReasonCode("IMPLICIT_DENY");

  /**
   * Validates the reason code.
   *
   * @param value raw code
   */
  public ReasonCode {
    Objects.requireNonNull(value, "value");
    if (value.isBlank()) {
      throw new IllegalArgumentException("reason code must not be blank");
    }
    value = value.trim().toUpperCase();
  }

  @Override
  public String toString() {
    return value;
  }
}
