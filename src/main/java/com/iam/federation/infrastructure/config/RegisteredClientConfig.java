package com.iam.federation.infrastructure.config;

import com.iam.federation.infrastructure.persistence.JdbcOidcClientRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
public class RegisteredClientConfig {

    @Bean
    RegisteredClientRepository registeredClientRepository(JdbcOidcClientRepository oidcClientRepository) {
        return oidcClientRepository.getRegisteredClientRepository();
    }
}
