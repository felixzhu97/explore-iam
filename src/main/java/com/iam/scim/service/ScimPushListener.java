package com.iam.scim.service;

import com.iam.identity.domain.event.GroupMembershipChangedEvent;
import com.iam.identity.domain.event.UserDeactivatedEvent;
import com.iam.identity.domain.event.UserDeletedEvent;
import com.iam.identity.domain.event.UserProfileChangedEvent;
import com.iam.identity.domain.event.UserProvisionedEvent;
import com.iam.identity.domain.event.UserReactivatedEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Turns identity Domain Events into SCIM pushes. Each listener runs after commit in its own
 * transaction; the Event Publication Registry keeps failed pushes for resubmission.
 */
@Component
public class ScimPushListener {

  private final ScimPushService pushService;

  /**
   * Creates the listener.
   *
   * @param pushService push orchestration
   */
  public ScimPushListener(ScimPushService pushService) {
    this.pushService = pushService;
  }

  @ApplicationModuleListener
  void handleUserProvisionedEvent(UserProvisionedEvent event) {
    pushService.syncUser(event.userId());
  }

  @ApplicationModuleListener
  void handleUserProfileChangedEvent(UserProfileChangedEvent event) {
    pushService.syncUser(event.userId());
  }

  @ApplicationModuleListener
  void handleUserDeactivatedEvent(UserDeactivatedEvent event) {
    pushService.syncUser(event.userId());
  }

  @ApplicationModuleListener
  void handleUserReactivatedEvent(UserReactivatedEvent event) {
    pushService.syncUser(event.userId());
  }

  @ApplicationModuleListener
  void handleUserDeletedEvent(UserDeletedEvent event) {
    pushService.deleteUser(event.userId());
  }

  @ApplicationModuleListener
  void handleGroupMembershipChangedEvent(GroupMembershipChangedEvent event) {
    Set<String> userIds = new LinkedHashSet<>(event.addedUserIds());
    userIds.addAll(event.removedUserIds());
    pushService.syncAssignment(event.groupId(), userIds);
  }
}
