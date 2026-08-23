package com.iam.audit.domain.vo;

import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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

  @Column(name = "target_type", nullable = false, length = 64)
  private String type;

  @Column(name = "target_id", nullable = false, length = 256)
  private String id;

  /**
   * Creates an audit target.
   *
   * @param type resource kind
   * @param id resource identifier
   */
  public AuditTarget(String type, String id) {
    this.type = DomainStrings.requireNonBlank(type, "target type");
    this.id = DomainStrings.requireNonBlank(id, "target id");
  }

  /**
   * Returns true when this target matches the given type and id.
   *
   * @param type resource kind
   * @param id resource identifier
   * @return whether the target matches
   */
  public boolean matches(String type, String id) {
    return this.type.equals(type) && this.id.equals(id);
  }
}
