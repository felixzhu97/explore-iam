package com.iam.identity.infra.persistence;

import com.iam.common.domain.vo.Arn;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.repository.RoleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaRoleRepository implements RoleRepository {

  private final SpringDataRoleRepository roleRepository;

  JpaRoleRepository(SpringDataRoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public Optional<Role> findByName(String name) {
    return roleRepository.findByName(name);
  }

  @Override
  public Optional<Role> findByArn(String arn) {
    return roleRepository.findByArn(new Arn(arn));
  }

  @Override
  public Optional<Role> findById(String id) {
    return roleRepository.findById(id);
  }

  @Override
  public Role save(Role role) {
    return roleRepository.save(role);
  }

  @Override
  public List<Role> findByUserId(String userId) {
    return roleRepository.findRolesByUserId(userId);
  }

  @Override
  public List<Role> findAll() {
    return roleRepository.findAll();
  }
}
