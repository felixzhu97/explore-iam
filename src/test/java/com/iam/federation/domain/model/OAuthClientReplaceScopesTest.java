package com.iam.federation.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OAuthClient")
class OAuthClientReplaceScopesTest {

  @Test
  @DisplayName("should replace scopes with GitHub style product scopes when openid present")
  void shouldReplaceScopesWithGitHubStyleProductScopesWhenOpenidPresent() {
    OAuthClient client =
        OAuthClient.seedPublic(
            new ClientId("explore-ai-ios"),
            "Explore AI iOS",
            Set.of(new RedirectUri("com.explore.ai://oauth/callback")),
            Set.of(),
            Set.of("openid", "profile", "email"));

    client.replaceScopes(
        Set.of("openid", "profile", "email", "write:ai_chat", "write:ai_audio"));

    assertThat(client.scopes()).contains("write:ai_chat", "write:ai_audio", "openid");
  }
}
