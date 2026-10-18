package com.iam.identity.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.identity.domain.event.GroupMembershipChangedEvent;
import com.iam.identity.domain.event.UserDeactivatedEvent;
import com.iam.identity.domain.event.UserDeletedEvent;
import com.iam.identity.domain.event.UserProfileChangedEvent;
import com.iam.identity.domain.event.UserProvisionedEvent;
import com.iam.identity.domain.event.UserReactivatedEvent;
import com.iam.identity.domain.model.ExternalId;
import com.iam.identity.domain.model.User;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RecordApplicationEvents
@Transactional
@DisplayName("Identity services publishing domain events")
class IdentityEventPublishingTest {

  @Autowired private UserProvisioningService userProvisioningService;

  @Autowired private GroupProvisioningService groupProvisioningService;

  @Autowired private GroupService groupService;

  @Autowired private ApplicationEvents events;

  @Test
  @DisplayName("should publish provisioned and deactivated when inactive user is provisioned")
  void shouldPublishProvisionedAndDeactivatedWhenInactiveUserIsProvisioned() {
    User user = userProvisioningService.provisionUser(profile(unique("alice"), false));

    assertThat(events.stream(UserProvisionedEvent.class))
        .containsExactly(new UserProvisionedEvent(user.getId()));
    assertThat(events.stream(UserDeactivatedEvent.class))
        .containsExactly(new UserDeactivatedEvent(user.getId()));
  }

  @Test
  @DisplayName("should publish only changed facts when user is replaced")
  void shouldPublishOnlyChangedFactsWhenUserIsReplaced() {
    UserProfile profile = profile(unique("bob"), true);
    User user = userProvisioningService.provisionUser(profile);
    events.clear();

    userProvisioningService.replaceUser(user.getId(), profile, null);
    assertThat(events.stream()).isEmpty();

    userProvisioningService.replaceUser(
        user.getId(),
        new UserProfile(
            profile.username(), "bob@example.com", null, profile.externalId(), false),
        null);
    userProvisioningService.replaceUser(
        user.getId(),
        new UserProfile(
            profile.username(), "bob@example.com", null, profile.externalId(), true),
        null);

    assertThat(events.stream(UserProfileChangedEvent.class))
        .containsExactly(new UserProfileChangedEvent(user.getId()));
    assertThat(events.stream(UserDeactivatedEvent.class)).hasSize(1);
    assertThat(events.stream(UserReactivatedEvent.class)).hasSize(1);
  }

  @Test
  @DisplayName("should publish membership change and user deleted when member is deleted")
  void shouldPublishMembershipChangeAndUserDeletedWhenMemberIsDeleted() {
    User user = userProvisioningService.provisionUser(profile(unique("carol"), true));
    String groupId = groupService.createGroup(unique("team")).getId();
    groupService.addMember(groupId, user.getId());
    groupService.addMember(groupId, user.getId());

    userProvisioningService.deleteUser(user.getId(), null);

    assertThat(events.stream(GroupMembershipChangedEvent.class))
        .containsExactly(
            new GroupMembershipChangedEvent(groupId, Set.of(user.getId()), Set.of()),
            new GroupMembershipChangedEvent(groupId, Set.of(), Set.of(user.getId())));
    assertThat(events.stream(UserDeletedEvent.class))
        .containsExactly(new UserDeletedEvent(user.getId()));
  }

  @Test
  @DisplayName("should publish added and removed ids when group members are replaced")
  void shouldPublishAddedAndRemovedIdsWhenGroupMembersAreReplaced() {
    String first = userProvisioningService.provisionUser(profile(unique("dave"), true)).getId();
    String second = userProvisioningService.provisionUser(profile(unique("erin"), true)).getId();
    String name = unique("group");
    String groupId =
        groupProvisioningService
            .provisionGroup(new GroupProfile(name, null, Set.of(first)))
            .getId();
    events.clear();

    groupProvisioningService.replaceGroup(
        groupId, new GroupProfile(name, null, Set.of(second)), null);

    assertThat(events.stream(GroupMembershipChangedEvent.class))
        .containsExactly(
            new GroupMembershipChangedEvent(groupId, Set.of(second), Set.of(first)));
  }

  private static UserProfile profile(String username, boolean active) {
    return new UserProfile(username, null, "Name", new ExternalId(unique("x")), active);
  }

  private static String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }
}
