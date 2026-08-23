package com.iam.federation.infra.config;

import com.iam.federation.infra.persistence.JdbcOidcClientRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/** Exposes Spring Authorization Server's {@link RegisteredClientRepository} bean. */
@Configuration
public class RegisteredClientConfig {

  @Bean
  RegisteredClientRepository registeredClientRepository(
      JdbcOidcClientRepository oidcClientRepository) {
    return oidcClientRepository.getRegisteredClientRepository();
  }
}
