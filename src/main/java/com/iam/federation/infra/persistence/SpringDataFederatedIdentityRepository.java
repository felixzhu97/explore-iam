package com.iam.federation.infra.persistence;

import com.iam.federation.domain.model.FederatedIdentity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataFederatedIdentityRepository
    extends JpaRepository<FederatedIdentity, String> {

  Optional<FederatedIdentity> findByProviderAndExternalSubject(
      String provider, String externalSubject);
}
