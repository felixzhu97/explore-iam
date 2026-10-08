package com.iam.sts.infra.persistence;

import com.iam.sts.domain.model.ShortLivedCredential;
import com.iam.sts.domain.repository.ShortLivedCredentialRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaShortLivedCredentialRepository implements ShortLivedCredentialRepository {

  private final SpringDataShortLivedCredentialRepository springData;

  JpaShortLivedCredentialRepository(SpringDataShortLivedCredentialRepository springData) {
    this.springData = springData;
  }

  @Override
  public ShortLivedCredential save(ShortLivedCredential session) {
    return springData.save(session);
  }

  @Override
  public Optional<ShortLivedCredential> findById(String id) {
    return springData.findById(id);
  }
}
