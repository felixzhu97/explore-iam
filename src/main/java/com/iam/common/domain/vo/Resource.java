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

/** Resource ARN or pattern being authorized. */
@Embeddable
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Resource extends AbstractEmbeddable {

  @EqualsAndHashCode.Include
  @NotBlank
  @Size(max = 512)
  private String value;

  /**
   * Validates the resource identifier.
   *
   * @param value raw resource
   */
  public Resource(String value) {
    Objects.requireNonNull(value, "value");
    if (value.isBlank()) {
      throw new IllegalArgumentException("resource must not be blank");
    }
    this.value = value.trim();
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

  /** Returns the resource string value. */
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
