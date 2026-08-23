package com.iam.federation.mapper;

import com.iam.federation.domain.model.OidcClient;
import com.iam.federation.domain.vo.ClientId;
import com.iam.federation.domain.vo.RedirectUri;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.StringUtils;

/** Maps OIDC client domain objects to Spring Authorization Server types. */
public final class OidcClientMapper {

  static final String SETTING_CLIENT_URI = "settings.client.client-uri";
  static final String SETTING_RESPONSE_TYPES = "settings.client.response-types";

  private OidcClientMapper() {}

  /** Converts a domain OIDC client to a registered client. */
  public static RegisteredClient toRegisteredClient(OidcClient client) {
    ClientSettings.Builder settings =
        ClientSettings.builder().requireAuthorizationConsent(client.requiresAuthorizationConsent());
    if (StringUtils.hasText(client.clientUri())) {
      settings.setting(SETTING_CLIENT_URI, client.clientUri());
    }
    if (!client.responseTypes().isEmpty()) {
      settings.setting(SETTING_RESPONSE_TYPES, String.join(",", client.responseTypes()));
    }

    RegisteredClient.Builder builder =
        RegisteredClient.withId(client.id())
            .clientId(client.clientId().value())
            .clientIdIssuedAt(client.clientIdIssuedAt())
            .clientName(client.clientName())
            .clientSettings(settings.build())
            .tokenSettings(TokenSettings.builder().build());

    if (StringUtils.hasText(client.storedSecretHash())) {
      builder.clientSecret(client.storedSecretHash());
    }

    client
        .clientAuthenticationMethods()
        .forEach(
            method -> builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method)));
    client
        .authorizationGrantTypes()
        .forEach(grant -> builder.authorizationGrantType(new AuthorizationGrantType(grant)));
    client.redirectUris().forEach(uri -> builder.redirectUri(uri.value()));
    client.postLogoutRedirectUris().forEach(uri -> builder.postLogoutRedirectUri(uri.value()));
    client.scopes().forEach(builder::scope);
    return builder.build();
  }

  /** Converts a registered client to a domain OIDC client. */
  public static OidcClient toDomain(RegisteredClient registeredClient) {
    ClientSettings settings = registeredClient.getClientSettings();
    String clientUri = settings.getSetting(SETTING_CLIENT_URI);
    Set<String> responseTypes = splitCsv(settings.getSetting(SETTING_RESPONSE_TYPES));
    if (responseTypes.isEmpty()) {
      responseTypes = Set.of("code");
    }

    return OidcClient.reconstitute(
        registeredClient.getId(),
        new ClientId(registeredClient.getClientId()),
        registeredClient.getClientIdIssuedAt() == null
            ? Instant.now()
            : registeredClient.getClientIdIssuedAt(),
        registeredClient.getClientName(),
        registeredClient.getClientSecret() == null ? null : registeredClient.getClientSecret(),
        StringUtils.hasText(clientUri) ? clientUri : null,
        registeredClient.getRedirectUris().stream()
            .map(RedirectUri::new)
            .collect(Collectors.toCollection(LinkedHashSet::new)),
        registeredClient.getPostLogoutRedirectUris().stream()
            .map(RedirectUri::new)
            .collect(Collectors.toCollection(LinkedHashSet::new)),
        new LinkedHashSet<>(registeredClient.getScopes()),
        responseTypes,
        registeredClient.getClientAuthenticationMethods().stream()
            .map(ClientAuthenticationMethod::getValue)
            .collect(Collectors.toCollection(LinkedHashSet::new)),
        registeredClient.getAuthorizationGrantTypes().stream()
            .map(AuthorizationGrantType::getValue)
            .collect(Collectors.toCollection(LinkedHashSet::new)),
        settings.isRequireAuthorizationConsent());
  }

  static Set<String> splitCsv(String csv) {
    if (!StringUtils.hasText(csv)) {
      return Set.of();
    }
    return Arrays.stream(StringUtils.commaDelimitedListToStringArray(csv))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
