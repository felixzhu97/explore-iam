package com.iam.policy.domain.model;

import com.iam.common.domain.base.AbstractImmutable;
import com.iam.common.domain.converter.ActionAttributeConverter;
import com.iam.common.domain.converter.ResourceAttributeConverter;
import com.iam.common.domain.converter.ScopeAttributeConverter;
import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Resource;
import com.iam.common.domain.vo.Scope;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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

  private String code;

  @Convert(converter = ScopeAttributeConverter.class)
  private Scope oauthScope;

  private String module;

  @Convert(converter = ActionAttributeConverter.class)
  private Action action;

  @Convert(converter = ResourceAttributeConverter.class)
  private Resource resource;

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
   * Rebuilds from persistence.
   *
   * @param id internal id
   * @param code business key
   * @param oauthScope oauth scope
   * @param module module
   * @param action action
   * @param resource resource
   * @param description description
   * @param createdAt created at
   * @return reconstituted aggregate
   */
  public static PermissionPoint reconstitute(
      String id,
      String code,
      String oauthScope,
      String module,
      Action action,
      Resource resource,
      String description,
      Instant createdAt) {
    return new PermissionPoint(
        id, code, Scope.of(oauthScope), module, action, resource, description, createdAt);
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
