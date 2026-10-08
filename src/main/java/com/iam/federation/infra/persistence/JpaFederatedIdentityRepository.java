package com.iam.federation.infra.persistence;

import com.iam.federation.domain.model.FederatedIdentity;
import com.iam.federation.domain.repository.FederatedIdentityRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaFederatedIdentityRepository implements FederatedIdentityRepository {

  private final SpringDataFederatedIdentityRepository springData;

  JpaFederatedIdentityRepository(SpringDataFederatedIdentityRepository springData) {
    this.springData = springData;
  }

  @Override
  public FederatedIdentity save(FederatedIdentity link) {
    return springData.save(link);
  }

  @Override
  public Optional<FederatedIdentity> findByProviderAndExternalSubject(
      String provider, String externalSubject) {
    return springData.findByProviderAndExternalSubject(provider, externalSubject);
  }
}
