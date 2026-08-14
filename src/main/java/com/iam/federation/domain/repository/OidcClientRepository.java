package com.iam.federation.domain.repository;

import com.iam.federation.domain.model.OidcClient;
import com.iam.federation.domain.vo.ClientId;
import java.util.List;
import java.util.Optional;

/** Persistence port for registered OIDC clients. */
public interface OidcClientRepository {

  /**
   * Persists a new or updated OIDC client.
   *
   * @param client aggregate to store
   * @return stored aggregate
   */
  OidcClient save(OidcClient client);

  /**
   * Finds a client by its public client_id.
   *
   * @param clientId public identifier
   * @return matching client when present
   */
  Optional<OidcClient> findByClientId(ClientId clientId);

  /**
   * Finds a client by its internal identifier.
   *
   * @param id internal id
   * @return matching client when present
   */
  Optional<OidcClient> findById(String id);

  /**
   * Lists all registered OIDC clients.
   *
   * @return all clients in stable iteration order
   */
  List<OidcClient> findAll();
}
