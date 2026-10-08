package com.iam.federation.infra.config;

import com.iam.federation.domain.model.ClientId;
import com.iam.federation.domain.model.OAuthClient;
import com.iam.federation.domain.model.RedirectUri;
import com.iam.federation.domain.repository.OAuthClientRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/** Seeds configured OIDC clients on application startup (idempotent scope merge). */
@Configuration
@EnableConfigurationProperties(OidcSeedClientProperties.class)
public class OAuthClientBootstrapConfig {

  private static final Logger log = LoggerFactory.getLogger(OAuthClientBootstrapConfig.class);

  @Bean
  ApplicationRunner seedOAuthClients(
      OAuthClientRepository oauthClientRepository,
      PasswordEncoder passwordEncoder,
      OidcSeedClientProperties properties) {
    return args -> {
      for (OidcSeedClientProperties.SeedClient seed : properties.getSeedClients()) {
        if (!StringUtils.hasText(seed.getClientId())) {
          log.warn("Skipping OIDC seed client with blank client-id");
          continue;
        }
        if (!seed.isPublicClient() && !StringUtils.hasText(seed.getClientSecret())) {
          log.warn(
              "Skipping confidential OIDC seed client '{}' with blank client-secret",
              seed.getClientId());
          continue;
        }
        ClientId clientId = new ClientId(seed.getClientId());
        Set<String> desiredScopes =
            seed.getScopes().isEmpty()
                ? properties.defaultScopes()
                : new LinkedHashSet<>(seed.getScopes());
        var existing = oauthClientRepository.findByClientId(clientId);
        if (existing.isPresent()) {
          OAuthClient client = existing.get();
          if (!client.scopes().equals(desiredScopes)) {
            client.replaceScopes(desiredScopes);
            oauthClientRepository.save(client);
            log.info("Updated OIDC client '{}' scopes", clientId.value());
          }
          continue;
        }
        Set<RedirectUri> redirectUris =
            seed.getRedirectUris().stream()
                .map(RedirectUri::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<RedirectUri> postLogout =
            seed.getPostLogoutRedirectUris().stream()
                .map(RedirectUri::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        OAuthClient client =
            seed.isPublicClient()
                ? OAuthClient.createPublicBootstrapOAuthClient(
                    clientId, seed.getClientName(), redirectUris, postLogout, desiredScopes)
                : OAuthClient.createBootstrapOAuthClient(
                    clientId,
                    seed.getClientName(),
                    passwordEncoder.encode(seed.getClientSecret()),
                    redirectUris,
                    postLogout,
                    desiredScopes);
        oauthClientRepository.save(client);
        log.info(
            "Seeded OIDC client '{}' (public={})", clientId.value(), seed.isPublicClient());
      }
    };
  }
}
