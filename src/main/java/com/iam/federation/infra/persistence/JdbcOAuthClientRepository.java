package com.iam.federation.infra.persistence;

import com.iam.federation.domain.model.ClientId;
import com.iam.federation.domain.model.OAuthClient;
import com.iam.federation.domain.repository.OAuthClientRepository;
import com.iam.federation.mapper.OAuthClientMapper;
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
public class JdbcOAuthClientRepository implements OAuthClientRepository {

  private final JdbcRegisteredClientRepository registeredClientRepository;
  private final JdbcTemplate jdbcTemplate;

  /**
   * Creates the JDBC-backed OIDC client repository.
   *
   * @param jdbcTemplate shared JDBC template
   */
  public JdbcOAuthClientRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
  }

  public JdbcRegisteredClientRepository getRegisteredClientRepository() {
    return this.registeredClientRepository;
  }

  @Override
  public OAuthClient save(OAuthClient client) {
    this.registeredClientRepository.save(OAuthClientMapper.toRegisteredClient(client));
    return findById(client.id()).orElse(client);
  }

  @Override
  public Optional<OAuthClient> findByClientId(ClientId clientId) {
    RegisteredClient registered = this.registeredClientRepository.findByClientId(clientId.value());
    return Optional.ofNullable(registered).map(OAuthClientMapper::toDomain);
  }

  @Override
  public Optional<OAuthClient> findById(String id) {
    RegisteredClient registered = this.registeredClientRepository.findById(id);
    return Optional.ofNullable(registered).map(OAuthClientMapper::toDomain);
  }

  @Override
  public List<OAuthClient> findAll() {
    List<String> ids =
        this.jdbcTemplate.query(
            "SELECT id FROM oauth2_registered_client ORDER BY client_id_issued_at DESC",
            (rs, rowNum) -> rs.getString("id"));
    return ids.stream()
        .map(this.registeredClientRepository::findById)
        .filter(java.util.Objects::nonNull)
        .map(OAuthClientMapper::toDomain)
        .toList();
  }
}
