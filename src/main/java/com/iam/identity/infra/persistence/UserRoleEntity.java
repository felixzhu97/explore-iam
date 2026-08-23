package com.iam.identity.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "iam_user_roles")
@IdClass(UserRoleEntity.Pk.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
class UserRoleEntity {

  @Id
  @Column(name = "user_id", length = 36, nullable = false)
  private String userId;

  @Id
  @Column(name = "role_id", length = 36, nullable = false)
  private String roleId;

  UserRoleEntity(String userId, String roleId) {
    this.userId = userId;
    this.roleId = roleId;
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
  @AllArgsConstructor
  @EqualsAndHashCode
  static class Pk implements Serializable {

    private String userId;
    private String roleId;
  }
}
