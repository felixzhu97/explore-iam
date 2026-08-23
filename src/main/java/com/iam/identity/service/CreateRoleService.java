package com.iam.identity.service;

import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.model.TrustPolicyDocument;
import com.iam.identity.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates a new IAM role with an optional trust policy. */
@Service
public class CreateRoleService {

  private final RoleRepository roleRepository;

  /**
   * Creates the use case.
   *
   * @param roleRepository role repository
   */
  public CreateRoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
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
    TrustPolicyDocument trust =
        trustPolicyJson == null || trustPolicyJson.isBlank()
            ? TrustPolicyDocument.allowAll()
            : new TrustPolicyDocument(trustPolicyJson);
    return roleRepository.save(Role.create(name, trust));
  }
}
