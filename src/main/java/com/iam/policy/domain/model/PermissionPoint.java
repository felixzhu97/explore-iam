package com.iam.policy.domain.model;

import com.iam.common.domain.base.AbstractImmutable;
import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Resource;
import com.iam.common.domain.vo.Scope;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Catalog entry linking an OAuth {@link Scope} to an {@link Action} and {@link Resource}.
 *
 * <p>Scope format matches GitHub OAuth scopes ({@code read:packages}, {@code admin:org}) except for
 * OIDC standard scopes {@code openid}, {@code profile}, {@code email}.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PermissionPoint extends AbstractImmutable {

  @NotBlank
  @Size(max = 128)
  @Column(nullable = false, unique = true, length = 128)
  private String code;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "oauth_scope", nullable = false, length = 128))
  @Valid
  private Scope oauthScope;

  @NotBlank
  @Size(max = 64)
  @Column(nullable = false, length = 64)
  private String module;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "action", nullable = false, length = 128))
  @Valid
  private Action action;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "resource", nullable = false, length = 512))
  @Valid
  private Resource resource;

  @Size(max = 512)
  @Column(nullable = false, length = 512)
  private String description;

  private PermissionPoint(
      String id,
      String code,
      Scope oauthScope,
      String module,
      Action action,
      Resource resource,
      String description,
      Instant createdAt) {
    super(id, createdAt);
    this.code = requireCode(code);
    this.module = requireModule(module);
    this.oauthScope = Objects.requireNonNull(oauthScope, "oauthScope");
    requireModuleCompatibleScope(this.module, this.oauthScope);
    if (!this.code.equals(this.oauthScope.value())) {
      throw new IllegalArgumentException("code must equal oauthScope");
    }
    this.action = Objects.requireNonNull(action, "action");
    this.resource = Objects.requireNonNull(resource, "resource");
    this.description = description == null ? "" : description.trim();
  }

  /**
   * Creates a permission point; {@code code} must equal {@code oauthScope}.
   *
   * @param code business key / oauth scope
   * @param oauthScope GitHub-style or OIDC scope
   * @param module product area ({@code ai}, {@code chat}, {@code oidc})
   * @param action IAM action
   * @param resource IAM resource
   * @param description human-readable purpose
   * @return new aggregate
   */
  public static PermissionPoint create(
      String code,
      String oauthScope,
      String module,
      Action action,
      Resource resource,
      String description) {
    return new PermissionPoint(
        UUID.randomUUID().toString(),
        code,
        Scope.of(oauthScope),
        module,
        action,
        resource,
        description,
        Instant.now());
  }

  /**
   * Returns true when the granted scope matches this catalog entry.
   *
   * @param scope oauth scope from a token
   * @return whether scopes match
   */
  public boolean matchesScope(String scope) {
    return oauthScope.value().equals(scope);
  }

  /** Returns the OAuth scope string for access tokens. */
  public String oauthScopeValue() {
    return oauthScope.value();
  }

  /** Returns the OAuth scope string for controllers and DTO mapping. */
  public String oauthScope() {
    return oauthScopeValue();
  }

  private static String requireCode(String code) {
    Objects.requireNonNull(code, "code");
    String trimmed = code.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("code must not be blank");
    }
    return trimmed;
  }

  private static String requireModule(String module) {
    Objects.requireNonNull(module, "module");
    String trimmed = module.trim().toLowerCase(Locale.ROOT);
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("module must not be blank");
    }
    return trimmed;
  }

  private static void requireModuleCompatibleScope(String module, Scope scope) {
    if ("oidc".equals(module) && !scope.isOidcStandard()) {
      throw new IllegalArgumentException(
          "oidc module scopes must be openid, profile, or email");
    }
  }
}
