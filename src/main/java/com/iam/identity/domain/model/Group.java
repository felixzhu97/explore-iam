package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractNamedEntity;
import com.iam.common.domain.base.DomainStrings;
import com.iam.common.domain.vo.Arn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Named collection of IAM users. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Group extends AbstractNamedEntity {

  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<GroupMember> members = new ArrayList<>();

  private Group(String id, String name, Instant createdAt, Instant updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  /**
   * Creates a new IAM group.
   *
   * @param name unique group name
   * @return new aggregate
   */
  public static Group create(String name) {
    Instant now = Instant.now();
    return new Group(UUID.randomUUID().toString(), name, now, now);
  }

  /**
   * Rebuilds a group from persistence.
   *
   * @param id internal id
   * @param name group name
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @return reconstituted aggregate
   */
  public static Group reconstitute(
      String id, String name, Instant createdAt, Instant updatedAt) {
    return new Group(id, name, createdAt, updatedAt);
  }

  /** Returns the ARN for this group. */
  public Arn arn() {
    return new Arn("arn:iam::explore-iam:group/" + getName());
  }

  /**
   * Adds a user to the group when not already a member.
   *
   * @param userId user id
   */
  public void addMember(String userId) {
    String normalized = DomainStrings.requireNonBlank(userId, "userId");
    if (hasMember(normalized)) {
      return;
    }
    members.add(new GroupMember(this, normalized));
    touch();
  }

  /**
   * Removes a user from the group.
   *
   * @param userId user id
   */
  public void removeMember(String userId) {
    if (members.removeIf(member -> member.userId().equals(userId))) {
      touch();
    }
  }

  /**
   * Returns true when the user is a member.
   *
   * @param userId user id
   * @return whether the user belongs to this group
   */
  public boolean hasMember(String userId) {
    return members.stream().anyMatch(member -> member.userId().equals(userId));
  }

  /** Returns member user ids. */
  public List<String> memberUserIds() {
    return Collections.unmodifiableList(members.stream().map(GroupMember::userId).toList());
  }
}
