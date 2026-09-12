package com.iam.identity.service;

import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.repository.GroupRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** IAM group listing and membership operations. */
@Service
@Transactional(readOnly = true)
public class GroupService {

  private final GroupRepository groupRepository;
  private final ManagementAuditRecorder managementAuditRecorder;

  /**
   * Creates the group service.
   *
   * @param groupRepository group repository
   * @param managementAuditRecorder management audit recorder
   */
  public GroupService(
      GroupRepository groupRepository, ManagementAuditRecorder managementAuditRecorder) {
    this.groupRepository = groupRepository;
    this.managementAuditRecorder = managementAuditRecorder;
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
  public Group create(String name) {
    Group group = groupRepository.save(Group.create(name));
    managementAuditRecorder.recordSuccess("identity:CreateGroup", "Group", group.getId());
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
    group.addMember(userId);
    groupRepository.save(group);
    managementAuditRecorder.recordSuccess("identity:AddGroupMember", "Group", groupId);
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
    group.removeMember(userId);
    groupRepository.save(group);
    managementAuditRecorder.recordSuccess("identity:RemoveGroupMember", "Group", groupId);
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
    managementAuditRecorder.recordSuccess("identity:DeleteGroup", "Group", groupId);
  }
}
