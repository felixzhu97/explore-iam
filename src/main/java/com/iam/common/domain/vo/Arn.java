package com.iam.common.domain.vo;

import com.iam.common.domain.base.AbstractEmbeddable;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Amazon Resource Name-style identifier for IAM entities. */
@Embeddable
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Arn extends AbstractEmbeddable {

  private static final Pattern PATTERN =
      Pattern.compile("^arn:iam::[^:]+:(user|role|group|policy)/[a-zA-Z0-9._-]+$");

  @EqualsAndHashCode.Include
  @NotBlank
  @Size(max = 512)
  private String value;

  /**
   * Validates the ARN format.
   *
   * @param value raw ARN
   */
  public Arn(String value) {
    Objects.requireNonNull(value, "value");
    String trimmed = value.trim();
    if (!PATTERN.matcher(trimmed).matches()) {
      throw new IllegalArgumentException("invalid ARN: " + trimmed);
    }
    this.value = trimmed;
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

  /** Returns the ARN string value. */
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
