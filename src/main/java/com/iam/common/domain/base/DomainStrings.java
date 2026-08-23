package com.iam.common.domain.base;

/** Shared string validation for IAM domain types. */
public final class DomainStrings {

  private DomainStrings() {}

  /**
   * Returns a trimmed non-blank string.
   *
   * @param value raw value
   * @param fieldName field label for error messages
   * @return trimmed value
   */
  public static String requireNonBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value.trim();
  }
}
