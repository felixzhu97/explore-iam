package com.iam.identity.service;

import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates a new IAM role with an optional trust policy. */
@Service
public class CreateRoleService {

  private final RoleRepository roleRepository;
  private final ManagementAuditRecorder managementAuditRecorder;

  /**
   * Creates the use case.
   *
   * @param roleRepository role repository
   * @param managementAuditRecorder management audit recorder
   */
  public CreateRoleService(
      RoleRepository roleRepository, ManagementAuditRecorder managementAuditRecorder) {
    this.roleRepository = roleRepository;
    this.managementAuditRecorder = managementAuditRecorder;
  }

  /**
   * Creates a role with the given name and trust policy JSON.
   *
   * @param name role name
   * @param trustPolicyJson optional trust policy JSON
   * @return persisted role
   */
  @Transactional
  public Role execute(String name, String trustPolicyJson) {
    Role role = roleRepository.save(Role.create(name, trustPolicyJson));
    managementAuditRecorder.recordSuccess("identity:CreateRole", "Role", role.getId());
    return role;
  }
}
