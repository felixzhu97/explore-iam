package com.iam.federation.infra.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** OIDC protocol defaults, allow-lists, and bootstrap seed clients. */
@ConfigurationProperties(prefix = "app.oidc")
public class OidcSeedClientProperties {

  private ProtocolDefaults defaults = new ProtocolDefaults();
  private ProtocolAllowed allowed = new ProtocolAllowed();
  private List<SeedClient> seedClients = new ArrayList<>();

  public ProtocolDefaults getDefaults() {
    return defaults;
  }

  public void setDefaults(ProtocolDefaults defaults) {
    this.defaults = defaults == null ? new ProtocolDefaults() : defaults;
  }

  public ProtocolAllowed getAllowed() {
    return allowed;
  }

  public void setAllowed(ProtocolAllowed allowed) {
    this.allowed = allowed == null ? new ProtocolAllowed() : allowed;
  }

  public List<SeedClient> getSeedClients() {
    return seedClients;
  }

  public void setSeedClients(List<SeedClient> seedClients) {
    this.seedClients = seedClients == null ? new ArrayList<>() : seedClients;
  }

  /**
   * Default scopes applied when a registration omits scopes.
   *
   * @return immutable default scopes
   */
  public Set<String> defaultScopes() {
    return Set.copyOf(new LinkedHashSet<>(defaults.getScopes()));
  }

  /**
   * Default OAuth response types.
   *
   * @return immutable default response types
   */
  public Set<String> defaultResponseTypes() {
    return Set.copyOf(new LinkedHashSet<>(defaults.getResponseTypes()));
  }

  /**
   * Default authorization grant types.
   *
   * @return immutable default grant types
   */
  public Set<String> defaultGrantTypes() {
    return Set.copyOf(new LinkedHashSet<>(defaults.getGrantTypes()));
  }

  /**
   * Default client authentication methods.
   *
   * @return immutable default auth methods
   */
  public Set<String> defaultAuthMethods() {
    return Set.copyOf(new LinkedHashSet<>(defaults.getAuthMethods()));
  }

  /**
   * Allowed OAuth response types for new registrations.
   *
   * @return immutable allow-list
   */
  public Set<String> allowedResponseTypes() {
    return Set.copyOf(new LinkedHashSet<>(allowed.getResponseTypes()));
  }

  /**
   * Allowed authorization grant types for new registrations.
   *
   * @return immutable allow-list
   */
  public Set<String> allowedGrantTypes() {
    return Set.copyOf(new LinkedHashSet<>(allowed.getGrantTypes()));
  }

  /**
   * Allowed client authentication methods for new registrations.
   *
   * @return immutable allow-list
   */
  public Set<String> allowedAuthMethods() {
    return Set.copyOf(new LinkedHashSet<>(allowed.getAuthMethods()));
  }

  /** Default protocol values for new and seeded clients. */
  public static class ProtocolDefaults {
    private List<String> scopes = new ArrayList<>(List.of("openid", "profile", "email"));
    private List<String> responseTypes = new ArrayList<>(List.of("code"));
    private List<String> grantTypes =
        new ArrayList<>(List.of("authorization_code", "refresh_token"));
    private List<String> authMethods =
        new ArrayList<>(List.of("client_secret_basic", "client_secret_post"));

    public List<String> getScopes() {
      return scopes;
    }

    /**
     * Replaces default scopes when the list is non-empty.
     *
     * @param scopes configured scopes
     */
    public void setScopes(List<String> scopes) {
      if (scopes != null && !scopes.isEmpty()) {
        this.scopes = new ArrayList<>(scopes);
      }
    }

    public List<String> getResponseTypes() {
      return responseTypes;
    }

    /**
     * Replaces default response types when the list is non-empty.
     *
     * @param responseTypes configured response types
     */
    public void setResponseTypes(List<String> responseTypes) {
      if (responseTypes != null && !responseTypes.isEmpty()) {
        this.responseTypes = new ArrayList<>(responseTypes);
      }
    }

    public List<String> getGrantTypes() {
      return grantTypes;
    }

    /**
     * Replaces default grant types when the list is non-empty.
     *
     * @param grantTypes configured grant types
     */
    public void setGrantTypes(List<String> grantTypes) {
      if (grantTypes != null && !grantTypes.isEmpty()) {
        this.grantTypes = new ArrayList<>(grantTypes);
      }
    }

    public List<String> getAuthMethods() {
      return authMethods;
    }

    /**
     * Replaces default auth methods when the list is non-empty.
     *
     * @param authMethods configured auth methods
     */
    public void setAuthMethods(List<String> authMethods) {
      if (authMethods != null && !authMethods.isEmpty()) {
        this.authMethods = new ArrayList<>(authMethods);
      }
    }
  }

  /** Allow-lists for values accepted during interactive registration. */
  public static class ProtocolAllowed {
    private List<String> responseTypes = new ArrayList<>(List.of("code"));
    private List<String> grantTypes =
        new ArrayList<>(List.of("authorization_code", "refresh_token"));
    private List<String> authMethods =
        new ArrayList<>(List.of("client_secret_basic", "client_secret_post", "none"));

    public List<String> getResponseTypes() {
      return responseTypes;
    }

    /**
     * Replaces allowed response types when the list is non-empty.
     *
     * @param responseTypes configured response types
     */
    public void setResponseTypes(List<String> responseTypes) {
      if (responseTypes != null && !responseTypes.isEmpty()) {
        this.responseTypes = new ArrayList<>(responseTypes);
      }
    }

    public List<String> getGrantTypes() {
      return grantTypes;
    }

    /**
     * Replaces allowed grant types when the list is non-empty.
     *
     * @param grantTypes configured grant types
     */
    public void setGrantTypes(List<String> grantTypes) {
      if (grantTypes != null && !grantTypes.isEmpty()) {
        this.grantTypes = new ArrayList<>(grantTypes);
      }
    }

    public List<String> getAuthMethods() {
      return authMethods;
    }

    /**
     * Replaces allowed auth methods when the list is non-empty.
     *
     * @param authMethods configured auth methods
     */
    public void setAuthMethods(List<String> authMethods) {
      if (authMethods != null && !authMethods.isEmpty()) {
        this.authMethods = new ArrayList<>(authMethods);
      }
    }
  }

  /** One bootstrap OIDC client bound from configuration. */
  public static class SeedClient {
    private String clientId;
    private String clientName;
    private String clientSecret;
    private List<String> redirectUris = new ArrayList<>();
    private List<String> postLogoutRedirectUris = new ArrayList<>();
    private List<String> scopes = new ArrayList<>();

    public String getClientId() {
      return clientId;
    }

    public void setClientId(String clientId) {
      this.clientId = clientId;
    }

    public String getClientName() {
      return clientName;
    }

    public void setClientName(String clientName) {
      this.clientName = clientName;
    }

    public String getClientSecret() {
      return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
    }

    public List<String> getRedirectUris() {
      return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
      this.redirectUris = redirectUris == null ? new ArrayList<>() : redirectUris;
    }

    public List<String> getPostLogoutRedirectUris() {
      return postLogoutRedirectUris;
    }

    public void setPostLogoutRedirectUris(List<String> postLogoutRedirectUris) {
      this.postLogoutRedirectUris =
          postLogoutRedirectUris == null ? new ArrayList<>() : postLogoutRedirectUris;
    }

    public List<String> getScopes() {
      return scopes;
    }

    public void setScopes(List<String> scopes) {
      this.scopes = scopes == null ? new ArrayList<>() : scopes;
    }
  }
}
