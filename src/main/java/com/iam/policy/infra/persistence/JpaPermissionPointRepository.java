package com.iam.policy.infra.persistence;

import com.iam.policy.domain.model.PermissionPoint;
import com.iam.policy.domain.repository.PermissionPointRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaPermissionPointRepository implements PermissionPointRepository {

  private final SpringDataPermissionPointRepository springData;

  JpaPermissionPointRepository(SpringDataPermissionPointRepository springData) {
    this.springData = springData;
  }

  @Override
  public PermissionPoint save(PermissionPoint permissionPoint) {
    return springData.save(permissionPoint);
  }

  @Override
  public Optional<PermissionPoint> findByCode(String code) {
    return springData.findByCode(code);
  }

  @Override
  public List<PermissionPoint> findAll() {
    return springData.findAll();
  }

  @Override
  public boolean existsByCode(String code) {
    return springData.existsByCode(code);
  }
}
