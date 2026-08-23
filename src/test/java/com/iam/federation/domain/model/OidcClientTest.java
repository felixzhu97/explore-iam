package com.iam.federation.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iam.federation.domain.vo.RedirectUri;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OidcClientTest {

  @Test
  @DisplayName("should reject scopes without openid when registering client")
  void shouldRejectScopesWithoutOpenidWhenRegisteringClient() {
    assertThatThrownBy(
            () ->
                OidcClient.register(
                    "demo",
                    "hash",
                    null,
                    Set.of(new RedirectUri("https://example.com/callback")),
                    Set.of(),
                    Set.of("profile"),
                    Set.of("code"),
                    Set.of("client_secret_basic"),
                    Set.of("authorization_code")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("openid");
  }
}
