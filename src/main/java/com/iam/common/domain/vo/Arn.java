package com.iam.common.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/** Amazon Resource Name-style identifier for IAM entities. */
public record Arn(String value) {

  private static final Pattern PATTERN =
      Pattern.compile("^arn:iam::[^:]+:(user|role|group|policy)/[a-zA-Z0-9._-]+$");

  /**
   * Validates the ARN format.
   *
   * @param value raw ARN
   */
  public Arn {
    Objects.requireNonNull(value, "value");
    String trimmed = value.trim();
    if (!PATTERN.matcher(trimmed).matches()) {
      throw new IllegalArgumentException("invalid ARN: " + trimmed);
    }
    value = trimmed;
  }

  /**
   * Builds a user ARN for the Explore IAM namespace.
   *
   * @param username login name
   * @return user ARN
   */
  public static Arn user(String username) {
    return new Arn("arn:iam::explore-iam:user/" + username);
  }

  /**
   * Builds a role ARN for the Explore IAM namespace.
   *
   * @param roleName role name slug
   * @return role ARN
   */
  public static Arn role(String roleName) {
    return new Arn("arn:iam::explore-iam:role/" + roleName);
  }

  @Override
  public String toString() {
    return value;
  }
}
