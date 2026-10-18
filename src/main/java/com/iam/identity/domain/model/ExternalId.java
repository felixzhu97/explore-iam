package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractEmbeddable;
import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Identifier assigned to a User or Group by the provisioning client (SCIM externalId). */
@Embeddable
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ExternalId extends AbstractEmbeddable {

  public static final int MAX_LENGTH = 256;

  @EqualsAndHashCode.Include
  @NotBlank
  @Size(max = MAX_LENGTH)
  private String value;

  /**
   * Validates and trims the identifier.
   *
   * @param value raw identifier from the provisioning client
   */
  public ExternalId(String value) {
    String trimmed = DomainStrings.requireNonBlank(value, "externalId").trim();
    if (trimmed.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("externalId exceeds " + MAX_LENGTH + " characters");
    }
    this.value = trimmed;
  }

  /**
   * Returns an External ID or {@code null} for a blank value.
   *
   * @param value raw identifier, may be blank
   * @return value object or {@code null}
   */
  public static ExternalId ofNullable(String value) {
    return value == null || value.isBlank() ? null : new ExternalId(value);
  }

  /** Returns the identifier string. */
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
