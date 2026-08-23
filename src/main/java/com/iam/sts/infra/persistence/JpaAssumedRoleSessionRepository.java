package com.iam.sts.infra.persistence;

import com.iam.sts.domain.model.AssumedRoleSession;
import com.iam.sts.domain.repository.AssumedRoleSessionRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaAssumedRoleSessionRepository implements AssumedRoleSessionRepository {

  private final SpringDataAssumedRoleSessionRepository springData;

  JpaAssumedRoleSessionRepository(SpringDataAssumedRoleSessionRepository springData) {
    this.springData = springData;
  }

  @Override
  public AssumedRoleSession save(AssumedRoleSession session) {
    return springData.save(session);
  }

  @Override
  public Optional<AssumedRoleSession> findById(String id) {
    return springData.findById(id);
  }
}
