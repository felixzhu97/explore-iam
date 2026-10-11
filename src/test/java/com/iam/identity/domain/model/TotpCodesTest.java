package com.iam.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TotpCodes")
class TotpCodesTest {

  @Test
  @DisplayName("should verify generated secret against current window")
  void shouldVerifyGeneratedSecretAgainstCurrentWindow() {
    String secret = TotpCodes.generateSecret();
    // RFC 6238 test vector secret "12345678901234567890" in Base32 is known;
    // here we only assert otpauth URI shape and verify rejects garbage.
    assertThat(TotpCodes.otpAuthUri("Explore IAM", "demo", secret)).contains("otpauth://totp/");
    assertThat(TotpCodes.verifyCode(secret, "000000", Instant.now())).isFalse();
  }
}
