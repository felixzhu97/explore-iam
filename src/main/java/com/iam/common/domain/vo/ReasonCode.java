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

/** Machine-readable reason for an authorization decision. */
@Embeddable
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ReasonCode extends AbstractEmbeddable {

  public static final ReasonCode EXPLICIT_DENY = new ReasonCode("EXPLICIT_DENY");
  public static final ReasonCode EXPLICIT_ALLOW = new ReasonCode("EXPLICIT_ALLOW");
  public static final ReasonCode IMPLICIT_DENY = new ReasonCode("IMPLICIT_DENY");

  @EqualsAndHashCode.Include
  @NotBlank
  @Size(max = 64)
  private String value;

  /**
   * Validates the reason code.
   *
   * @param value raw code
   */
  public ReasonCode(String value) {
    Objects.requireNonNull(value, "value");
    if (value.isBlank()) {
      throw new IllegalArgumentException("reason code must not be blank");
    }
    this.value = value.trim().toUpperCase();
  }

  /** Returns the reason code string value. */
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
