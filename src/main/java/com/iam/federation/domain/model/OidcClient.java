package com.iam.federation.domain.model;

import com.iam.federation.domain.vo.ClientId;
import com.iam.federation.domain.vo.RedirectUri;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * OIDC Relying Party registered with Explore IAM (maps to Spring Authorization Server {@code
 * RegisteredClient}).
 */
public class OidcClient {

  private final String id;
  private final ClientId clientId;
  private final Instant clientIdIssuedAt;
  private String clientName;
  private String clientSecretHash;
  private final String clientUri;
  private final Set<RedirectUri> redirectUris;
  private final Set<RedirectUri> postLogoutRedirectUris;
  private final Set<String> scopes;
  private final Set<String> responseTypes;
  private final Set<String> clientAuthenticationMethods;
  private final Set<String> authorizationGrantTypes;
  private final boolean requireAuthorizationConsent;

  private OidcClient(
      String id,
      ClientId clientId,
      Instant clientIdIssuedAt,
      String clientName,
      String clientSecretHash,
      String clientUri,
      Set<RedirectUri> redirectUris,
      Set<RedirectUri> postLogoutRedirectUris,
      Set<String> scopes,
      Set<String> responseTypes,
      Set<String> clientAuthenticationMethods,
      Set<String> authorizationGrantTypes,
      boolean requireAuthorizationConsent) {
    this.id = Objects.requireNonNull(id, "id");
    this.clientId = Objects.requireNonNull(clientId, "clientId");
    this.clientIdIssuedAt = Objects.requireNonNull(clientIdIssuedAt, "clientIdIssuedAt");
    this.clientName = requireName(clientName);
    this.clientSecretHash = clientSecretHash;
    this.clientUri = clientUri;
    this.redirectUris = copyUris(redirectUris, "redirectUris");
    this.postLogoutRedirectUris =
        copyUris(
            postLogoutRedirectUris == null ? Set.of() : postLogoutRedirectUris,
            "postLogoutRedirectUris");
    this.scopes = copyStrings(scopes, "scopes");
    this.responseTypes = copyStrings(responseTypes, "responseTypes");
    this.clientAuthenticationMethods =
        copyStrings(clientAuthenticationMethods, "clientAuthenticationMethods");
    this.authorizationGrantTypes = copyStrings(authorizationGrantTypes, "authorizationGrantTypes");
    this.requireAuthorizationConsent = requireAuthorizationConsent;
    if (this.redirectUris.isEmpty()) {
      throw new IllegalArgumentException("at least one redirect_uri is required");
    }
    if (this.scopes.isEmpty()) {
      throw new IllegalArgumentException("at least one scope is required");
    }
    if (this.responseTypes.isEmpty()) {
      throw new IllegalArgumentException("at least one response_type is required");
    }
    if (this.clientAuthenticationMethods.isEmpty()) {
      throw new IllegalArgumentException("at least one client authentication method is required");
    }
    if (this.authorizationGrantTypes.isEmpty()) {
      throw new IllegalArgumentException("at least one authorization grant type is required");
    }
    requireOpenIdScope(this.scopes);
    requireAuthorizationCodeGrant(this.authorizationGrantTypes);
    requireExclusiveNoneAuth(this.clientAuthenticationMethods);
  }

