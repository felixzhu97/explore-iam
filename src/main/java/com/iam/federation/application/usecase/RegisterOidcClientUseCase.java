package com.iam.federation.application.usecase;

import com.iam.federation.domain.model.OidcClient;
import com.iam.federation.domain.repository.OidcClientRepository;
import com.iam.federation.domain.vo.RedirectUri;
import com.iam.federation.infrastructure.config.OidcSeedClientProperties;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Registers an OIDC Relying Party and returns the plaintext client secret once (confidential
 * clients only).
 */
@Service
public class RegisterOidcClientUseCase {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OidcClientRepository oidcClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final OidcSeedClientProperties oidcProperties;

    public RegisterOidcClientUseCase(
            OidcClientRepository oidcClientRepository,
            PasswordEncoder passwordEncoder,
            OidcSeedClientProperties oidcProperties) {
        this.oidcClientRepository = oidcClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.oidcProperties = oidcProperties;
    }

    @Transactional
    public RegisteredOidcClientResult execute(RegisterOidcClientCommand command) {
        Objects.requireNonNull(command, "command");
        Set<RedirectUri> redirectUris = toRedirectUris(command.redirectUris());
        Set<RedirectUri> postLogout = command.postLogoutRedirectUris() == null
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
            secretHash = this.passwordEncoder.encode(plaintextSecret);
        }

        OidcClient client = OidcClient.register(
                command.clientName(),
                secretHash,
                clientUri,
                redirectUris,
                postLogout,
                scopes,
                responseTypes,
                authMethods,
                grantTypes);
        OidcClient saved = this.oidcClientRepository.save(client);
        return RegisteredOidcClientResult.from(saved, plaintextSecret);
    }

    private Set<String> normalizeScopes(List<String> raw) {
        Set<String> scopes =
                raw == null || raw.isEmpty() ? this.oidcProperties.defaultScopes() : Set.copyOf(raw);
        if (!scopes.contains("openid")) {
            throw new IllegalArgumentException("scopes must include openid");
        }
        return scopes;
    }

    private Set<String> normalizeResponseTypes(List<String> raw) {
        Set<String> types = toNormalizedSet(raw, "responseTypes");
        if (types.isEmpty()) {
            types = this.oidcProperties.defaultResponseTypes();
        }
        Set<String> allowed = this.oidcProperties.allowedResponseTypes();
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
            grants = this.oidcProperties.defaultGrantTypes();
        }
        if (!grants.contains("authorization_code")) {
            throw new IllegalArgumentException("authorization_code grant type is required");
        }
        Set<String> allowed = this.oidcProperties.allowedGrantTypes();
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
            methods = this.oidcProperties.defaultAuthMethods();
        }
        Set<String> allowed = this.oidcProperties.allowedAuthMethods();
        for (String method : methods) {
            if (!allowed.contains(method)) {
                throw new IllegalArgumentException("unsupported client_authentication_method: " + method);
            }
        }
        if (methods.contains("none") && methods.size() > 1) {
            throw new IllegalArgumentException("none cannot be combined with other authentication methods");
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

    public record RegisterOidcClientCommand(
            String clientName,
            List<String> redirectUris,
            List<String> postLogoutRedirectUris,
            List<String> scopes,
            List<String> responseTypes,
            List<String> authorizationGrantTypes,
            List<String> clientAuthenticationMethods,
            String clientUri) {}

    public record RegisteredOidcClientResult(
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

        static RegisteredOidcClientResult from(OidcClient client, String plaintextSecret) {
            return new RegisteredOidcClientResult(
                    client.getId(),
                    client.getClientId().value(),
                    client.getClientName(),
                    plaintextSecret,
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
