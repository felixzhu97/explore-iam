package com.iam.policy.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iam.common.domain.model.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Permission")
class PermissionTest {

  @Test
  @DisplayName("should accept GitHub style write scope when module is ai")
  void shouldAcceptGitHubStyleWriteScopeWhenModuleIsAi() {
    Permission point =
        Permission.createPermission(
            "write:ai_chat",
            "write:ai_chat",
            "ai",
            new com.iam.common.domain.model.Permission("ai:Invoke"),
            new Resource("arn:ai:::chat/*"),
            "chat");

    assertThat(point.getOauthScope().value()).isEqualTo("write:ai_chat");
    assertThat(point.matchesOAuthScope("write:ai_chat")).isTrue();
  }

  @Test
  @DisplayName("should reject dotted product scope when module is ai")
  void shouldRejectDottedProductScopeWhenModuleIsAi() {
    assertThatThrownBy(
            () ->
                Permission.createPermission(
                    "ai.chat",
                    "ai.chat",
                    "ai",
                    new com.iam.common.domain.model.Permission("ai:Invoke"),
                    new Resource("arn:ai:::chat/*"),
                    "chat"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("GitHub style");
  }

  @Test
  @DisplayName("should accept openid when module is oidc")
  void shouldAcceptOpenidWhenModuleIsOidc() {
    Permission point =
        Permission.createPermission(
            "openid",
            "openid",
            "oidc",
            new com.iam.common.domain.model.Permission("oidc:OpenId"),
            new Resource("arn:oidc:::openid"),
            "OIDC");

    assertThat(point.getCode()).isEqualTo("openid");
  }
}
