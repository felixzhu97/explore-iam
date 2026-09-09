package com.iam.federation.domain.vo;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * Absolute redirect URI for an OIDC Relying Party. Allows {@code http}/{@code https} and native
 * custom schemes (for example {@code com.explore.ai://oauth/callback}). Fragments and dangerous
 * schemes are rejected.
 */
public record RedirectUri(String value) {

  /**
   * Validates an absolute redirect URI without a fragment.
   *
   * @param value raw redirect_uri
   */
  public RedirectUri {
    Objects.requireNonNull(value, "value");
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("redirect_uri must not be blank");
    }
    URI uri;
    try {
      uri = URI.create(trimmed);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("redirect_uri is not a valid URI: " + trimmed, ex);
    }
    if (uri.getScheme() == null) {
      throw new IllegalArgumentException("redirect_uri must be absolute: " + trimmed);
    }
    String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
    if ("http".equals(scheme) || "https".equals(scheme)) {
      if (uri.getHost() == null || uri.getHost().isBlank()) {
        throw new IllegalArgumentException("redirect_uri must include a host: " + trimmed);
      }
    } else if (!isAllowedCustomScheme(scheme)) {
      throw new IllegalArgumentException(
          "redirect_uri scheme must be http, https, or a reverse-DNS custom scheme: " + trimmed);
    }
    if (uri.getFragment() != null) {
      throw new IllegalArgumentException("redirect_uri must not contain a fragment: " + trimmed);
    }
    value = trimmed;
  }

  private static boolean isAllowedCustomScheme(String scheme) {
    if ("javascript".equals(scheme)
        || "data".equals(scheme)
        || "file".equals(scheme)
        || "about".equals(scheme)) {
      return false;
    }
    // Native app schemes: com.explore.ai, com.explore.chat, …
    return scheme.contains(".") && scheme.chars().allMatch(ch ->
        (ch >= 'a' && ch <= 'z')
            || (ch >= '0' && ch <= '9')
            || ch == '.'
            || ch == '+'
            || ch == '-');
  }

  @Override
  public String toString() {
    return value;
  }
}
