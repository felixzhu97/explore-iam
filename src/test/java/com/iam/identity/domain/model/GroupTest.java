package com.iam.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.identity.domain.event.GroupMembershipChanged;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupTest {

  @Test
  @DisplayName("should add member idempotently when adding same user twice")
  void shouldAddMemberIdempotentlyWhenAddingSameUserTwice() {
    Group group = Group.createGroup("developers");

    group.addMember("user-1");
    group.addMember("user-1");

    assertThat(group.memberUserIds()).containsExactly("user-1");
  }

  @Test
  @DisplayName("should remove member when removeMember is called")
  void shouldRemoveMemberWhenRemoveMemberIsCalled() {
    Group group = Group.createGroup("developers");
    group.addMember("user-1");

    group.removeMember("user-1");

    assertThat(group.hasMember("user-1")).isFalse();
  }

  @Test
  @DisplayName("should replace members and register added and removed ids")
  void shouldReplaceMembersAndRegisterAddedAndRemovedIds() {
    Group group = Group.createGroup("developers");
    group.addMember("user-1");
    group.addMember("user-2");
    group.clearDomainEvents();

    group.replaceMembers(List.of("user-2", "user-3"));

    assertThat(group.memberUserIds()).containsExactlyInAnyOrder("user-2", "user-3");
    assertThat(group.domainEvents())
        .containsExactly(
            new GroupMembershipChanged(group.getId(), Set.of("user-3"), Set.of("user-1")));
  }

  @Test
  @DisplayName("should not register event when replace members keeps same set")
  void shouldNotRegisterEventWhenReplaceMembersKeepsSameSet() {
    Group group = Group.createGroup("developers");
    group.addMember("user-1");
    group.clearDomainEvents();

    group.replaceMembers(List.of("user-1"));

    assertThat(group.domainEvents()).isEmpty();
  }

  @Test
  @DisplayName("should provision and rename group with external id")
  void shouldProvisionAndRenameGroupWithExternalId() {
    Group group = Group.provisionGroup("developers", new ExternalId("okta-g1"));

    group.renameGroup("engineering");

    assertThat(group.getName()).isEqualTo("engineering");
    assertThat(group.getExternalId()).isEqualTo(new ExternalId("okta-g1"));
  }
}
