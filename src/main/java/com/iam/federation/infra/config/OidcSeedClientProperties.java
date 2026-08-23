package com.iam.federation.infra.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** OIDC protocol defaults, allow-lists, and bootstrap seed clients. */
@ConfigurationProperties(prefix = "app.oidc")
@Getter
@Setter
public class OidcSeedClientProperties {

  private ProtocolDefaults defaults = new ProtocolDefaults();
  private ProtocolAllowed allowed = new ProtocolAllowed();
  private List<SeedClient> seedClients = new ArrayList<>();

  public void setDefaults(ProtocolDefaults defaults) {
    this.defaults = defaults == null ? new ProtocolDefaults() : defaults;
  }

  public void setAllowed(ProtocolAllowed allowed) {
    this.allowed = allowed == null ? new ProtocolAllowed() : allowed;
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
  @Getter
  public static class ProtocolDefaults {
    private List<String> scopes = new ArrayList<>(List.of("openid", "profile", "email"));
    private List<String> responseTypes = new ArrayList<>(List.of("code"));
    private List<String> grantTypes =
        new ArrayList<>(List.of("authorization_code", "refresh_token"));
    private List<String> authMethods =
        new ArrayList<>(List.of("client_secret_basic", "client_secret_post"));

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
  @Getter
  public static class ProtocolAllowed {
    private List<String> responseTypes = new ArrayList<>(List.of("code"));
    private List<String> grantTypes =
        new ArrayList<>(List.of("authorization_code", "refresh_token"));
    private List<String> authMethods =
        new ArrayList<>(List.of("client_secret_basic", "client_secret_post", "none"));

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
  @Getter
  @Setter
  public static class SeedClient {
    private String clientId;
    private String clientName;
    private String clientSecret;
    private List<String> redirectUris = new ArrayList<>();
    private List<String> postLogoutRedirectUris = new ArrayList<>();
    private List<String> scopes = new ArrayList<>();

    public void setRedirectUris(List<String> redirectUris) {
      this.redirectUris = redirectUris == null ? new ArrayList<>() : redirectUris;
    }

    public void setPostLogoutRedirectUris(List<String> postLogoutRedirectUris) {
      this.postLogoutRedirectUris =
          postLogoutRedirectUris == null ? new ArrayList<>() : postLogoutRedirectUris;
    }

    public void setScopes(List<String> scopes) {
      this.scopes = scopes == null ? new ArrayList<>() : scopes;
    }
  }
}
