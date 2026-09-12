package com.iam.audit.domain.vo;

import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Resource affected by a management-plane audit action. */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AuditTarget {

  @NotBlank
  @Size(max = 64)
  @Column(nullable = false, length = 64)
  private String type;

  @NotBlank
  @Size(max = 256)
  @Column(nullable = false, length = 256)
  private String targetId;

  /**
   * Creates an audit target.
   *
   * @param type resource kind
   * @param targetId resource identifier
   */
  public AuditTarget(String type, String targetId) {
    this.type = DomainStrings.requireNonBlank(type, "target type");
    this.targetId = DomainStrings.requireNonBlank(targetId, "target id");
  }

  /**
   * Returns true when this target matches the given type and id.
   *
   * @param type resource kind
   * @param targetId resource identifier
   * @return whether the target matches
   */
  public boolean matches(String type, String targetId) {
    return this.type.equals(type) && this.targetId.equals(targetId);
  }
}
