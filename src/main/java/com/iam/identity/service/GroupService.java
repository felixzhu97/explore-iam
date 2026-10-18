package com.iam.identity.service;

import com.iam.identity.domain.event.GroupCreatedEvent;
import com.iam.identity.domain.event.GroupDeletedEvent;
import com.iam.identity.domain.event.GroupMembershipChangedEvent;
import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.repository.GroupRepository;
import java.util.List;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** IAM group listing and membership operations. */
@Service
@Transactional(readOnly = true)
public class GroupService {

  private final GroupRepository groupRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Creates the group service.
   *
   * @param groupRepository group repository
   * @param eventPublisher domain event publisher
   */
  public GroupService(GroupRepository groupRepository, ApplicationEventPublisher eventPublisher) {
    this.groupRepository = groupRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Returns all IAM groups.
   *
   * @return group list
   */
  public List<Group> findAll() {
    return groupRepository.findAll();
  }

  /**
   * Returns one group.
   *
   * @param groupId group id
   * @return group
   */
  public Group get(String groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found"));
  }

  /**
   * Creates a group with the given name.
   *
   * @param name unique group name
   * @return persisted group
   */
  @Transactional
  public Group createGroup(String name) {
    Group group = groupRepository.save(Group.createGroup(name));
    eventPublisher.publishEvent(new GroupCreatedEvent(group.getId()));
    return group;
  }

  /**
   * Adds a user to a group.
   *
   * @param groupId group id
   * @param userId user id
   */
  @Transactional
  public void addMember(String groupId, String userId) {
    Group group = get(groupId);
    if (group.addMember(userId)) {
      groupRepository.save(group);
      eventPublisher.publishEvent(
          new GroupMembershipChangedEvent(groupId, Set.of(userId), Set.of()));
    }
  }

  /**
   * Removes a user from a group.
   *
   * @param groupId group id
   * @param userId user id
   */
  @Transactional
  public void removeMember(String groupId, String userId) {
    Group group = get(groupId);
    if (group.removeMember(userId)) {
      groupRepository.save(group);
      eventPublisher.publishEvent(
          new GroupMembershipChangedEvent(groupId, Set.of(), Set.of(userId)));
    }
  }

  /**
   * Deletes a group when it has no members.
   *
   * @param groupId group id
   */
  @Transactional
  public void delete(String groupId) {
    Group group = get(groupId);
    if (!group.memberUserIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, "group still has members");
    }
    groupRepository.delete(group);
    eventPublisher.publishEvent(new GroupDeletedEvent(groupId));
  }
}
