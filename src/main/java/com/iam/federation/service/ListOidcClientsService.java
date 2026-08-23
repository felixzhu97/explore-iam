package com.iam.federation.service;

import com.iam.federation.domain.model.OidcClient;
import com.iam.federation.domain.repository.OidcClientRepository;
import com.iam.federation.domain.vo.ClientId;
import com.iam.federation.domain.vo.RedirectUri;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lists registered OIDC clients for the console and API. */
@Service
public class ListOidcClientsService {

  private final OidcClientRepository oidcClientRepository;

  /**
   * Creates the list-clients use case.
   *
   * @param oidcClientRepository client repository
   */
  public ListOidcClientsService(OidcClientRepository oidcClientRepository) {
    this.oidcClientRepository = oidcClientRepository;
  }

  /**
   * Returns all clients as read models.
   *
   * @return client views
   */
  @Transactional(readOnly = true)
  public List<OidcClientView> list() {
    return this.oidcClientRepository.findAll().stream().map(OidcClientView::from).toList();
  }

  /**
   * Finds one client by public client_id.
   *
   * @param clientId public client_id
   * @return view when present
   */
  @Transactional(readOnly = true)
  public Optional<OidcClientView> findByClientId(String clientId) {
    return this.oidcClientRepository
        .findByClientId(new ClientId(clientId))
        .map(OidcClientView::from);
  }

  /** Read model for an OIDC client without the secret. */
  public record OidcClientView(
      String id,
      String clientId,
      String clientName,
      String clientUri,
      Set<String> redirectUris,
      Set<String> postLogoutRedirectUris,
      Set<String> scopes,
      Set<String> responseTypes,
      Set<String> authorizationGrantTypes,
      Set<String> clientAuthenticationMethods) {

    static OidcClientView from(OidcClient client) {
      return new OidcClientView(
          client.getId(),
          client.getClientId().value(),
          client.getClientName(),
          client.getClientUri(),
          client.getRedirectUris().stream()
              .map(RedirectUri::value)
              .collect(Collectors.toCollection(LinkedHashSet::new)),
          client.getPostLogoutRedirectUris().stream()
              .map(RedirectUri::value)
              .collect(Collectors.toCollection(LinkedHashSet::new)),
          client.getScopes(),
          client.getResponseTypes(),
          client.getAuthorizationGrantTypes(),
          client.getClientAuthenticationMethods());
    }
  }
}
