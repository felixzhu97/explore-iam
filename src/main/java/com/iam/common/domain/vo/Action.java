package com.iam.common.domain.vo;

import java.util.Objects;

/** IAM action being authorized (e.g. {@code s3:GetObject}). */
public record Action(String value) {

  /**
   * Validates the action string.
   *
   * @param value raw action
   */
  public Action {
    Objects.requireNonNull(value, "value");
    if (value.isBlank()) {
      throw new IllegalArgumentException("action must not be blank");
    }
    value = value.trim();
  }

  /**
   * Returns true when this action matches the requested action (exact or wildcard).
   *
   * @param requested requested action
   * @return whether the pattern matches
   */
  public boolean matches(Action requested) {
    if ("*".equals(this.value) || "*".equals(requested.value)) {
      return true;
    }
    return this.value.equals(requested.value);
  }

  @Override
  public String toString() {
    return value;
  }
}
