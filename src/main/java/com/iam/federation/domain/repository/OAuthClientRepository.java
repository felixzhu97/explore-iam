package com.iam.federation.domain.repository;

import com.iam.federation.domain.model.ClientId;
import com.iam.federation.domain.model.OAuthClient;
import java.util.List;
import java.util.Optional;

/** Persistence port for registered OIDC clients. */
public interface OAuthClientRepository {

  /**
   * Persists a new or updated OIDC client.
   *
   * @param client aggregate to store
   * @return stored aggregate
   */
  OAuthClient save(OAuthClient client);

  /**
   * Finds a client by its public client_id.
   *
   * @param clientId public identifier
   * @return matching client when present
   */
  Optional<OAuthClient> findByClientId(ClientId clientId);

  /**
   * Finds a client by its internal identifier.
   *
   * @param id internal id
   * @return matching client when present
   */
  Optional<OAuthClient> findById(String id);

  /**
   * Lists all registered OIDC clients.
   *
   * @return all clients in stable iteration order
   */
  List<OAuthClient> findAll();
}
