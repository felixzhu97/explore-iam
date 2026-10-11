package com.iam.identity.service;

import com.iam.identity.domain.model.TotpCodes;
import com.iam.identity.domain.model.TotpCredential;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.TotpCredentialRepository;
import com.iam.identity.domain.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Enrollment and verification for authenticator-app TOTP. */
@Service
@RequiredArgsConstructor
public class TotpService {

  private static final int BACKUP_COUNT = 8;

  private final TotpCredentialRepository totpCredentialRepository;
  private final UserRepository userRepository;
  private final TextEncryptor totpSecretEncryptor;
  private final PasswordEncoder passwordEncoder;

  /**
   * Starts enrollment for the user; returns plaintext secret and backup codes once.
   *
   * @param username directory username
   * @return enrollment material
   */
  @Transactional
  public TotpEnrollment beginEnrollment(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    String secret = TotpCodes.generateSecret();
    List<String> backupCodes = generateBackupCodes();
    String hashes =
        String.join(
            ",", backupCodes.stream().map(passwordEncoder::encode).toList());
    TotpCredential credential =
        totpCredentialRepository
            .findByUserId(user.getId())
            .map(
                existing -> {
                  existing.rotateSecret(totpSecretEncryptor.encrypt(secret), hashes);
                  return existing;
                })
            .orElseGet(
                () ->
                    TotpCredential.createTotpCredential(
                        user.getId(), totpSecretEncryptor.encrypt(secret), hashes));
    totpCredentialRepository.save(credential);
    return new TotpEnrollment(
        secret,
        TotpCodes.otpAuthUri("Explore IAM", user.getUsername(), secret),
        backupCodes);
  }

  /**
   * Confirms enrollment with a TOTP code and enables the credential.
   *
   * @param username directory username
   * @param code six-digit code
   */
  @Transactional
  public void confirmEnrollment(String username, String code) {
    TotpCredential credential = requireCredential(username);
    String secret = totpSecretEncryptor.decrypt(credential.getSecretCiphertext());
    if (!TotpCodes.verifyCode(secret, code, Instant.now())) {
      throw new IllegalArgumentException("Invalid TOTP code");
    }
    credential.enableTotp();
    totpCredentialRepository.save(credential);
  }

  /**
   * Verifies a login challenge code for an enabled credential.
   *
   * @param username directory username
   * @param code TOTP or backup code
   * @return true when verification succeeds
   */
  @Transactional
  public boolean verifyChallenge(String username, String code) {
    Optional<TotpCredential> found =
        userRepository
            .findByUsername(username)
            .flatMap(user -> totpCredentialRepository.findByUserId(user.getId()));
    if (found.isEmpty() || !found.get().isEnabled()) {
      return false;
    }
    TotpCredential credential = found.get();
    String secret = totpSecretEncryptor.decrypt(credential.getSecretCiphertext());
    if (TotpCodes.verifyCode(secret, code, Instant.now())) {
      return true;
    }
    return consumeBackupCode(credential, code);
  }

  /**
   * Whether the user has an enabled TOTP credential.
   *
   * @param username directory username
   * @return true when enabled
   */
  @Transactional(readOnly = true)
  public boolean isEnabled(String username) {
    return userRepository
        .findByUsername(username)
        .flatMap(user -> totpCredentialRepository.findByUserId(user.getId()))
        .map(TotpCredential::isEnabled)
        .orElse(false);
  }

  private boolean consumeBackupCode(TotpCredential credential, String code) {
    String[] hashes = credential.getBackupCodeHashes().split(",");
    List<String> remaining = new ArrayList<>();
    boolean matched = false;
    for (String hash : hashes) {
      if (!matched && passwordEncoder.matches(code, hash)) {
        matched = true;
        continue;
      }
      remaining.add(hash);
    }
    if (!matched) {
      return false;
    }
    credential.replaceBackupCodeHashes(
        remaining.isEmpty() ? passwordEncoder.encode("exhausted") : String.join(",", remaining));
    totpCredentialRepository.save(credential);
    return true;
  }

  private TotpCredential requireCredential(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return totpCredentialRepository
        .findByUserId(user.getId())
        .orElseThrow(() -> new IllegalArgumentException("TOTP not started"));
  }

  private static List<String> generateBackupCodes() {
    SecureRandom random = new SecureRandom();
    List<String> codes = new ArrayList<>(BACKUP_COUNT);
    for (int i = 0; i < BACKUP_COUNT; i++) {
      codes.add(String.format(Locale.ROOT, "%08d", random.nextInt(100_000_000)));
    }
    return codes;
  }

  /** One-time enrollment payload. */
  public record TotpEnrollment(String secret, String otpAuthUri, List<String> backupCodes) {}
}
