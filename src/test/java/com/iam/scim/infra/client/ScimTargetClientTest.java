package com.iam.scim.infra.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.iam.identity.domain.model.ExternalId;
import com.iam.identity.domain.model.User;
import com.iam.scim.domain.model.ProvisioningTarget;
import com.iam.scim.domain.service.ScimTargetGateway;
import com.iam.scim.infra.ScimJsonHttpMessageConverter;
import com.iam.scim.mapper.ScimUserMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@SpringJUnitConfig(ScimTargetClientTest.TestConfig.class)
@DisplayName("ScimTargetClient")
class ScimTargetClientTest {

  private static final String BASE = "https://ai.example/scim/v2";
  private static final String USER_JSON =
      """
      {"schemas":["urn:ietf:params:scim:schemas:core:2.0:User"],
       "id":"remote-1","userName":"alice"}
      """;
  private static final String LIST_JSON =
      """
      {"schemas":["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
       "totalResults":1,
       "Resources":[{"schemas":["urn:ietf:params:scim:schemas:core:2.0:User"],
                     "id":"remote-9","userName":"alice"}]}
      """;

  @Autowired private ScimTargetGateway gateway;

  @Autowired private MockRestServiceServer server;

  private final ProvisioningTarget target =
      ProvisioningTarget.createProvisioningTarget("explore-ai", BASE, "iam-scim-outbound", null);

  private final User user =
      User.provisionUser("alice", "alice@example.com", "Alice", new ExternalId("okta-1"));

  @BeforeEach
  void resetServer() {
    server.reset();
  }

  @Test
  @DisplayName("should send bearer token and scim json when creating account")
  void shouldSendBearerTokenAndScimJsonWhenCreatingAccount() {
    server
        .expect(requestTo(BASE + "/Users"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", "Bearer test-token"))
        .andExpect(content().contentTypeCompatibleWith(ScimJsonHttpMessageConverter.SCIM_JSON))
        .andExpect(jsonPath("$.externalId").value(user.getId()))
        .andExpect(jsonPath("$.userName").value("alice"))
        .andExpect(jsonPath("$.active").value(true))
        .andRespond(
            withStatus(HttpStatus.CREATED)
                .contentType(ScimJsonHttpMessageConverter.SCIM_JSON)
                .body(USER_JSON));

    assertThat(gateway.createAccount(target, user)).isEqualTo("remote-1");
    server.verify();
  }

  @Test
  @DisplayName("should replace existing account when create conflicts")
  void shouldReplaceExistingAccountWhenCreateConflicts() {
    server
        .expect(requestTo(BASE + "/Users"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.CONFLICT));
    server
        .expect(method(HttpMethod.GET))
        .andExpect(queryParam("filter", "externalId%20eq%20%22" + user.getId() + "%22"))
        .andRespond(withSuccess(LIST_JSON, ScimJsonHttpMessageConverter.SCIM_JSON));
    server
        .expect(requestTo(BASE + "/Users/remote-9"))
        .andExpect(method(HttpMethod.PUT))
        .andRespond(withSuccess(USER_JSON, ScimJsonHttpMessageConverter.SCIM_JSON));

    assertThat(gateway.createAccount(target, user)).isEqualTo("remote-9");
    server.verify();
  }

  @Test
  @DisplayName("should retry when target is temporarily unavailable")
  void shouldRetryWhenTargetIsTemporarilyUnavailable() {
    server
        .expect(ExpectedCount.times(2), method(HttpMethod.GET))
        .andRespond(withServiceUnavailable());
    server
        .expect(method(HttpMethod.GET))
        .andRespond(withSuccess(LIST_JSON, ScimJsonHttpMessageConverter.SCIM_JSON));

    assertThat(gateway.findRemoteId(target, user.getId())).contains("remote-9");
    server.verify();
  }

  @Test
  @DisplayName("should patch only active attribute when deactivating")
  void shouldPatchOnlyActiveAttributeWhenDeactivating() {
    server
        .expect(requestTo(BASE + "/Users/remote-1"))
        .andExpect(method(HttpMethod.PATCH))
        .andExpect(jsonPath("$.Operations[0].op").value("replace"))
        .andExpect(jsonPath("$.Operations[0].path").value("active"))
        .andExpect(jsonPath("$.Operations[0].value").value(false))
        .andRespond(withSuccess(USER_JSON, ScimJsonHttpMessageConverter.SCIM_JSON));

    assertThat(gateway.setAccountActive(target, "remote-1", false)).isTrue();
    server.verify();
  }

  @Test
  @DisplayName("should report missing account when replace returns not found")
  void shouldReportMissingAccountWhenReplaceReturnsNotFound() {
    server
        .expect(requestTo(BASE + "/Users/remote-1"))
        .andExpect(method(HttpMethod.PUT))
        .andRespond(withResourceNotFound());

    assertThat(gateway.replaceAccount(target, "remote-1", user)).isFalse();
    server.verify();
  }

  @Test
  @DisplayName("should treat missing account as deleted")
  void shouldTreatMissingAccountAsDeleted() {
    server
        .expect(requestTo(BASE + "/Users/remote-1"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withResourceNotFound());

    gateway.deleteAccount(target, "remote-1");
    server.verify();
  }

  @Configuration
  @EnableResilientMethods
  static class TestConfig {

    private final RestClient.Builder builder = RestClient.builder();

    @Bean
    MockRestServiceServer server() {
      return MockRestServiceServer.bindTo(builder).build();
    }

    @Bean
    ScimTargetClient scimTargetClient(MockRestServiceServer server) {
      return new ScimTargetClient(builder, authorizedClientManager(), new ScimUserMapper());
    }

    private static OAuth2AuthorizedClientManager authorizedClientManager() {
      ClientRegistration registration =
          ClientRegistration.withRegistrationId("iam-scim-outbound")
              .clientId("iam-scim-outbound")
              .clientSecret("secret")
              .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
              .tokenUri("https://iam.example/oauth2/token")
              .build();
      OAuth2AccessToken token =
          new OAuth2AccessToken(
              OAuth2AccessToken.TokenType.BEARER,
              "test-token",
              Instant.now(),
              Instant.now().plusSeconds(300));
      return request -> new OAuth2AuthorizedClient(registration, "iam-scim-outbound", token);
    }
  }
}
