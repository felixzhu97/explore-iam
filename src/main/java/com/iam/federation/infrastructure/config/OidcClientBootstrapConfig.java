package com.iam.federation.infrastructure.config;

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
                if (!StringUtils.hasText(seed.getClientId()) || !StringUtils.hasText(seed.getClientSecret())) {
                    log.warn("Skipping OIDC seed client with blank client-id or client-secret");
                    continue;
                }
                ClientId clientId = new ClientId(seed.getClientId());
                if (oidcClientRepository.findByClientId(clientId).isPresent()) {
                    continue;
                }
                Set<RedirectUri> redirectUris = seed.getRedirectUris().stream()
                        .map(RedirectUri::new)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                Set<RedirectUri> postLogout = seed.getPostLogoutRedirectUris().stream()
                        .map(RedirectUri::new)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                Set<String> scopes = seed.getScopes().isEmpty()
                        ? properties.defaultScopes()
                        : new LinkedHashSet<>(seed.getScopes());
                OidcClient client = OidcClient.seed(
                        clientId,
                        seed.getClientName(),
                        passwordEncoder.encode(seed.getClientSecret()),
                        redirectUris,
                        postLogout,
                        scopes);
                oidcClientRepository.save(client);
                log.info("Seeded OIDC client '{}'", clientId.value());
            }
        };
    }
}
