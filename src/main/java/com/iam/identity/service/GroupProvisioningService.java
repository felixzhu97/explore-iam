package com.iam.identity.service;

import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.repository.GroupRepository;
import com.iam.identity.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Creates, replaces, and deletes groups on behalf of a provisioning client. */
@Service
@Transactional(readOnly = true)
public class GroupProvisioningService {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  /**
   * Creates the group provisioning service.
   *
   * @param groupRepository IAM group repository
   * @param userRepository IAM user repository
   */
  public GroupProvisioningService(GroupRepository groupRepository, UserRepository userRepository) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  /**
   * Returns one group by id with members loaded.
   *
   * @param groupId IAM group id
   * @return group
   */
  public Group get(String groupId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found"));
    group.memberUserIds();
    return group;
  }

  /**
   * Finds a group by name with members loaded.
   *
   * @param name group name
   * @return matching group when present
   */
  public Optional<Group> findByName(String name) {
    return groupRepository.findByName(name).map(GroupProvisioningService::loadMembers);
  }

  /**
   * Finds a group by External ID with members loaded.
   *
   * @param externalId identifier from the provisioning client
   * @return matching group when present
   */
  public Optional<Group> findByExternalId(String externalId) {
    return groupRepository.findByExternalId(externalId).map(GroupProvisioningService::loadMembers);
  }

  /**
   * Lists all groups with members loaded.
   *
   * @return groups
   */
  public List<Group> findAll() {
    List<Group> groups = groupRepository.findAll();
    groups.forEach(GroupProvisioningService::loadMembers);
    return groups;
  }

  /**
   * Creates a group with its members.
   *
   * @param profile desired state
   * @return stored group
   */
  @Transactional
  public Group provisionGroup(GroupProfile profile) {
    requireNameAvailable(profile.name(), null);
    requireUsersExist(profile);
    Group group = Group.provisionGroup(profile.name(), profile.externalId());
    group.replaceMembers(profile.memberUserIds());
    return groupRepository.save(group);
  }

  /**
   * Replaces a group's name, External ID, and members.
   *
   * @param groupId IAM group id
   * @param profile desired state
   * @param expectedVersion version from {@code If-Match}, or {@code null} to skip the check
   * @return stored group
   */
  @Transactional
  public Group replaceGroup(String groupId, GroupProfile profile, Long expectedVersion) {
    Group group = get(groupId);
    UserProvisioningService.requireVersion(group.getVersion(), expectedVersion);
    requireNameAvailable(profile.name(), groupId);
    requireUsersExist(profile);
    group.renameGroup(profile.name());
    group.changeExternalId(profile.externalId());
    group.replaceMembers(profile.memberUserIds());
    return groupRepository.save(group);
  }

  /**
   * Deletes a group and its memberships.
   *
   * @param groupId IAM group id
   * @param expectedVersion version from {@code If-Match}, or {@code null} to skip the check
   */
  @Transactional
  public void deleteGroup(String groupId, Long expectedVersion) {
    Group group = get(groupId);
    UserProvisioningService.requireVersion(group.getVersion(), expectedVersion);
    group.replaceMembers(List.of());
    groupRepository.delete(group);
  }

  private void requireNameAvailable(String name, String currentGroupId) {
    groupRepository
        .findByName(name)
        .filter(existing -> !existing.getId().equals(currentGroupId))
        .ifPresent(
            existing -> {
              throw new ResponseStatusException(HttpStatus.CONFLICT, "group name exists");
            });
  }

  private void requireUsersExist(GroupProfile profile) {
    for (String userId : profile.memberUserIds()) {
      if (userRepository.findById(userId).isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown member " + userId);
      }
    }
  }

  private static Group loadMembers(Group group) {
    group.memberUserIds();
    return group;
  }
}
