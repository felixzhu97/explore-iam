package com.iam.identity.domain.model;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * RFC 6238 TOTP helpers (HMAC-SHA1, 30s step, 6 digits).
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6238">RFC 6238</a>
 */
public final class TotpCodes {

  private static final int STEP_SECONDS = 30;
  private static final int DIGITS = 6;
  private static final String ALGO = "HmacSHA1";
  private static final char[] BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

  private TotpCodes() {}

  /**
   * Generates a new Base32-encoded secret.
   *
   * @return secret
   */
  public static String generateSecret() {
    byte[] bytes = new byte[20];
    new SecureRandom().nextBytes(bytes);
    return encodeBase32(bytes);
  }

  /**
   * Verifies a user-entered code against the secret (±1 time step).
   *
   * @param secretBase32 shared secret
   * @param code six-digit code
   * @param now verification instant
   * @return true when valid
   */
  public static boolean verifyCode(String secretBase32, String code, Instant now) {
    if (secretBase32 == null || code == null || !code.matches("\\d{6}")) {
      return false;
    }
    long counter = now.getEpochSecond() / STEP_SECONDS;
    byte[] key = decodeBase32(secretBase32);
    for (long skew = -1; skew <= 1; skew++) {
      if (code.equals(formatCode(hotp(key, counter + skew)))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Builds an otpauth URI for authenticator enrollment.
   *
   * @param issuer issuer label
   * @param accountName account (usually username)
   * @param secretBase32 secret
   * @return otpauth URI
   */
  public static String otpAuthUri(String issuer, String accountName, String secretBase32) {
    return "otpauth://totp/"
        + urlEncode(issuer)
        + ":"
        + urlEncode(accountName)
        + "?secret="
        + secretBase32
        + "&issuer="
        + urlEncode(issuer)
        + "&digits="
        + DIGITS
        + "&period="
        + STEP_SECONDS;
  }

  private static int hotp(byte[] key, long counter) {
    try {
      Mac mac = Mac.getInstance(ALGO);
      mac.init(new SecretKeySpec(key, ALGO));
      byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
      int offset = hash[hash.length - 1] & 0x0f;
      int binary =
          ((hash[offset] & 0x7f) << 24)
              | ((hash[offset + 1] & 0xff) << 16)
              | ((hash[offset + 2] & 0xff) << 8)
              | (hash[offset + 3] & 0xff);
      return binary % 1_000_000;
    } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
      throw new IllegalStateException("TOTP HMAC failed", ex);
    }
  }

  private static String formatCode(int value) {
    return String.format(Locale.ROOT, "%0" + DIGITS + "d", value);
  }

  private static String encodeBase32(byte[] data) {
    StringBuilder out = new StringBuilder((data.length * 8 + 4) / 5);
    int buffer = 0;
    int bitsLeft = 0;
    for (byte b : data) {
      buffer = (buffer << 8) | (b & 0xff);
      bitsLeft += 8;
      while (bitsLeft >= 5) {
        out.append(BASE32[(buffer >> (bitsLeft - 5)) & 31]);
        bitsLeft -= 5;
      }
    }
    if (bitsLeft > 0) {
      out.append(BASE32[(buffer << (5 - bitsLeft)) & 31]);
    }
    return out.toString();
  }

  private static byte[] decodeBase32(String encoded) {
    String normalized = encoded.trim().toUpperCase(Locale.ROOT).replace("=", "");
    ByteBuffer buffer = ByteBuffer.allocate((normalized.length() * 5) / 8);
    int bits = 0;
    int value = 0;
    for (int i = 0; i < normalized.length(); i++) {
      int idx = indexOfBase32(normalized.charAt(i));
      if (idx < 0) {
        throw new IllegalArgumentException("Invalid Base32 secret");
      }
      value = (value << 5) | idx;
      bits += 5;
      if (bits >= 8) {
        buffer.put((byte) ((value >> (bits - 8)) & 0xff));
        bits -= 8;
      }
    }
    byte[] result = new byte[buffer.position()];
    buffer.rewind();
    buffer.get(result);
    return result;
  }

  private static int indexOfBase32(char c) {
    if (c >= 'A' && c <= 'Z') {
      return c - 'A';
    }
    if (c >= '2' && c <= '7') {
      return 26 + (c - '2');
    }
    return -1;
  }

  private static String urlEncode(String value) {
    return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8)
        .replace("+", "%20");
  }
}
