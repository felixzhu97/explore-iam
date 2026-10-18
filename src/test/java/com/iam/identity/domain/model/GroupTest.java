package com.iam.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupTest {

  @Test
  @DisplayName("should add member idempotently when adding same user twice")
  void shouldAddMemberIdempotentlyWhenAddingSameUserTwice() {
    Group group = Group.createGroup("developers");

    assertThat(group.addMember("user-1")).isTrue();
    assertThat(group.addMember("user-1")).isFalse();

    assertThat(group.memberUserIds()).containsExactly("user-1");
  }

  @Test
  @DisplayName("should remove member when removeMember is called")
  void shouldRemoveMemberWhenRemoveMemberIsCalled() {
    Group group = Group.createGroup("developers");
    group.addMember("user-1");

    assertThat(group.removeMember("user-1")).isTrue();

    assertThat(group.hasMember("user-1")).isFalse();
    assertThat(group.removeMember("user-1")).isFalse();
  }

  @Test
  @DisplayName("should replace members and return added and removed ids")
  void shouldReplaceMembersAndReturnAddedAndRemovedIds() {
    Group group = Group.createGroup("developers");
    group.addMember("user-1");
    group.addMember("user-2");

    MembershipChange change = group.replaceMembers(List.of("user-2", "user-3"));

    assertThat(group.memberUserIds()).containsExactlyInAnyOrder("user-2", "user-3");
    assertThat(change).isEqualTo(new MembershipChange(Set.of("user-3"), Set.of("user-1")));
  }

  @Test
  @DisplayName("should return empty change when replace members keeps same set")
  void shouldReturnEmptyChangeWhenReplaceMembersKeepsSameSet() {
    Group group = Group.createGroup("developers");
    group.addMember("user-1");

    assertThat(group.replaceMembers(List.of("user-1")).isEmpty()).isTrue();
  }

  @Test
  @DisplayName("should provision and rename group with external id")
  void shouldProvisionAndRenameGroupWithExternalId() {
    Group group = Group.provisionGroup("developers", new ExternalId("okta-g1"));

    assertThat(group.renameGroup("engineering")).isTrue();
    assertThat(group.renameGroup("engineering")).isFalse();

    assertThat(group.getName()).isEqualTo("engineering");
    assertThat(group.getExternalId()).isEqualTo(new ExternalId("okta-g1"));
  }
}
