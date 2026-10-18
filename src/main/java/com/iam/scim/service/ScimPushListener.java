package com.iam.scim.service;

import com.iam.identity.domain.event.GroupMembershipChanged;
import com.iam.identity.domain.event.UserDeactivated;
import com.iam.identity.domain.event.UserDeleted;
import com.iam.identity.domain.event.UserProfileChanged;
import com.iam.identity.domain.event.UserProvisioned;
import com.iam.identity.domain.event.UserReactivated;
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
  void on(UserProvisioned event) {
    pushService.syncUser(event.userId());
  }

  @ApplicationModuleListener
  void on(UserProfileChanged event) {
    pushService.syncUser(event.userId());
  }

  @ApplicationModuleListener
  void on(UserDeactivated event) {
    pushService.syncUser(event.userId());
  }

  @ApplicationModuleListener
  void on(UserReactivated event) {
    pushService.syncUser(event.userId());
  }

  @ApplicationModuleListener
  void on(UserDeleted event) {
    pushService.deleteUser(event.userId());
  }

  @ApplicationModuleListener
  void on(GroupMembershipChanged event) {
    Set<String> userIds = new LinkedHashSet<>(event.addedUserIds());
    userIds.addAll(event.removedUserIds());
    pushService.syncAssignment(event.groupId(), userIds);
  }
}
