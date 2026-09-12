package com.iam.policy.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PermissionPoint")
class PermissionPointTest {

  @Test
  @DisplayName("should accept GitHub style write scope when module is ai")
  void shouldAcceptGitHubStyleWriteScopeWhenModuleIsAi() {
    PermissionPoint point =
        PermissionPoint.create(
            "write:ai_chat",
            "write:ai_chat",
            "ai",
            new Action("ai:Invoke"),
            new Resource("arn:ai:::chat/*"),
            "chat");

    assertThat(point.oauthScopeValue()).isEqualTo("write:ai_chat");
    assertThat(point.matchesScope("write:ai_chat")).isTrue();
  }

  @Test
  @DisplayName("should reject dotted product scope when module is ai")
  void shouldRejectDottedProductScopeWhenModuleIsAi() {
    assertThatThrownBy(
            () ->
                PermissionPoint.create(
                    "ai.chat",
                    "ai.chat",
                    "ai",
                    new Action("ai:Invoke"),
                    new Resource("arn:ai:::chat/*"),
                    "chat"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("GitHub style");
  }

  @Test
  @DisplayName("should accept openid when module is oidc")
  void shouldAcceptOpenidWhenModuleIsOidc() {
    PermissionPoint point =
        PermissionPoint.create(
            "openid",
            "openid",
            "oidc",
            new Action("oidc:OpenId"),
            new Resource("arn:oidc:::openid"),
            "OIDC");

    assertThat(point.getCode()).isEqualTo("openid");
  }
}
