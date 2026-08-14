package com.iam.federation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("OIDC provider smoke")
class OidcProviderSmokeTest {

  @LocalServerPort private int port;

  @Autowired private RegisteredClientRepository registeredClientRepository;

  @Test
  @DisplayName("should expose openid configuration when service is up")
  void shouldExposeOpenidConfigurationWhenServiceIsUp() {
    var body =
        RestClient.create()
            .get()
            .uri("http://localhost:" + port + "/.well-known/openid-configuration")
            .retrieve()
            .body(String.class);

    assertThat(body).contains("\"issuer\":\"http://localhost:9100\"");
    assertThat(body).contains("authorization_endpoint");
    assertThat(body).contains("token_endpoint");
  }

  @Test
  @DisplayName("should register explore ai client when properties are loaded")
  void shouldRegisterExploreAiClientWhenPropertiesAreLoaded() {
    RegisteredClient client = registeredClientRepository.findByClientId("explore-ai");

    assertThat(client).isNotNull();
    assertThat(client.getRedirectUris())
        .contains(
            "http://localhost:4200/login/oauth2/code/explore-iam",
            "http://localhost:9000/login/oauth2/code/explore-iam");
    assertThat(client.getScopes()).contains("openid", "profile", "email");
  }
}
