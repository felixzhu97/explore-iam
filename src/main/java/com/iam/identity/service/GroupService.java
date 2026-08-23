package com.iam.identity.service;

import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.repository.GroupRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("group not found: " + groupId));
    group.addMember(userId);
    groupRepository.save(group);
    managementAuditRecorder.recordSuccess("identity:AddGroupMember", "Group", groupId);
  }
}
