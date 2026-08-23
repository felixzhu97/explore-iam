package com.iam.common.domain.vo;

/** Policy evaluation effect (Allow or Deny). */
public enum Effect {
  ALLOW,
  DENY;

  /**
   * Parses an effect from its string representation.
   *
   * @param raw effect name
   * @return matching effect
   */
  public static Effect fromString(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("effect must not be blank");
    }
    return Effect.valueOf(raw.trim().toUpperCase());
  }
}
