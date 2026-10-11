package com.iam.federation.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.identity.infra.security.MfaTestSessions;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("OIDC client registration API")
class OAuthClientControllerTest {

  @LocalServerPort private int port;

  @Autowired private DataSource dataSource;

  @Test
  @DisplayName("should register and list client when authenticated")
  void shouldRegisterAndListClientWhenAuthenticated() {
    RestClient client = MfaTestSessions.demoUserWithMfa(this.port, this.dataSource);
    String name = "WhatsFeed-" + UUID.randomUUID().toString().substring(0, 8);

    ResponseEntity<OAuthClientResponse> created =
        client
            .post()
            .uri("/api/v1/oauthClients")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Map.of(
                    "clientName", name,
                    "redirectUris", List.of("http://localhost:4300/oauth/callback"),
                    "postLogoutRedirectUris", List.of("http://localhost:4300/"),
                    "scopes", List.of("openid", "profile"),
                    "responseTypes", List.of("code"),
                    "authorizationGrantTypes", List.of("authorization_code", "refresh_token"),
                    "clientAuthenticationMethods", List.of("client_secret_basic"),
                    "clientUri", "http://localhost:4300"))
            .retrieve()
            .toEntity(OAuthClientResponse.class);

    assertThat(created.getStatusCode().value()).isEqualTo(201);
    assertThat(created.getBody()).isNotNull();
    assertThat(created.getBody().clientId()).isNotBlank();
    assertThat(created.getBody().clientSecret()).isNotBlank();
    assertThat(created.getBody().clientName()).isEqualTo(name);
    assertThat(created.getBody().clientUri()).isEqualTo("http://localhost:4300");
    assertThat(created.getBody().responseTypes()).containsExactly("code");
    assertThat(created.getBody().authorizationGrantTypes())
        .contains("authorization_code", "refresh_token");
    assertThat(created.getBody().clientAuthenticationMethods())
        .containsExactly("client_secret_basic");

    List<OAuthClientResponse> list =
        client
            .get()
            .uri("/api/v1/oauthClients")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

    assertThat(list).isNotNull();
    OAuthClientResponse listed =
        list.stream().filter(c -> name.equals(c.clientName())).findFirst().orElseThrow();
    assertThat(listed.clientSecret()).isNull();
    assertThat(listed.clientUri()).isEqualTo("http://localhost:4300");
    assertThat(listed.responseTypes()).containsExactly("code");
    assertThat(listed.clientAuthenticationMethods()).containsExactly("client_secret_basic");
  }

  @Test
  @DisplayName("should reject invalid redirect uri")
  void shouldRejectInvalidRedirectUri() {
    RestClient client = MfaTestSessions.demoUserWithMfa(this.port, this.dataSource);
    org.assertj.core.api.Assertions.assertThatThrownBy(
            () ->
                client
                    .post()
                    .uri("/api/v1/oauthClients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        Map.of("clientName", "Bad", "redirectUris", List.of("javascript:alert(1)")))
                    .retrieve()
                    .toBodilessEntity())
        .hasMessageContaining("400");
  }
}
