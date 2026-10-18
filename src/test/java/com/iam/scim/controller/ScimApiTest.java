package com.iam.scim.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("SCIM 2.0 API")
class ScimApiTest {

  private static final MediaType SCIM = MediaType.parseMediaType("application/scim+json");
  private static final ParameterizedTypeReference<Map<String, Object>> JSON =
      new ParameterizedTypeReference<>() {};

  @LocalServerPort private int port;

  private RestClient client;

  @BeforeEach
  void setUp() {
    client =
        RestClient.builder()
            .baseUrl("http://localhost:" + port)
            .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {})
            .build();
  }

  @Test
  @DisplayName("should return 401 scim error when no bearer token is sent")
  void shouldReturn401ScimErrorWhenNoBearerTokenIsSent() {
    ResponseEntity<Map<String, Object>> response =
        client.get().uri("/scim/v2/Users").retrieve().toEntity(JSON);

    assertThat(response.getStatusCode().value()).isEqualTo(401);
    assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Bearer");
    assertThat(response.getBody()).containsEntry("status", "401");
  }

  @Test
  @DisplayName("should return 403 when token lacks write iam scim scope")
  void shouldReturn403WhenTokenLacksWriteIamScimScope() {
    String token = token("scim-no-scope", "write:ai_chat");

    ResponseEntity<Map<String, Object>> response =
        client.get().uri("/scim/v2/Users").headers(h -> h.setBearerAuth(token)).retrieve()
            .toEntity(JSON);

    assertThat(response.getStatusCode().value()).isEqualTo(403);
  }

  @Test
  @DisplayName("should serve discovery endpoints when authorized")
  void shouldServeDiscoveryEndpointsWhenAuthorized() {
    String token = provisionerToken();

    Map<String, Object> config = get(token, "/scim/v2/ServiceProviderConfig").getBody();
    Map<String, Object> types = get(token, "/scim/v2/ResourceTypes").getBody();
    Map<String, Object> schemas = get(token, "/scim/v2/Schemas").getBody();

    assertThat(config).extractingByKey("patch").isEqualTo(Map.of("supported", true));
    assertThat(config).extractingByKey("bulk").asInstanceOf(
        org.assertj.core.api.InstanceOfAssertFactories.MAP).containsEntry("supported", false);
    assertThat(types).containsEntry("totalResults", 2);
    assertThat(schemas).containsEntry("totalResults", 2);
  }

  @Test
  @DisplayName("should run user lifecycle with etag filter patch and delete")
  void shouldRunUserLifecycleWithEtagFilterPatchAndDelete() {
    String token = provisionerToken();
    String userName = "scim-" + UUID.randomUUID().toString().substring(0, 8);
    String externalId = "okta-" + UUID.randomUUID();

    ResponseEntity<Map<String, Object>> created =
        send(token, "POST", "/scim/v2/Users", null, Map.of(
            "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
            "userName", userName,
            "externalId", externalId,
            "displayName", "Scim User",
            "emails", List.of(Map.of("value", userName + "@example.com", "primary", true)),
            "active", true));
    assertThat(created.getStatusCode().value()).isEqualTo(201);
    assertThat(created.getHeaders().getContentType().isCompatibleWith(SCIM)).isTrue();
    assertThat(created.getHeaders().getLocation()).isNotNull();
    assertThat(created.getHeaders().getETag()).isEqualTo("W/\"0\"");
    String id = (String) created.getBody().get("id");

    ResponseEntity<Map<String, Object>> duplicate =
        send(token, "POST", "/scim/v2/Users", null, Map.of("userName", userName));
    assertThat(duplicate.getStatusCode().value()).isEqualTo(409);
    assertThat(duplicate.getBody()).containsEntry("scimType", "uniqueness");

    Map<String, Object> filtered =
        get(token, "/scim/v2/Users?filter=externalId eq \"" + externalId + "\"").getBody();
    assertThat(filtered).containsEntry("totalResults", 1);

    ResponseEntity<Map<String, Object>> stale =
        send(token, "PATCH", "/scim/v2/Users/" + id, "W/\"9\"", deactivatePatch());
    assertThat(stale.getStatusCode().value()).isEqualTo(412);

    ResponseEntity<Map<String, Object>> patched =
        send(token, "PATCH", "/scim/v2/Users/" + id, "W/\"0\"", deactivatePatch());
    assertThat(patched.getStatusCode().value()).isEqualTo(200);
    assertThat(patched.getBody()).containsEntry("active", false);
    assertThat(patched.getHeaders().getETag()).isEqualTo("W/\"1\"");

    assertThat(send(token, "DELETE", "/scim/v2/Users/" + id, null, null).getStatusCode().value())
        .isEqualTo(204);
    assertThat(get(token, "/scim/v2/Users/" + id).getStatusCode().value()).isEqualTo(404);
  }

  @Test
  @DisplayName("should manage group members with patch add and remove")
  void shouldManageGroupMembersWithPatchAddAndRemove() {
    String token = provisionerToken();
    String userId = createUser(token);
    String groupName = "scim-group-" + UUID.randomUUID().toString().substring(0, 8);

    ResponseEntity<Map<String, Object>> created =
        send(token, "POST", "/scim/v2/Groups", null, Map.of(
            "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:Group"),
            "displayName", groupName,
            "members", List.of()));
    assertThat(created.getStatusCode().value()).isEqualTo(201);
    String groupId = (String) created.getBody().get("id");

    ResponseEntity<Map<String, Object>> added =
        send(token, "PATCH", "/scim/v2/Groups/" + groupId, null, Map.of(
            "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp"),
            "Operations", List.of(Map.of(
                "op", "add", "path", "members", "value", List.of(Map.of("value", userId))))));
    assertThat(added.getStatusCode().value()).isEqualTo(200);
    assertThat((List<?>) added.getBody().get("members")).hasSize(1);

    ResponseEntity<Map<String, Object>> removed =
        send(token, "PATCH", "/scim/v2/Groups/" + groupId, null, Map.of(
            "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp"),
            "Operations", List.of(Map.of(
                "op", "remove", "path", "members[value eq \"" + userId + "\"]"))));
    assertThat(removed.getStatusCode().value()).isEqualTo(200);
    assertThat(removed.getBody().get("members")).satisfiesAnyOf(
        members -> assertThat(members).isNull(),
        members -> assertThat((List<?>) members).isEmpty());

    assertThat(send(token, "DELETE", "/scim/v2/Groups/" + groupId, null, null)
        .getStatusCode().value()).isEqualTo(204);
  }

  @Test
  @DisplayName("should return 400 invalid filter for malformed filter")
  void shouldReturn400InvalidFilterForMalformedFilter() {
    ResponseEntity<Map<String, Object>> response =
        get(provisionerToken(), "/scim/v2/Users?filter=userName eq");

    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody()).containsEntry("scimType", "invalidFilter");
  }

  private String createUser(String token) {
    String userName = "member-" + UUID.randomUUID().toString().substring(0, 8);
    return (String) send(token, "POST", "/scim/v2/Users", null, Map.of("userName", userName))
        .getBody().get("id");
  }

  private static Map<String, Object> deactivatePatch() {
    return Map.of(
        "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp"),
        "Operations", List.of(Map.of("op", "replace", "value", Map.of("active", false))));
  }

  private ResponseEntity<Map<String, Object>> get(String token, String uri) {
    return client.get().uri(uri).headers(h -> h.setBearerAuth(token)).accept(SCIM).retrieve()
        .toEntity(JSON);
  }

  private ResponseEntity<Map<String, Object>> send(
      String token, String method, String uri, String ifMatch, Object body) {
    RestClient.RequestBodySpec spec =
        client.method(org.springframework.http.HttpMethod.valueOf(method)).uri(uri)
            .headers(h -> {
              h.setBearerAuth(token);
              if (ifMatch != null) {
                h.setIfMatch(ifMatch);
              }
            })
            .accept(SCIM);
    if (body != null) {
      spec.contentType(SCIM).body(body);
    }
    return spec.retrieve().toEntity(JSON);
  }

  private String provisionerToken() {
    return token("scim-provisioner", "write:iam_scim");
  }

  private String token(String clientId, String scope) {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "client_credentials");
    form.add("scope", scope);
    Map<String, Object> response =
        client.post().uri("/oauth2/token")
            .headers(h -> h.setBasicAuth(clientId, "scim-test-secret"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(JSON);
    assertThat(response).containsKey("access_token");
    return (String) response.get("access_token");
  }
}
