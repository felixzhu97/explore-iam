package com.iam.identity.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.identity.infra.security.MfaTestSessions;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("IAM user admin API")
class UserApiTest {

  private static final ParameterizedTypeReference<Map<String, Object>> JSON =
      new ParameterizedTypeReference<>() {};

  @LocalServerPort private int port;
  @Autowired private DataSource dataSource;

  private RestClient admin;

  @BeforeEach
  void setUp() {
    admin =
        MfaTestSessions.demoUserWithMfa(port, dataSource)
            .mutate()
            .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {})
            .build();
  }

  @Test
  @DisplayName("should disable enable and delete a user when admin is signed in")
  void shouldDisableEnableAndDeleteAUserWhenAdminIsSignedIn() {
    String id = createUser();

    ResponseEntity<Map<String, Object>> disabled = post("/api/v1/users/" + id + ":disable");
    ResponseEntity<Map<String, Object>> enabled = post("/api/v1/users/" + id + ":enable");
    ResponseEntity<Void> deleted =
        admin.delete().uri("/api/v1/users/{id}", id).retrieve().toBodilessEntity();
    ResponseEntity<Void> afterDelete =
        admin.get().uri("/api/v1/users/{id}", id).retrieve().toBodilessEntity();

    assertThat(disabled.getBody()).containsEntry("enabled", false);
    assertThat(enabled.getBody()).containsEntry("enabled", true);
    assertThat(deleted.getStatusCode().value()).isEqualTo(204);
    assertThat(afterDelete.getStatusCode().value()).isEqualTo(404);
  }

  @Test
  @DisplayName("should refuse to disable or delete the signed in admin")
  void shouldRefuseToDisableOrDeleteTheSignedInAdmin() {
    String selfId =
        new JdbcTemplate(dataSource)
            .queryForObject(
                "select id from directory_user where username = ?", String.class, "demo");

    ResponseEntity<Map<String, Object>> disable = post("/api/v1/users/" + selfId + ":disable");
    ResponseEntity<Void> delete =
        admin.delete().uri("/api/v1/users/{id}", selfId).retrieve().toBodilessEntity();

    assertThat(disable.getStatusCode().value()).isEqualTo(409);
    assertThat(delete.getStatusCode().value()).isEqualTo(409);
  }

  @Test
  @DisplayName("should reject delete when csrf token is missing")
  void shouldRejectDeleteWhenCsrfTokenIsMissing() {
    String id = createUser();

    RestClient withoutCsrf =
        admin.mutate().defaultHeaders(headers -> headers.remove("X-XSRF-TOKEN")).build();

    ResponseEntity<Void> delete =
        withoutCsrf.delete().uri("/api/v1/users/{id}", id).retrieve().toBodilessEntity();
    ResponseEntity<Void> stillThere =
        admin.get().uri("/api/v1/users/{id}", id).retrieve().toBodilessEntity();

    assertThat(delete.getStatusCode().value()).isEqualTo(403);
    assertThat(stillThere.getStatusCode().value()).isEqualTo(200);
  }

  private String createUser() {
    String username = "admin-ui-" + UUID.randomUUID().toString().substring(0, 8);
    Map<String, Object> body =
        admin
            .post()
            .uri("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                Map.of(
                    "username", username,
                    "email", username + "@example.test",
                    "password", "Str0ng-password!"))
            .retrieve()
            .toEntity(JSON)
            .getBody();
    return (String) body.get("id");
  }

  private ResponseEntity<Map<String, Object>> post(String uri) {
    return admin.post().uri(uri).retrieve().toEntity(JSON);
  }
}
