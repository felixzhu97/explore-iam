package com.iam.identity.service;

import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.repository.IamUserRepository;
import com.iam.identity.domain.repository.RoleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** IAM role listing and assignment operations. */
@Service
@Transactional(readOnly = true)
public class RoleService {

  private final RoleRepository roleRepository;
  private final IamUserRepository iamUserRepository;
  private final ManagementAuditRecorder managementAuditRecorder;

  /**
   * Creates the role service.
   *
   * @param roleRepository role repository
   * @param iamUserRepository IAM user repository
   * @param managementAuditRecorder management audit recorder
   */
  public RoleService(
      RoleRepository roleRepository,
      IamUserRepository iamUserRepository,
      ManagementAuditRecorder managementAuditRecorder) {
    this.roleRepository = roleRepository;
    this.iamUserRepository = iamUserRepository;
    this.managementAuditRecorder = managementAuditRecorder;
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
  public Role create(String name, String trustPolicyJson) {
    Role role = roleRepository.save(Role.create(name, trustPolicyJson));
    managementAuditRecorder.recordSuccess("identity:CreateRole", "Role", role.getId());
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
        .orElseThrow(() -> new IllegalArgumentException("role not found: " + roleId));
    IamUser user =
        iamUserRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
    user.assignRole(roleId);
    iamUserRepository.save(user);
    managementAuditRecorder.recordSuccess("identity:AssignRole", "User", userId);
  }
}
