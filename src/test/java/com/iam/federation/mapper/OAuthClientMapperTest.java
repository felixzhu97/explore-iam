package com.iam.federation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.federation.domain.model.ClientId;
import com.iam.federation.domain.model.OAuthClient;
import com.iam.federation.domain.model.RedirectUri;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

class OAuthClientMapperTest {

  @Test
  @DisplayName("should require proof key when mapping public client")
  void shouldRequireProofKeyWhenMappingPublicClient() {
    OAuthClient client =
        OAuthClient.seedPublic(
            new ClientId("explore-ai-ios"),
            "Explore AI iOS",
            Set.of(new RedirectUri("com.explore.ai://oauth/callback")),
            Set.of(),
            Set.of("openid", "profile", "email"));

    RegisteredClient registered = OAuthClientMapper.toRegisteredClient(client);

    assertThat(registered.getClientSettings().isRequireProofKey()).isTrue();
    assertThat(client.isPublicClient()).isTrue();
  }
}
