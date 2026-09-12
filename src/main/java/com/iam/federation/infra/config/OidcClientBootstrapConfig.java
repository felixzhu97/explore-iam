package com.iam.federation.infra.config;

import com.iam.federation.domain.model.OidcClient;
import com.iam.federation.domain.repository.OidcClientRepository;
import com.iam.federation.domain.vo.ClientId;
import com.iam.federation.domain.vo.RedirectUri;
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
public class OidcClientBootstrapConfig {

  private static final Logger log = LoggerFactory.getLogger(OidcClientBootstrapConfig.class);

  @Bean
  ApplicationRunner seedOidcClients(
      OidcClientRepository oidcClientRepository,
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
        var existing = oidcClientRepository.findByClientId(clientId);
        if (existing.isPresent()) {
          OidcClient client = existing.get();
          if (!client.scopes().equals(desiredScopes)) {
            client.replaceScopes(desiredScopes);
            oidcClientRepository.save(client);
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
        OidcClient client =
            seed.isPublicClient()
                ? OidcClient.seedPublic(
                    clientId, seed.getClientName(), redirectUris, postLogout, desiredScopes)
                : OidcClient.seed(
                    clientId,
                    seed.getClientName(),
                    passwordEncoder.encode(seed.getClientSecret()),
                    redirectUris,
                    postLogout,
                    desiredScopes);
        oidcClientRepository.save(client);
        log.info(
            "Seeded OIDC client '{}' (public={})", clientId.value(), seed.isPublicClient());
      }
    };
  }
}
