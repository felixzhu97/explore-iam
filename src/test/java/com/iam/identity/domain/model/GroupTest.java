package com.iam.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupTest {

  @Test
  @DisplayName("should add member idempotently when adding same user twice")
  void shouldAddMemberIdempotentlyWhenAddingSameUserTwice() {
    Group group = Group.create("developers");

    group.addMember("user-1");
    group.addMember("user-1");

    assertThat(group.memberUserIds()).containsExactly("user-1");
  }

  @Test
  @DisplayName("should remove member when removeMember is called")
  void shouldRemoveMemberWhenRemoveMemberIsCalled() {
    Group group = Group.create("developers");
    group.addMember("user-1");

    group.removeMember("user-1");

    assertThat(group.hasMember("user-1")).isFalse();
  }
}
