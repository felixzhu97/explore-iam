package com.iam.identity.controller;

import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.repository.GroupRepository;
import com.iam.identity.domain.repository.RoleRepository;
import com.iam.identity.service.AssignMembershipService;
import com.iam.identity.service.CreateGroupService;
import com.iam.identity.service.CreateRoleService;
import com.iam.identity.service.DisableUserService;
import com.iam.identity.service.ListUsersService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Identity management API for users, groups, and roles. */
@RestController
@RequestMapping("/api/identity")
public class IdentityController {

  private final ListUsersService listUsersUseCase;
  private final DisableUserService disableUserUseCase;
  private final CreateGroupService createGroupUseCase;
  private final CreateRoleService createRoleUseCase;
  private final AssignMembershipService assignMembershipUseCase;
  private final GroupRepository groupRepository;
  private final RoleRepository roleRepository;

  /**
   * Creates the identity API controller.
   *
   * @param listUsersUseCase user listing use case
   * @param disableUserUseCase user disable use case
   * @param createGroupUseCase group creation use case
   * @param createRoleUseCase role creation use case
   * @param assignMembershipUseCase membership assignment use case
   * @param groupRepository group repository
   * @param roleRepository role repository
   */
  public IdentityController(
      ListUsersService listUsersUseCase,
      DisableUserService disableUserUseCase,
      CreateGroupService createGroupUseCase,
      CreateRoleService createRoleUseCase,
      AssignMembershipService assignMembershipUseCase,
      GroupRepository groupRepository,
      RoleRepository roleRepository) {
    this.listUsersUseCase = listUsersUseCase;
    this.disableUserUseCase = disableUserUseCase;
    this.createGroupUseCase = createGroupUseCase;
    this.createRoleUseCase = createRoleUseCase;
    this.assignMembershipUseCase = assignMembershipUseCase;
    this.groupRepository = groupRepository;
    this.roleRepository = roleRepository;
  }

  /**
   * Lists all IAM users.
   *
   * @return user summaries
   */
  @GetMapping("/users")
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public List<UserResponse> listUsers() {
    return listUsersUseCase.execute().stream().map(UserResponse::from).toList();
  }

  /**
   * Disables an IAM user.
   *
   * @param userId user id
   * @return updated user
   */
  @PostMapping("/users/{userId}/disable")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public UserResponse disableUser(@PathVariable String userId) {
    return UserResponse.from(disableUserUseCase.execute(userId));
  }

  /**
   * Lists all IAM groups.
   *
   * @return group summaries
   */
  @GetMapping("/groups")
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public List<GroupResponse> listGroups() {
    return groupRepository.findAll().stream().map(GroupResponse::from).toList();
  }

  /**
   * Creates a new IAM group.
   *
   * @param request group creation payload
   * @return created group
   */
  @PostMapping("/groups")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<GroupResponse> createGroup(@RequestBody CreateGroupRequest request) {
    Group group = createGroupUseCase.execute(request.name());
    return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(group));
  }

  /**
   * Adds a user to a group.
   *
   * @param groupId group id
   * @param request member payload
   * @return empty response
   */
  @PostMapping("/groups/{groupId}/members")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<Void> addGroupMember(
      @PathVariable String groupId, @RequestBody MemberRequest request) {
    assignMembershipUseCase.addToGroup(groupId, request.userId());
    return ResponseEntity.noContent().build();
  }

  /**
   * Lists all IAM roles.
   *
   * @return role summaries
   */
  @GetMapping("/roles")
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public List<RoleResponse> listRoles() {
    return roleRepository.findAll().stream().map(RoleResponse::from).toList();
  }

  /**
   * Creates a new IAM role.
   *
   * @param request role creation payload
   * @return created role
   */
  @PostMapping("/roles")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<RoleResponse> createRole(@RequestBody CreateRoleRequest request) {
    Role role = createRoleUseCase.execute(request.name(), request.trustPolicyJson());
    return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.from(role));
  }

  /**
   * Assigns a role to a user.
   *
   * @param roleId role id
   * @param request member payload
   * @return empty response
   */
  @PostMapping("/roles/{roleId}/assign")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<Void> assignRole(
      @PathVariable String roleId, @RequestBody MemberRequest request) {
    assignMembershipUseCase.assignRole(request.userId(), roleId);
    return ResponseEntity.noContent().build();
  }

  /** Request body for creating a group. */
  public record CreateGroupRequest(String name) {}

  /** Request body for creating a role. */
  public record CreateRoleRequest(String name, String trustPolicyJson) {}

  /** Request body referencing a user id. */
  public record MemberRequest(String userId) {}

  /** IAM user summary. */
  public record UserResponse(String id, String username, String email, boolean enabled) {
    static UserResponse from(IamUser user) {
      return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.isEnabled());
    }
  }

  /** IAM group summary. */
  public record GroupResponse(String id, String name) {
    static GroupResponse from(Group group) {
      return new GroupResponse(group.getId(), group.getName());
    }
  }

  /** IAM role summary. */
  public record RoleResponse(String id, String name, String arn) {
    static RoleResponse from(Role role) {
      return new RoleResponse(role.getId(), role.getName(), role.getArn().value());
    }
  }
}
