package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.TotpCredential;
import com.iam.identity.domain.repository.TotpCredentialRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** JPA adapter for {@link TotpCredentialRepository}. */
@Repository
public class JpaTotpCredentialRepository implements TotpCredentialRepository {

  private final SpringDataTotpCredentialRepository springData;

  JpaTotpCredentialRepository(SpringDataTotpCredentialRepository springData) {
    this.springData = springData;
  }

  @Override
  public TotpCredential save(TotpCredential credential) {
    return springData.save(credential);
  }

  @Override
  public Optional<TotpCredential> findByUserId(String userId) {
    return springData.findByUserId(userId);
  }

  @Override
  public void deleteByUserId(String userId) {
    springData.deleteByUserId(userId);
  }
}
