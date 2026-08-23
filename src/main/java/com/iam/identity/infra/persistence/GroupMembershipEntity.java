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
@Table(name = "iam_group_memberships")
@IdClass(GroupMembershipEntity.Pk.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
class GroupMembershipEntity {

  @Id
  @Column(name = "group_id", length = 36, nullable = false)
  private String groupId;

  @Id
  @Column(name = "user_id", length = 36, nullable = false)
  private String userId;

  GroupMembershipEntity(String groupId, String userId) {
    this.groupId = groupId;
    this.userId = userId;
  }

  @Getter
  @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
  @AllArgsConstructor
  @EqualsAndHashCode
  static class Pk implements Serializable {

    private String groupId;
    private String userId;
  }
}
