package com.iam.federation.infra.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * JWT decoder for the OIDC Provider. Signing keys come from {@link PersistentJwkSourceConfig}.
 *
 * @see <a
 *     href="https://docs.spring.io/spring-boot/reference/web/spring-security.html#web.security.oauth2.authorization-server">Authorization
 *     Server</a>
 */
@Configuration
public class AuthorizationServerConfig {

  @Bean
  JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return NimbusJwtDecoder.withJwkSource(jwkSource).build();
  }
}
