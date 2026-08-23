package com.iam.audit.domain.vo;

import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Principal that performed an audited management action. */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuditActor {

  @Column(nullable = false, length = 256)
  private String value;

  /**
   * Creates an audit actor.
   *
   * @param value principal identifier
   */
  public AuditActor(String value) {
    this.value = DomainStrings.requireNonBlank(value, "actor");
  }
}
