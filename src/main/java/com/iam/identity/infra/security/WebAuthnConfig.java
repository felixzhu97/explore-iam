package com.iam.identity.infra.security;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AllRequiredFactorsAuthorizationManager;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;

/**
 * JDBC-backed Passkey repositories and MFA combination PASSWORD + WEBAUTHN.
 *
 * @see <a
 *     href="https://docs.spring.io/spring-security/reference/servlet/authentication/passkeys.html">Passkeys</a>
 */
@Configuration
class WebAuthnConfig {

  WebAuthnConfig(MfaAuthorizationManagers mfa) {
    mfa.addCombination(
        AllRequiredFactorsAuthorizationManager.<Object>builder()
            .requireFactor((factor) -> factor.passwordAuthority())
            .requireFactor((factor) -> factor.webauthnAuthority())
            .build());
  }

  @Bean
  PublicKeyCredentialUserEntityRepository publicKeyCredentialUserEntityRepository(
      DataSource dataSource) {
    JdbcOperations jdbc = new JdbcTemplate(dataSource);
    return new JdbcPublicKeyCredentialUserEntityRepository(jdbc);
  }

  @Bean
  UserCredentialRepository userCredentialRepository(DataSource dataSource) {
    JdbcOperations jdbc = new JdbcTemplate(dataSource);
    return new JdbcUserCredentialRepository(jdbc);
  }

  @Bean
  WebAuthnProperties webAuthnProperties(
      @Value("${app.security.webauthn.rp-id:localhost}") String rpId,
      @Value("${app.security.webauthn.rp-name:Explore IAM}") String rpName,
      @Value("${app.security.webauthn.allowed-origins:http://localhost:9100}")
          String allowedOrigins) {
    return new WebAuthnProperties(rpId, rpName, allowedOrigins.split("\\s*,\\s*"));
  }

  record WebAuthnProperties(String rpId, String rpName, String[] allowedOrigins) {}
}
