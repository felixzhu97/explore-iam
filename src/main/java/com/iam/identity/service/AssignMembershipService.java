package com.iam.identity.service;

import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.GroupRepository;
import com.iam.identity.domain.repository.IamUserRepository;
import com.iam.identity.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Assigns a role to a user or adds a user to a group. */
@Service
public class AssignMembershipService {

  private final RoleRepository roleRepository;
  private final GroupRepository groupRepository;
  private final IamUserRepository iamUserRepository;
  private final ManagementAuditRecorder managementAuditRecorder;

  /**
   * Creates the use case.
   *
   * @param roleRepository role repository
   * @param groupRepository group repository
   * @param iamUserRepository IAM user repository
   * @param managementAuditRecorder management audit recorder
   */
  public AssignMembershipService(
      RoleRepository roleRepository,
      GroupRepository groupRepository,
      IamUserRepository iamUserRepository,
      ManagementAuditRecorder managementAuditRecorder) {
    this.roleRepository = roleRepository;
    this.groupRepository = groupRepository;
    this.iamUserRepository = iamUserRepository;
    this.managementAuditRecorder = managementAuditRecorder;
  }

  /**
   * Assigns a role to a user.
   *
   * @param userId user id
   * @param roleId role id
   */
  @Transactional
  public void assignRole(String userId, String roleId) {
    roleRepository
        .findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("role not found: " + roleId));
    IamUser user =
        iamUserRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
    user.assignRole(roleId);
    iamUserRepository.save(user);
    managementAuditRecorder.recordSuccess("identity:AssignRole", "User", userId);
  }

  /**
   * Adds a user to a group.
   *
   * @param groupId group id
   * @param userId user id
   */
  @Transactional
  public void addToGroup(String groupId, String userId) {
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new IllegalArgumentException("group not found: " + groupId));
    group.addMember(userId);
    groupRepository.save(group);
    managementAuditRecorder.recordSuccess("identity:AddGroupMember", "Group", groupId);
  }
}
