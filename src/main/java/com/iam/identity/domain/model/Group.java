package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractEventEntity;
import com.iam.common.domain.base.DomainStrings;
import com.iam.common.domain.model.ResourceName;
import com.iam.identity.domain.event.GroupMembershipChanged;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Named collection of IAM users.
 *
 * <p>Table name is explicit because {@code group} is a reserved SQL word.
 */
@Entity
@Table(name = "directory_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Group extends AbstractEventEntity {

  @NotBlank
  @Size(max = 256)
  @Column(nullable = false, unique = true, length = 256)
  private String name;

  @Getter(AccessLevel.NONE)
  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<GroupMember> members = new ArrayList<>();

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "external_id", length = 256))
  @Valid
  private ExternalId externalId;

  private Group(String id, String name, Instant createdAt, Instant updatedAt) {
    super(id, createdAt, updatedAt);
    this.name = DomainStrings.requireNonBlank(name, "name");
  }

  /**
   * Creates a new IAM group.
   *
   * @param name unique group name
   * @return new aggregate
   */
  public static Group createGroup(String name) {
    Instant now = Instant.now();
    return new Group(UUID.randomUUID().toString(), name, now, now);
  }

  /**
   * Creates a group pushed by a SCIM Client.
   *
   * @param name unique group name
   * @param externalId optional identifier from the provisioning client
   * @return new aggregate
   */
  public static Group provisionGroup(String name, ExternalId externalId) {
    Group group = createGroup(name);
    group.externalId = externalId;
    return group;
  }

  /** Returns the External ID, or {@code null} when none is set. */
  public ExternalId getExternalId() {
    return externalId == null || externalId.value() == null ? null : externalId;
  }

  /**
   * Renames the group.
   *
   * @param name new unique name
   */
  public void renameGroup(String name) {
    String normalized = DomainStrings.requireNonBlank(name, "name");
    if (normalized.equals(this.name)) {
      return;
    }
    this.name = normalized;
    touch();
  }

  /**
   * Sets or clears the External ID.
   *
   * @param externalId new identifier, or {@code null} to clear
   */
  public void changeExternalId(ExternalId externalId) {
    if (Objects.equals(externalId, getExternalId())) {
      return;
    }
    this.externalId = externalId;
    touch();
  }

  /** Returns the resource name for this group. */
  public ResourceName getResourceName() {
    return new ResourceName("arn:iam::explore-iam:group/" + getName());
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
    registerEvent(new GroupMembershipChanged(getId(), Set.of(normalized), Set.of()));
    touch();
  }

  /**
   * Removes a user from the group.
   *
   * @param userId user id
   */
  public void removeMember(String userId) {
    if (members.removeIf(member -> member.userId().equals(userId))) {
      registerEvent(new GroupMembershipChanged(getId(), Set.of(), Set.of(userId)));
      touch();
    }
  }

  /**
   * Replaces the full member list.
   *
   * @param userIds desired member user ids
   */
  public void replaceMembers(Collection<String> userIds) {
    Set<String> desired = new LinkedHashSet<>();
    for (String userId : userIds) {
      desired.add(DomainStrings.requireNonBlank(userId, "userId"));
    }
    Set<String> removed = new LinkedHashSet<>(memberUserIds());
    removed.removeAll(desired);
    Set<String> added = new LinkedHashSet<>(desired);
    added.removeAll(memberUserIds());
    if (added.isEmpty() && removed.isEmpty()) {
      return;
    }
    members.removeIf(member -> removed.contains(member.userId()));
    added.forEach(userId -> members.add(new GroupMember(this, userId)));
    registerEvent(new GroupMembershipChanged(getId(), added, removed));
    touch();
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
