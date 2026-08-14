package com.iam.federation.infrastructure.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

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

    public Set<String> defaultScopes() {
        return Set.copyOf(new LinkedHashSet<>(defaults.getScopes()));
    }

    public Set<String> defaultResponseTypes() {
        return Set.copyOf(new LinkedHashSet<>(defaults.getResponseTypes()));
    }

    public Set<String> defaultGrantTypes() {
        return Set.copyOf(new LinkedHashSet<>(defaults.getGrantTypes()));
    }

    public Set<String> defaultAuthMethods() {
        return Set.copyOf(new LinkedHashSet<>(defaults.getAuthMethods()));
    }

    public Set<String> allowedResponseTypes() {
        return Set.copyOf(new LinkedHashSet<>(allowed.getResponseTypes()));
    }

    public Set<String> allowedGrantTypes() {
        return Set.copyOf(new LinkedHashSet<>(allowed.getGrantTypes()));
    }

    public Set<String> allowedAuthMethods() {
        return Set.copyOf(new LinkedHashSet<>(allowed.getAuthMethods()));
    }

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

        public void setScopes(List<String> scopes) {
            if (scopes != null && !scopes.isEmpty()) {
                this.scopes = new ArrayList<>(scopes);
            }
        }

        public List<String> getResponseTypes() {
            return responseTypes;
        }

        public void setResponseTypes(List<String> responseTypes) {
            if (responseTypes != null && !responseTypes.isEmpty()) {
                this.responseTypes = new ArrayList<>(responseTypes);
            }
        }

        public List<String> getGrantTypes() {
            return grantTypes;
        }

        public void setGrantTypes(List<String> grantTypes) {
            if (grantTypes != null && !grantTypes.isEmpty()) {
                this.grantTypes = new ArrayList<>(grantTypes);
            }
        }

        public List<String> getAuthMethods() {
            return authMethods;
        }

        public void setAuthMethods(List<String> authMethods) {
            if (authMethods != null && !authMethods.isEmpty()) {
                this.authMethods = new ArrayList<>(authMethods);
            }
        }
    }

    public static class ProtocolAllowed {
        private List<String> responseTypes = new ArrayList<>(List.of("code"));
        private List<String> grantTypes =
                new ArrayList<>(List.of("authorization_code", "refresh_token"));
        private List<String> authMethods =
                new ArrayList<>(List.of("client_secret_basic", "client_secret_post", "none"));

        public List<String> getResponseTypes() {
            return responseTypes;
        }

        public void setResponseTypes(List<String> responseTypes) {
            if (responseTypes != null && !responseTypes.isEmpty()) {
                this.responseTypes = new ArrayList<>(responseTypes);
            }
        }

        public List<String> getGrantTypes() {
            return grantTypes;
        }

        public void setGrantTypes(List<String> grantTypes) {
            if (grantTypes != null && !grantTypes.isEmpty()) {
                this.grantTypes = new ArrayList<>(grantTypes);
            }
        }

        public List<String> getAuthMethods() {
            return authMethods;
        }

        public void setAuthMethods(List<String> authMethods) {
            if (authMethods != null && !authMethods.isEmpty()) {
                this.authMethods = new ArrayList<>(authMethods);
            }
        }
    }

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
