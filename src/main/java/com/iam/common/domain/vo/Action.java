package com.iam.common.domain.vo;

import com.iam.common.domain.base.AbstractEmbeddable;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** IAM action being authorized (e.g. {@code s3:GetObject}). */
@Embeddable
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Action extends AbstractEmbeddable {

  @EqualsAndHashCode.Include
  @NotBlank
  @Size(max = 128)
  private String value;

  /**
   * Validates the action string.
   *
   * @param value raw action
   */
  public Action(String value) {
    Objects.requireNonNull(value, "value");
    if (value.isBlank()) {
      throw new IllegalArgumentException("action must not be blank");
    }
    this.value = value.trim();
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

  /** Returns the action string value. */
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
