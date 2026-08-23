package com.iam.common.domain.vo;

import java.util.Objects;

/** Resource ARN or pattern being authorized. */
public record Resource(String value) {

  /**
   * Validates the resource identifier.
   *
   * @param value raw resource
   */
  public Resource {
    Objects.requireNonNull(value, "value");
    if (value.isBlank()) {
      throw new IllegalArgumentException("resource must not be blank");
    }
    value = value.trim();
  }

  /**
   * Returns true when this resource pattern matches the requested resource.
   *
   * @param requested requested resource
   * @return whether the pattern matches
   */
  public boolean matches(Resource requested) {
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
