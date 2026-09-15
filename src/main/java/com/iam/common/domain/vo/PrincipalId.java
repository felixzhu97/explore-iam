package com.iam.common.domain.vo;

import com.iam.common.domain.base.AbstractEmbeddable;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Stable identifier for an IAM principal (user, role session, etc.). */
@Embeddable
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PrincipalId extends AbstractEmbeddable {

  @EqualsAndHashCode.Include
  @NotBlank
  @Size(max = 256)
  private String value;

  /**
   * Validates the principal identifier.
   *
   * @param value raw id
   */
  public PrincipalId(String value) {
    Objects.requireNonNull(value, "value");
    if (value.isBlank()) {
      throw new IllegalArgumentException("principal id must not be blank");
    }
    this.value = value.trim();
  }

  /** Generates a new random principal id. */
  public static PrincipalId generate() {
    return new PrincipalId(UUID.randomUUID().toString());
  }

  /** Returns the principal id string value. */
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
