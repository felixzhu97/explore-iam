package com.iam.federation.infra.persistence;

import com.iam.federation.domain.model.FederatedIdentityLink;
import com.iam.federation.domain.repository.FederatedIdentityLinkRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaFederatedIdentityLinkRepository implements FederatedIdentityLinkRepository {

  private final SpringDataFederatedIdentityLinkRepository springData;

  JpaFederatedIdentityLinkRepository(SpringDataFederatedIdentityLinkRepository springData) {
    this.springData = springData;
  }

  @Override
  public FederatedIdentityLink save(FederatedIdentityLink link) {
    return springData.save(link);
  }

  @Override
  public Optional<FederatedIdentityLink> findByProviderAndExternalSubject(
      String provider, String externalSubject) {
    return springData.findByProviderAndExternalSubject(provider, externalSubject);
  }
}
