package com.iam.scim.infra.persistence;

import com.iam.scim.domain.model.ProvisioningTarget;
import com.iam.scim.domain.repository.ProvisioningTargetRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaProvisioningTargetRepository implements ProvisioningTargetRepository {

  private final SpringDataProvisioningTargetRepository springData;

  JpaProvisioningTargetRepository(SpringDataProvisioningTargetRepository springData) {
    this.springData = springData;
  }

  @Override
  public Optional<ProvisioningTarget> findByName(String name) {
    return springData.findByName(name);
  }

  @Override
  public List<ProvisioningTarget> findEnabled() {
    return springData.findByEnabledTrue();
  }

  @Override
  public List<ProvisioningTarget> findAll() {
    return springData.findAll();
  }

  @Override
  public ProvisioningTarget save(ProvisioningTarget target) {
    return springData.save(target);
  }
}
