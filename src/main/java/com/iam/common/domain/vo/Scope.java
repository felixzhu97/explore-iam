package com.iam.common.domain.vo;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * OAuth scope value: OIDC standard scopes ({@code openid}, {@code profile}, {@code email}) or
 * GitHub-style {@code {access}:{resource}} (e.g. {@code write:ai_chat}).
 */
public record Scope(String value) {

  private static final Pattern GITHUB_STYLE =
      Pattern.compile("^(read|write|admin|delete):[a-z][a-z0-9_]*$");
  private static final Set<String> OIDC_SCOPES = Set.of("openid", "profile", "email");

  /**
   * Validates and normalizes a scope string.
   *
   * @param value raw scope
   */
  public Scope {
    Objects.requireNonNull(value, "value");
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("scope must not be blank");
    }
    if (!OIDC_SCOPES.contains(trimmed) && !GITHUB_STYLE.matcher(trimmed).matches()) {
      throw new IllegalArgumentException(
          "scope must be openid, profile, email, or GitHub style {access}:{resource}"
              + " (e.g. write:ai_chat, admin:chat)");
    }
    value = trimmed;
  }

  /**
   * Creates a scope from a raw string.
   *
   * @param raw raw scope
   * @return scope VO
   */
  public static Scope of(String raw) {
    return new Scope(raw);
  }

  /** Returns true when this is an OIDC standard scope. */
  public boolean isOidcStandard() {
    return OIDC_SCOPES.contains(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
