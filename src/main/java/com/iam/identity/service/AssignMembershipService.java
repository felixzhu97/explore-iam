package com.iam.identity.service;

import com.iam.identity.domain.repository.GroupRepository;
import com.iam.identity.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Assigns a role to a user or adds a user to a group. */
@Service
public class AssignMembershipService {

  private final RoleRepository roleRepository;
  private final GroupRepository groupRepository;

  /**
   * Creates the use case.
   *
   * @param roleRepository role repository
   * @param groupRepository group repository
   */
  public AssignMembershipService(RoleRepository roleRepository, GroupRepository groupRepository) {
    this.roleRepository = roleRepository;
    this.groupRepository = groupRepository;
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
    roleRepository.assignToUser(userId, roleId);
  }

  /**
   * Adds a user to a group.
   *
   * @param groupId group id
   * @param userId user id
   */
  @Transactional
  public void addToGroup(String groupId, String userId) {
    groupRepository
        .findById(groupId)
        .orElseThrow(() -> new IllegalArgumentException("group not found: " + groupId));
    groupRepository.addMember(groupId, userId);
  }
}
