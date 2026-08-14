package com.iam.identity.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Login context API")
class LoginContextControllerTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("should return oauth false when client_id is absent")
    void shouldReturnOauthFalseWhenClientIdIsAbsent() {
        LoginContextResponse body = RestClient.create()
                .get()
                .uri("http://localhost:" + this.port + "/api/login/context")
                .retrieve()
                .body(LoginContextResponse.class);

        assertThat(body).isNotNull();
        assertThat(body.oauth()).isFalse();
        assertThat(body.clientId()).isNull();
    }

    @Test
    @DisplayName("should resolve explore-ai client name when client_id is present")
    void shouldResolveExploreAiClientNameWhenClientIdIsPresent() {
        LoginContextResponse body = RestClient.create()
                .get()
                .uri("http://localhost:" + this.port + "/api/login/context?client_id=explore-ai")
                .retrieve()
                .body(LoginContextResponse.class);

        assertThat(body).isNotNull();
        assertThat(body.oauth()).isTrue();
        assertThat(body.clientId()).isEqualTo("explore-ai");
        assertThat(body.clientName()).isEqualTo("Explore AI");
    }
}
