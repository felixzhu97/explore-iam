package com.iam.policy.infra.persistence;

import com.iam.policy.domain.model.Permission;
import com.iam.policy.domain.repository.PermissionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaPermissionRepository implements PermissionRepository {

  private final SpringDataPermissionRepository springData;

  JpaPermissionRepository(SpringDataPermissionRepository springData) {
    this.springData = springData;
  }

  @Override
  public Permission save(Permission permission) {
    return springData.save(permission);
  }

  @Override
  public Optional<Permission> findByCode(String code) {
    return springData.findByCode(code);
  }

  @Override
  public List<Permission> findAll() {
    return springData.findAll();
  }

  @Override
  public boolean existsByCode(String code) {
    return springData.existsByCode(code);
  }
}