  /**
   * Registers a new Relying Party with a generated client_id and hashed secret.
   *
   * @param clientName display name
   * @param clientSecretHash encoded client secret
   * @param clientUri optional client homepage
   * @param redirectUris allowed redirect URIs
   * @param postLogoutRedirectUris allowed post-logout redirect URIs
   * @param scopes requested scopes
   * @param responseTypes OAuth response types
   * @param clientAuthenticationMethods token endpoint auth methods
   * @param authorizationGrantTypes allowed grant types
   * @return new aggregate
   */
  public static OidcClient register(
      String clientName,
      String clientSecretHash,
      String clientUri,
      Set<RedirectUri> redirectUris,
      Set<RedirectUri> postLogoutRedirectUris,
      Set<String> scopes,
      Set<String> responseTypes,
      Set<String> clientAuthenticationMethods,
      Set<String> authorizationGrantTypes) {
    ClientId clientId =
        new ClientId("app-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
    return new OidcClient(
        UUID.randomUUID().toString(),
        clientId,
        Instant.now(),
        clientName,
        clientSecretHash,
        clientUri,
        redirectUris,
        postLogoutRedirectUris,
        scopes,
        responseTypes,
        clientAuthenticationMethods,
        authorizationGrantTypes,
        false);
  }

  /**
   * Seeds a known Relying Party (for local bootstrap) with fixed protocol defaults.
   *
   * @param clientId public client_id
   * @param clientName display name
   * @param clientSecretHash encoded client secret
   * @param redirectUris allowed redirect URIs
   * @param postLogoutRedirectUris allowed post-logout redirect URIs
   * @param scopes requested scopes
   * @return seeded aggregate
   */
  public static OidcClient seed(
      ClientId clientId,
      String clientName,
      String clientSecretHash,
      Set<RedirectUri> redirectUris,
      Set<RedirectUri> postLogoutRedirectUris,
      Set<String> scopes) {
    return new OidcClient(
        UUID.randomUUID().toString(),
        clientId,
        Instant.now(),
        clientName,
        clientSecretHash,
        null,
        redirectUris,
        postLogoutRedirectUris,
        scopes,
        Set.of("code"),
        Set.of("client_secret_basic", "client_secret_post"),
        Set.of("authorization_code", "refresh_token"),
        false);
  }

  /**
   * Seeds a public native / SPA Relying Party ({@code none} + PKCE).
   *
   * @param clientId public client_id
   * @param clientName display name
   * @param redirectUris allowed redirect URIs (custom schemes allowed)
   * @param postLogoutRedirectUris allowed post-logout redirect URIs
   * @param scopes requested scopes
   * @return seeded public aggregate
   */
  public static OidcClient seedPublic(
      ClientId clientId,
      String clientName,
      Set<RedirectUri> redirectUris,
      Set<RedirectUri> postLogoutRedirectUris,
      Set<String> scopes) {
    return new OidcClient(
        UUID.randomUUID().toString(),
        clientId,
        Instant.now(),
        clientName,
        null,
        null,
        redirectUris,
        postLogoutRedirectUris,
        scopes,
        Set.of("code"),
        Set.of("none"),
        Set.of("authorization_code", "refresh_token"),
        false);
  }

  /**
   * Rebuilds an aggregate from persistence without changing identifiers.
   *
   * @param id internal id
   * @param clientId public client_id
   * @param clientIdIssuedAt issuance timestamp
   * @param clientName display name
   * @param clientSecretHash encoded client secret
   * @param clientUri optional client homepage
   * @param redirectUris allowed redirect URIs
   * @param postLogoutRedirectUris allowed post-logout redirect URIs
   * @param scopes requested scopes
   * @param responseTypes OAuth response types
   * @param clientAuthenticationMethods token endpoint auth methods
   * @param authorizationGrantTypes allowed grant types
   * @param requireAuthorizationConsent whether consent is required
   * @return reconstituted aggregate
   */
  public static OidcClient reconstitute(
      String id,
      ClientId clientId,
      Instant clientIdIssuedAt,
      String clientName,
      String clientSecretHash,
      String clientUri,
      Set<RedirectUri> redirectUris,
      Set<RedirectUri> postLogoutRedirectUris,
      Set<String> scopes,
      Set<String> responseTypes,
      Set<String> clientAuthenticationMethods,
      Set<String> authorizationGrantTypes,
      boolean requireAuthorizationConsent) {
    return new OidcClient(
        id,
        clientId,
        clientIdIssuedAt,
        clientName,
        clientSecretHash,
        clientUri,
        redirectUris,
        postLogoutRedirectUris,
        scopes,
        responseTypes,
        clientAuthenticationMethods,
        authorizationGrantTypes,
        requireAuthorizationConsent);
  }

  private static String requireName(String clientName) {
    if (clientName == null || clientName.isBlank()) {
      throw new IllegalArgumentException("clientName must not be blank");
    }
    return clientName.trim();
  }

  private static Set<RedirectUri> copyUris(Set<RedirectUri> uris, String field) {
    Objects.requireNonNull(uris, field);
    return Set.copyOf(new LinkedHashSet<>(uris));
  }

  private static Set<String> copyStrings(Set<String> values, String field) {
    Objects.requireNonNull(values, field);
    LinkedHashSet<String> copy = new LinkedHashSet<>();
    for (String value : values) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException(field + " contains a blank value");
      }
      copy.add(value.trim());
    }
    return Set.copyOf(copy);
  }

  private static void requireOpenIdScope(Set<String> scopes) {
    if (!scopes.contains("openid")) {
      throw new IllegalArgumentException("scopes must include openid");
    }
  }

  private static void requireAuthorizationCodeGrant(Set<String> grantTypes) {
    if (!grantTypes.contains("authorization_code")) {
      throw new IllegalArgumentException("authorization_code grant type is required");
    }
  }

  private static void requireExclusiveNoneAuth(Set<String> authMethods) {
    if (authMethods.contains("none") && authMethods.size() > 1) {
      throw new IllegalArgumentException(
          "none cannot be combined with other authentication methods");
    }
  }

  public boolean isPublicClient() {
    return this.clientAuthenticationMethods.size() == 1
        && this.clientAuthenticationMethods.contains("none");
  }

  /** Returns the internal persistence id. */
  public String id() {
    return id;
  }

  /** Returns the public client_id. */
  public ClientId clientId() {
    return clientId;
  }

  /** Returns when the client_id was issued. */
  public Instant clientIdIssuedAt() {
    return clientIdIssuedAt;
  }

  /** Returns the display name shown in consent and admin UIs. */
  public String clientName() {
    return clientName;
  }

  /** Returns the optional client homepage URI. */
  public String clientUri() {
    return clientUri;
  }

  /** Returns allowed redirect URIs. */
  public Set<RedirectUri> redirectUris() {
    return redirectUris;
  }

  /** Returns allowed post-logout redirect URIs. */
  public Set<RedirectUri> postLogoutRedirectUris() {
    return postLogoutRedirectUris;
  }

  /** Returns registered OAuth scopes. */
  public Set<String> scopes() {
    return scopes;
  }

  /** Returns registered OAuth response types. */
  public Set<String> responseTypes() {
    return responseTypes;
  }

  /** Returns token-endpoint client authentication methods. */
  public Set<String> clientAuthenticationMethods() {
    return clientAuthenticationMethods;
  }

  /** Returns allowed authorization grant types. */
  public Set<String> authorizationGrantTypes() {
    return authorizationGrantTypes;
  }

  /** Returns true when authorization consent is required. */
  public boolean requiresAuthorizationConsent() {
    return requireAuthorizationConsent;
  }

  /**
   * Returns the stored client secret hash for persistence adapters only.
   *
   * @return encoded secret hash, or null for public clients
   */
  public String storedSecretHash() {
    return clientSecretHash;
  }
}
