package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractEntity;
import com.iam.common.domain.base.DomainStrings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Authenticator-app TOTP credential (RFC 6238) for a single user. */
@Entity
@Table(name = "totp_credential")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TotpCredential extends AbstractEntity {

  @Column(nullable = false, unique = true, length = 36)
  private String userId;

  @Column(nullable = false, length = 512)
  private String secretCiphertext;

  @Column(nullable = false)
  private boolean enabled;

  @Column(nullable = false, length = 2048)
  private String backupCodeHashes;

  private TotpCredential(
      String id,
      String userId,
      String secretCiphertext,
      boolean enabled,
      String backupCodeHashes,
      Instant createdAt,
      Instant updatedAt) {
    super(id, createdAt, updatedAt);
    this.userId = DomainStrings.requireNonBlank(userId, "userId");
    this.secretCiphertext = DomainStrings.requireNonBlank(secretCiphertext, "secretCiphertext");
    this.enabled = enabled;
    this.backupCodeHashes = DomainStrings.requireNonBlank(backupCodeHashes, "backupCodeHashes");
  }

  /**
   * Creates a disabled TOTP credential pending confirmation.
   *
   * @param userId owning user id
   * @param secretCiphertext encrypted Base32 secret
   * @param backupCodeHashes hashed backup codes (comma-separated)
   * @return new aggregate
   */
  public static TotpCredential createTotpCredential(
      String userId, String secretCiphertext, String backupCodeHashes) {
    Instant now = Instant.now();
    return new TotpCredential(
        UUID.randomUUID().toString(),
        userId,
        secretCiphertext,
        false,
        backupCodeHashes,
        now,
        now);
  }

  /** Enables TOTP after the user confirms a valid code. */
  public void enableTotp() {
    this.enabled = true;
    touch();
  }

  /** Disables TOTP for the user. */
  public void disableTotp() {
    this.enabled = false;
    touch();
  }

  /**
   * Replaces the encrypted secret and backup hashes (re-enrollment).
   *
   * @param secretCiphertext new ciphertext
   * @param backupCodeHashes new hashed backup codes
   */
  public void rotateSecret(String secretCiphertext, String backupCodeHashes) {
    this.secretCiphertext = DomainStrings.requireNonBlank(secretCiphertext, "secretCiphertext");
    this.backupCodeHashes = DomainStrings.requireNonBlank(backupCodeHashes, "backupCodeHashes");
    this.enabled = false;
    touch();
  }

  /**
   * Updates remaining backup code hashes after one is consumed.
   *
   * @param backupCodeHashes remaining hashes
   */
  public void replaceBackupCodeHashes(String backupCodeHashes) {
    this.backupCodeHashes = DomainStrings.requireNonBlank(backupCodeHashes, "backupCodeHashes");
    touch();
  }
}
