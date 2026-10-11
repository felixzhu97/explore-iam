package com.iam.identity.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

/** Encrypts TOTP shared secrets at rest. */
@Configuration
class TotpSecretEncryptorConfig {

  @Bean
  TextEncryptor totpSecretEncryptor(
      @Value("${app.security.totp.encrypt-password}") String password,
      @Value("${app.security.totp.encrypt-salt}") String salt) {
    if (!StringUtils.hasText(password) || !StringUtils.hasText(salt)) {
      throw new IllegalStateException(
          "Set app.security.totp.encrypt-password and app.security.totp.encrypt-salt"
              + " (e.g. APP_TOTP_ENCRYPT_PASSWORD / APP_TOTP_ENCRYPT_SALT).");
    }
    return Encryptors.text(password, salt);
  }
}
