package com.iam.federation.service;

import com.iam.audit.service.AdminActivityRecorder;
import com.iam.federation.domain.model.ClientId;
import com.iam.federation.domain.model.OAuthClient;
import com.iam.federation.domain.model.RedirectUri;
import com.iam.federation.domain.repository.OAuthClientRepository;
import com.iam.federation.infra.config.OidcSeedClientProperties;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Registers, lists, and looks up OIDC Relying Parties. Registration returns the plaintext client
 * secret once for confidential clients.
 */
@Service
@Transactional(readOnly = true)
public class OAuthClientService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final OAuthClientRepository oauthClientRepository;
  private final PasswordEncoder passwordEncoder;
  private final OidcSeedClientProperties oidcProperties;
  private final AdminActivityRecorder adminActivityRecorder;

  /**
   * Creates the OIDC client service.
   *
   * @param oauthClientRepository client repository
   * @param passwordEncoder encoder for client secrets
   * @param oidcProperties OIDC defaults and allow-lists
   * @param adminActivityRecorder management audit recorder
   */
  public OAuthClientService(
      OAuthClientRepository oauthClientRepository,
      PasswordEncoder passwordEncoder,
      OidcSeedClientProperties oidcProperties,
      AdminActivityRecorder adminActivityRecorder) {
    this.oauthClientRepository = oauthClientRepository;
    this.passwordEncoder = passwordEncoder;
    this.oidcProperties = oidcProperties;
    this.adminActivityRecorder = adminActivityRecorder;
  }

  /**
   * Returns all clients as read models.
   *
   * @return client views
   */
  public List<OAuthClientView> findAll() {
    return oauthClientRepository.findAll().stream().map(OAuthClientView::from).toList();
  }

  /**
   * Finds one client by public client_id.
   *
   * @param clientId public client_id
   * @return view when present
   */
  public Optional<OAuthClientView> findByClientId(String clientId) {
    return oauthClientRepository.findByClientId(new ClientId(clientId)).map(OAuthClientView::from);
  }

  /**
   * Registers a Relying Party and returns the plaintext secret once.
   *
   * @param command registration input
   * @return created client including one-time secret
   */
  @Transactional
  public RegisteredOAuthClientResult register(RegisterOAuthClientCommand command) {
    Objects.requireNonNull(command, "command");
    Set<RedirectUri> redirectUris = toRedirectUris(command.redirectUris());
    Set<RedirectUri> postLogout =
        command.postLogoutRedirectUris() == null
            ? Set.of()
            : toRedirectUris(command.postLogoutRedirectUris());
    Set<String> scopes = normalizeScopes(command.scopes());
    Set<String> responseTypes = normalizeResponseTypes(command.responseTypes());
    Set<String> grantTypes = normalizeGrantTypes(command.authorizationGrantTypes());
    Set<String> authMethods = normalizeAuthMethods(command.clientAuthenticationMethods());
    String clientUri = normalizeClientUri(command.clientUri());

    boolean publicClient = authMethods.size() == 1 && authMethods.contains("none");
    String plaintextSecret = null;
    String secretHash = null;
    if (!publicClient) {
      plaintextSecret = generateSecret();
      secretHash = passwordEncoder.encode(plaintextSecret);
    }

    OAuthClient client =
        OAuthClient.register(
            command.clientName(),
            secretHash,
            clientUri,
            redirectUris,
            postLogout,
            scopes,
            responseTypes,
            authMethods,
            grantTypes);
    OAuthClient saved = oauthClientRepository.save(client);
    adminActivityRecorder.recordSuccess(
        "federation:RegisterClient", "OAuthClient", saved.clientId().value());
    return RegisteredOAuthClientResult.from(saved, plaintextSecret);
  }

  /**
   * Replaces allowed scopes for an existing client.
   *
   * @param clientId public client_id
   * @param scopes new scope list (must include openid)
   * @return updated view
   */
  @Transactional
  public OAuthClientView updateScopes(String clientId, List<String> scopes) {
    OAuthClient client =
        oauthClientRepository
            .findByClientId(new ClientId(clientId))
            .orElseThrow(() -> new IllegalArgumentException("client not found: " + clientId));
    client.replaceScopes(normalizeScopes(scopes));
    OAuthClient saved = oauthClientRepository.save(client);
    adminActivityRecorder.recordSuccess(
        "federation:UpdateClientScopes", "OAuthClient", saved.clientId().value());
    return OAuthClientView.from(saved);
  }

  private Set<String> normalizeScopes(List<String> raw) {
    return raw == null || raw.isEmpty() ? oidcProperties.defaultScopes() : Set.copyOf(raw);
  }

  private Set<String> normalizeResponseTypes(List<String> raw) {
    Set<String> types = toNormalizedSet(raw, "responseTypes");
    if (types.isEmpty()) {
      types = oidcProperties.defaultResponseTypes();
    }
    Set<String> allowed = oidcProperties.allowedResponseTypes();
    for (String type : types) {
      if (!allowed.contains(type)) {
        throw new IllegalArgumentException("unsupported response_type: " + type);
      }
    }
    return types;
  }

  private Set<String> normalizeGrantTypes(List<String> raw) {
    Set<String> grants = toNormalizedSet(raw, "authorizationGrantTypes");
    if (grants.isEmpty()) {
      grants = oidcProperties.defaultGrantTypes();
    }
    Set<String> allowed = oidcProperties.allowedGrantTypes();
    for (String grant : grants) {
      if (!allowed.contains(grant)) {
        throw new IllegalArgumentException("unsupported authorization_grant_type: " + grant);
      }
    }
    return grants;
  }

  private Set<String> normalizeAuthMethods(List<String> raw) {
    Set<String> methods = toNormalizedSet(raw, "clientAuthenticationMethods");
    if (methods.isEmpty()) {
      methods = oidcProperties.defaultAuthMethods();
    }
    Set<String> allowed = oidcProperties.allowedAuthMethods();
    for (String method : methods) {
      if (!allowed.contains(method)) {
        throw new IllegalArgumentException("unsupported client_authentication_method: " + method);
      }
    }
    return methods;
  }

  private static String normalizeClientUri(String raw) {
    if (!StringUtils.hasText(raw)) {
      return null;
    }
    return new RedirectUri(raw.trim()).value();
  }

  private static Set<String> toNormalizedSet(List<String> raw, String field) {
    if (raw == null || raw.isEmpty()) {
      return Set.of();
    }
    LinkedHashSet<String> values = new LinkedHashSet<>();
    for (String value : raw) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException(field + " contains a blank value");
      }
      values.add(value.trim());
    }
    return Set.copyOf(values);
  }

  private static Set<RedirectUri> toRedirectUris(List<String> raw) {
    if (raw == null || raw.isEmpty()) {
      throw new IllegalArgumentException("at least one redirect_uri is required");
    }
    LinkedHashSet<RedirectUri> uris = new LinkedHashSet<>();
    for (String value : raw) {
      uris.add(new RedirectUri(value));
    }
    return uris;
  }

  private static String generateSecret() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  /** Input for registering an OIDC client. */
  public record RegisterOAuthClientCommand(
      String clientName,
      List<String> redirectUris,
      List<String> postLogoutRedirectUris,
      List<String> scopes,
      List<String> responseTypes,
      List<String> authorizationGrantTypes,
      List<String> clientAuthenticationMethods,
      String clientUri) {}

  /** Registration result including the one-time plaintext client secret. */
  public record RegisteredOAuthClientResult(
      String id,
      String clientId,
      String clientName,
      String clientSecret,
      String clientUri,
      Set<String> redirectUris,
      Set<String> postLogoutRedirectUris,
      Set<String> scopes,
      Set<String> responseTypes,
      Set<String> authorizationGrantTypes,
      Set<String> clientAuthenticationMethods) {

    static RegisteredOAuthClientResult from(OAuthClient client, String plaintextSecret) {
      return new RegisteredOAuthClientResult(
          client.id(),
          client.clientId().value(),
          client.clientName(),
          plaintextSecret,
          client.clientUri(),
          client.redirectUris().stream()
              .map(RedirectUri::value)
              .collect(Collectors.toCollection(LinkedHashSet::new)),
          client.postLogoutRedirectUris().stream()
              .map(RedirectUri::value)
              .collect(Collectors.toCollection(LinkedHashSet::new)),
          client.scopes(),
          client.responseTypes(),
          client.authorizationGrantTypes(),
          client.clientAuthenticationMethods());
    }
  }

  /** Read model for an OIDC client without the secret. */
  public record OAuthClientView(
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

    static OAuthClientView from(OAuthClient client) {
      return new OAuthClientView(
          client.id(),
          client.clientId().value(),
          client.clientName(),
          client.clientUri(),
          client.redirectUris().stream()
              .map(RedirectUri::value)
              .collect(Collectors.toCollection(LinkedHashSet::new)),
          client.postLogoutRedirectUris().stream()
              .map(RedirectUri::value)
              .collect(Collectors.toCollection(LinkedHashSet::new)),
          client.scopes(),
          client.responseTypes(),
          client.authorizationGrantTypes(),
          client.clientAuthenticationMethods());
    }
  }
}
