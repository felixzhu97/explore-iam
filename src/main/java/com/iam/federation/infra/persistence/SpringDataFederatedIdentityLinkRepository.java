package com.iam.federation.infra.persistence;

import com.iam.federation.domain.model.FederatedIdentityLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataFederatedIdentityLinkRepository
    extends JpaRepository<FederatedIdentityLink, String> {

  Optional<FederatedIdentityLink> findByProviderAndExternalSubject(
      String provider, String externalSubject);
}
