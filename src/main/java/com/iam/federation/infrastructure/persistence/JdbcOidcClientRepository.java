package com.iam.federation.infrastructure.persistence;

import com.iam.federation.domain.model.OidcClient;
import com.iam.federation.domain.repository.OidcClientRepository;
import com.iam.federation.domain.vo.ClientId;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Repository;

/**
 * Persists OIDC clients in {@code oauth2_registered_client} via Spring Authorization Server JDBC
 * support, plus list queries for the Console.
 */
@Repository
public class JdbcOidcClientRepository implements OidcClientRepository {

  private final JdbcRegisteredClientRepository registeredClientRepository;
  private final JdbcTemplate jdbcTemplate;

  /**
   * Creates the JDBC-backed OIDC client repository.
   *
   * @param jdbcTemplate shared JDBC template
   */
  public JdbcOidcClientRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
  }

  public JdbcRegisteredClientRepository getRegisteredClientRepository() {
    return this.registeredClientRepository;
  }

  @Override
  public OidcClient save(OidcClient client) {
    this.registeredClientRepository.save(OidcClientMapper.toRegisteredClient(client));
    return findById(client.getId()).orElse(client);
  }

  @Override
  public Optional<OidcClient> findByClientId(ClientId clientId) {
    RegisteredClient registered = this.registeredClientRepository.findByClientId(clientId.value());
    return Optional.ofNullable(registered).map(OidcClientMapper::toDomain);
  }

  @Override
  public Optional<OidcClient> findById(String id) {
    RegisteredClient registered = this.registeredClientRepository.findById(id);
    return Optional.ofNullable(registered).map(OidcClientMapper::toDomain);
  }

  @Override
  public List<OidcClient> findAll() {
    List<String> ids =
        this.jdbcTemplate.query(
            "SELECT id FROM oauth2_registered_client ORDER BY client_id_issued_at DESC",
            (rs, rowNum) -> rs.getString("id"));
    return ids.stream()
        .map(this.registeredClientRepository::findById)
        .filter(java.util.Objects::nonNull)
        .map(OidcClientMapper::toDomain)
        .toList();
  }
}
