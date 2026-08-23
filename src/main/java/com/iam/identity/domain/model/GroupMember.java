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

/** Membership linking a user to a group. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class GroupMember {

  @EmbeddedId private Pk id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("groupId")
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  GroupMember(Group group, String userId) {
    this.group = Objects.requireNonNull(group, "group");
    this.id = new Pk(group.getId(), DomainStrings.requireNonBlank(userId, "userId"));
  }

  /** Returns the member user id. */
  public String userId() {
    return id.userId;
  }

  @Embeddable
  @Getter
  @EqualsAndHashCode
  @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  static class Pk implements Serializable {

    private String groupId;
    private String userId;
  }
}
