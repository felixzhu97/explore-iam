package com.iam.federation.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Client registration API")
class ClientControllerTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("should register and list client when authenticated")
    void shouldRegisterAndListClientWhenAuthenticated() {
        RestClient client = authenticatedClient();
        String name = "WhatsFeed-" + UUID.randomUUID().toString().substring(0, 8);

        ResponseEntity<ClientResponse> created = client.post()
                .uri("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "clientName", name,
                        "redirectUris", List.of("http://localhost:4300/oauth/callback"),
                        "postLogoutRedirectUris", List.of("http://localhost:4300/"),
                        "scopes", List.of("openid", "profile"),
                        "responseTypes", List.of("code"),
                        "authorizationGrantTypes", List.of("authorization_code", "refresh_token"),
                        "clientAuthenticationMethods", List.of("client_secret_basic"),
                        "clientUri", "http://localhost:4300"))
                .retrieve()
                .toEntity(ClientResponse.class);

        assertThat(created.getStatusCode().value()).isEqualTo(201);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().clientId()).isNotBlank();
        assertThat(created.getBody().clientSecret()).isNotBlank();
        assertThat(created.getBody().clientName()).isEqualTo(name);
        assertThat(created.getBody().clientUri()).isEqualTo("http://localhost:4300");
        assertThat(created.getBody().responseTypes()).containsExactly("code");
        assertThat(created.getBody().authorizationGrantTypes())
                .contains("authorization_code", "refresh_token");
        assertThat(created.getBody().clientAuthenticationMethods()).containsExactly("client_secret_basic");

        List<ClientResponse> list = client.get()
                .uri("/api/clients")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(list).isNotNull();
        ClientResponse listed = list.stream()
                .filter(c -> name.equals(c.clientName()))
                .findFirst()
                .orElseThrow();
        assertThat(listed.clientSecret()).isNull();
        assertThat(listed.clientUri()).isEqualTo("http://localhost:4300");
        assertThat(listed.responseTypes()).containsExactly("code");
        assertThat(listed.clientAuthenticationMethods()).containsExactly("client_secret_basic");
    }

    @Test
    @DisplayName("should reject invalid redirect uri")
    void shouldRejectInvalidRedirectUri() {
        RestClient client = authenticatedClient();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.post()
                        .uri("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of(
                                "clientName", "Bad",
                                "redirectUris", List.of("javascript:alert(1)")))
                        .retrieve()
                        .toBodilessEntity())
                .hasMessageContaining("400");
    }

    private RestClient authenticatedClient() {
        var cookieStore = new CookieManager();
        var httpClient = java.net.http.HttpClient.newBuilder()
                .cookieHandler(cookieStore)
                .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
                .build();
        RestClient rest = RestClient.builder()
                .baseUrl("http://localhost:" + this.port)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();

        rest.get().uri("/login").retrieve().toBodilessEntity();
        String csrf = cookieStore.getCookieStore().getCookies().stream()
                .filter(c -> "XSRF-TOKEN".equals(c.getName()))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElseThrow();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "demo");
        form.add("password", "demo-password");
        form.add("_csrf", csrf);

        rest.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();

        String csrfAfterLogin = cookieStore.getCookieStore().getCookies().stream()
                .filter(c -> "XSRF-TOKEN".equals(c.getName()))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElse(csrf);

        return RestClient.builder()
                .baseUrl("http://localhost:" + this.port)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .defaultHeader("X-XSRF-TOKEN", csrfAfterLogin)
                .build();
    }
}
