package com.iam.federation.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OAuthClientTest {

  @Test
  @DisplayName("should reject scopes without openid when registering client")
  void shouldRejectScopesWithoutOpenidWhenRegisteringClient() {
    assertThatThrownBy(
            () ->
                OAuthClient.createOAuthClient(
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

  @Test
  @DisplayName("should create machine client without redirect uris or openid")
  void shouldCreateMachineClientWithoutRedirectUrisOrOpenid() {
    OAuthClient client =
        OAuthClient.createMachineBootstrapOAuthClient(
            new ClientId("scim-provisioner"), "SCIM", "{bcrypt}hash", Set.of("write:iam_scim"));

    assertThat(client.isMachineClient()).isTrue();
    assertThat(client.redirectUris()).isEmpty();
    assertThat(client.authorizationGrantTypes()).containsExactly("client_credentials");
  }

  @Test
  @DisplayName("should reject machine client without secret")
  void shouldRejectMachineClientWithoutSecret() {
    assertThatThrownBy(
            () ->
                OAuthClient.createMachineBootstrapOAuthClient(
                    new ClientId("scim-provisioner"), "SCIM", null, Set.of("write:iam_scim")))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
