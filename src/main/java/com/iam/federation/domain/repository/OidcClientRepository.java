package com.iam.federation.domain.repository;

import com.iam.federation.domain.model.OidcClient;
import com.iam.federation.domain.vo.ClientId;
import java.util.List;
import java.util.Optional;

public interface OidcClientRepository {

    OidcClient save(OidcClient client);

    Optional<OidcClient> findByClientId(ClientId clientId);

    Optional<OidcClient> findById(String id);

    List<OidcClient> findAll();
}
