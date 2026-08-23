package com.iam.federation.infra.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

/** Configures access and refresh token lifetimes for the Authorization Server. */
@Configuration
public class AuthorizationServerTokenSettingsConfig {

  @Bean
  TokenSettings tokenSettings(
      @Value("${app.security.token.access-token-ttl:PT15M}") Duration accessTokenTtl) {
    return TokenSettings.builder()
        .accessTokenTimeToLive(accessTokenTtl)
        .refreshTokenTimeToLive(Duration.ofHours(24))
        .build();
  }

  @Bean
  AuthorizationServerSettings authorizationServerSettings(
      @Value("${spring.security.oauth2.authorizationserver.issuer}") String issuer) {
    return AuthorizationServerSettings.builder().issuer(issuer).build();
  }
}
