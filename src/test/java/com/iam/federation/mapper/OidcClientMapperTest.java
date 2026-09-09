package com.iam.federation.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.federation.domain.model.OidcClient;
import com.iam.federation.domain.vo.ClientId;
import com.iam.federation.domain.vo.RedirectUri;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

class OidcClientMapperTest {

  @Test
  @DisplayName("should require proof key when mapping public client")
  void shouldRequireProofKeyWhenMappingPublicClient() {
    OidcClient client =
        OidcClient.seedPublic(
            new ClientId("explore-ai-ios"),
            "Explore AI iOS",
            Set.of(new RedirectUri("com.explore.ai://oauth/callback")),
            Set.of(),
            Set.of("openid", "profile", "email"));

    RegisteredClient registered = OidcClientMapper.toRegisteredClient(client);

    assertThat(registered.getClientSettings().isRequireProofKey()).isTrue();
    assertThat(client.isPublicClient()).isTrue();
  }
}
