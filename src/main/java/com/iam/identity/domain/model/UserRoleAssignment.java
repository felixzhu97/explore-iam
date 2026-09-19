package com.iam.identity.domain.model;

import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Assignment linking a role to a user. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class UserRoleAssignment {

  @EmbeddedId private Pk id;

  @Getter(AccessLevel.PACKAGE)
  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("userId")
  @JoinColumn(nullable = false)
  private IamUser user;

  UserRoleAssignment(IamUser user, String roleId) {
    this.user = Objects.requireNonNull(user, "user");
    this.id = new Pk(user.getId(), DomainStrings.requireNonBlank(roleId, "roleId"));
  }

  /** Returns the assigned role id. */
  public String roleId() {
    return id.roleId;
  }

  @Embeddable
  @Getter
  @EqualsAndHashCode
  @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static class Pk implements Serializable {

    private String userId;
    private String roleId;
  }
}
