package com.iam.federation.infra.persistence;

import com.iam.common.domain.base.AbstractImmutable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Persisted RSA signing key pair for the OIDC Authorization Server. */
@Entity
@Table(name = "iam_signing_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
class SigningKeyEntity extends AbstractImmutable {

  @Column(name = "public_key_pem", nullable = false, columnDefinition = "clob")
  private String publicKeyPem;

  @Column(name = "private_key_pem", nullable = false, columnDefinition = "clob")
  private String privateKeyPem;

  SigningKeyEntity(String id, String publicKeyPem, String privateKeyPem, Instant createdAt) {
    super(id, createdAt);
    this.publicKeyPem = publicKeyPem;
    this.privateKeyPem = privateKeyPem;
  }
}
