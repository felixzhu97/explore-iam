package com.iam.identity.service;

import com.iam.audit.service.AdminActivityRecorder;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.RoleRepository;
import com.iam.identity.domain.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** IAM role listing and assignment operations. */
@Service
@Transactional(readOnly = true)
public class RoleService {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final AdminActivityRecorder adminActivityRecorder;

  /**
   * Creates the role service.
   *
   * @param roleRepository role repository
   * @param userRepository IAM user repository
   * @param adminActivityRecorder management audit recorder
   */
  public RoleService(
      RoleRepository roleRepository,
      UserRepository userRepository,
      AdminActivityRecorder adminActivityRecorder) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.adminActivityRecorder = adminActivityRecorder;
  }

  /**
   * Returns all IAM roles.
   *
   * @return role list
   */
  public List<Role> findAll() {
    return roleRepository.findAll();
  }

  /**
   * Creates a role with the given name and trust policy JSON.
   *
   * @param name role name
   * @param trustPolicyJson optional trust policy JSON
   * @return persisted role
   */
  @Transactional
  public Role createRole(String name, String trustPolicyJson) {
    Role role = roleRepository.save(Role.createRole(name, trustPolicyJson));
    adminActivityRecorder.recordSuccess("identity:CreateRole", "Role", role.getId());
    return role;
  }

  /**
   * Assigns a role to a user.
   *
   * @param userId user id
   * @param roleId role id
   */
  @Transactional
  public void assignToUser(String userId, String roleId) {
    roleRepository
        .findById(roleId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "role not found"));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    user.assignRole(roleId);
    userRepository.save(user);
    adminActivityRecorder.recordSuccess("identity:AssignRole", "User", userId);
  }

  /**
   * Removes a role assignment from a user.
   *
   * @param userId user id
   * @param roleId role id
   */
  @Transactional
  public void unassignFromUser(String userId, String roleId) {
    roleRepository
        .findById(roleId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "role not found"));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    user.unassignRole(roleId);
    userRepository.save(user);
    adminActivityRecorder.recordSuccess("identity:UnassignRole", "User", userId);
  }
}
