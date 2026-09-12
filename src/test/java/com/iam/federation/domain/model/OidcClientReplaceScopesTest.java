package com.iam.federation.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.federation.domain.vo.ClientId;
import com.iam.federation.domain.vo.RedirectUri;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OidcClient")
class OidcClientReplaceScopesTest {

  @Test
  @DisplayName("should replace scopes with GitHub style product scopes when openid present")
  void shouldReplaceScopesWithGitHubStyleProductScopesWhenOpenidPresent() {
    OidcClient client =
        OidcClient.seedPublic(
            new ClientId("explore-ai-ios"),
            "Explore AI iOS",
            Set.of(new RedirectUri("com.explore.ai://oauth/callback")),
            Set.of(),
            Set.of("openid", "profile", "email"));

    client.replaceScopes(
        Set.of("openid", "profile", "email", "write:ai_chat", "write:ai_audio"));

    assertThat(client.scopeValues()).contains("write:ai_chat", "write:ai_audio", "openid");
  }
}
