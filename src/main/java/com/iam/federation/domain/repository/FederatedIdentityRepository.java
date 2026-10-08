package com.iam.federation.domain.repository;

import com.iam.federation.domain.model.FederatedIdentity;
import java.util.Optional;

/** Persistence port for federated identity links. */
public interface FederatedIdentityRepository {

  /**
   * Persists a federated identity link.
   *
   * @param link link to store
   * @return stored link
   */
  FederatedIdentity save(FederatedIdentity link);

  /**
   * Finds a link by provider and external subject.
   *
   * @param provider OAuth provider id
   * @param externalSubject subject from the provider
   * @return matching link when present
   */
  Optional<FederatedIdentity> findByProviderAndExternalSubject(
      String provider, String externalSubject);
}
