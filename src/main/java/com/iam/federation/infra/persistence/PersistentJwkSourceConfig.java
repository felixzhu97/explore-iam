package com.iam.federation.infra.persistence;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads or generates a persistent RSA JWK for the OIDC Authorization Server.
 *
 * <p>Keys are stored in {@code iam_signing_keys} so tokens survive restarts.
 */
@Configuration
public class PersistentJwkSourceConfig {

  private final SpringDataSigningKeyRepository signingKeyRepository;

  PersistentJwkSourceConfig(SpringDataSigningKeyRepository signingKeyRepository) {
    this.signingKeyRepository = signingKeyRepository;
  }

  @Bean
  @Transactional
  JWKSource<SecurityContext> jwkSource() {
    SigningKeyEntity entity =
        signingKeyRepository
            .findFirstByOrderByCreatedAtDesc()
            .orElseGet(this::generateAndPersistKey);
    RSAKey rsaKey = toRsaKey(entity);
    return new ImmutableJWKSet<>(new JWKSet(rsaKey));
  }

  private SigningKeyEntity generateAndPersistKey() {
    KeyPair keyPair = generateRsaKey();
    String publicPem = toPem(keyPair.getPublic().getEncoded(), "PUBLIC KEY");
    String privatePem = toPem(keyPair.getPrivate().getEncoded(), "PRIVATE KEY");
    SigningKeyEntity entity =
        new SigningKeyEntity(UUID.randomUUID().toString(), publicPem, privatePem, Instant.now());
    return signingKeyRepository.save(entity);
  }

  private RSAKey toRsaKey(SigningKeyEntity entity) {
    try {
      RSAPublicKey publicKey = parsePublicKey(entity.getPublicKeyPem());
      RSAPrivateKey privateKey = parsePrivateKey(entity.getPrivateKeyPem());
      return new RSAKey.Builder(publicKey)
          .privateKey(privateKey)
          .keyID(entity.getId())
          .build();
    } catch (Exception ex) {
      throw new IllegalStateException("failed to load signing key", ex);
    }
  }

  private static KeyPair generateRsaKey() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      return generator.generateKeyPair();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static String toPem(byte[] encoded, String label) {
    String base64 = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(encoded);
    return "-----BEGIN " + label + "-----\n" + base64 + "\n-----END " + label + "-----";
  }

  private static RSAPublicKey parsePublicKey(String pem) throws Exception {
    byte[] decoded = decodePem(pem);
    KeyFactory factory = KeyFactory.getInstance("RSA");
    return (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(decoded));
  }

  private static RSAPrivateKey parsePrivateKey(String pem) throws Exception {
    byte[] decoded = decodePem(pem);
    KeyFactory factory = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
  }

  private static byte[] decodePem(String pem) {
    String stripped =
        pem.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    return Base64.getDecoder().decode(stripped);
  }
}
