package com.iam.federation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iam.audit.service.AdminActivityRecorder;
import com.iam.federation.domain.model.OAuthClient;
import com.iam.federation.domain.model.RedirectUri;
import com.iam.federation.domain.repository.OAuthClientRepository;
import com.iam.federation.infra.config.OidcSeedClientProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuthClientService")
class OAuthClientServiceTest {

  @Mock private OAuthClientRepository oauthClientRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private AdminActivityRecorder adminActivityRecorder;

  private OAuthClientService oauthClientService;

  @BeforeEach
  void setUp() {
    oauthClientService =
        new OAuthClientService(
            oauthClientRepository,
            passwordEncoder,
            new OidcSeedClientProperties(),
            adminActivityRecorder);
  }

  @Test
  @DisplayName("should register client and return plaintext secret once")
  void shouldRegisterClientAndReturnPlaintextSecretOnce() {
    when(passwordEncoder.encode(any())).thenReturn("{bcrypt}hash");
    when(oauthClientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result =
        oauthClientService.createOAuthClient(
            new OAuthClientService.RegisterOAuthClientCommand(
                "Demo App",
                List.of("http://localhost:3000/callback"),
                List.of("http://localhost:3000/"),
                null,
                List.of("code"),
                List.of("authorization_code", "refresh_token"),
                List.of("client_secret_basic"),
                "http://localhost:3000"));

    assertThat(result.clientName()).isEqualTo("Demo App");
    assertThat(result.clientId()).startsWith("app-");
    assertThat(result.clientSecret()).isNotBlank();
    assertThat(result.clientUri()).isEqualTo("http://localhost:3000");
    assertThat(result.redirectUris()).containsExactly("http://localhost:3000/callback");
    assertThat(result.scopes()).contains("openid", "profile", "email");
    assertThat(result.responseTypes()).containsExactly("code");
    assertThat(result.authorizationGrantTypes()).contains("authorization_code", "refresh_token");
    assertThat(result.clientAuthenticationMethods()).containsExactly("client_secret_basic");

    ArgumentCaptor<OAuthClient> captor = ArgumentCaptor.forClass(OAuthClient.class);
    verify(oauthClientRepository).save(captor.capture());
    assertThat(captor.getValue().storedSecretHash()).isEqualTo("{bcrypt}hash");
    assertThat(captor.getValue().redirectUris())
        .containsExactly(new RedirectUri("http://localhost:3000/callback"));
  }

  @Test
  @DisplayName("should register public client without secret")
  void shouldRegisterPublicClientWithoutSecret() {
    when(oauthClientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result =
        oauthClientService.createOAuthClient(
            new OAuthClientService.RegisterOAuthClientCommand(
                "SPA",
                List.of("http://localhost:4200/callback"),
                null,
                List.of("openid", "profile"),
                List.of("code"),
                List.of("authorization_code"),
                List.of("none"),
                null));

    assertThat(result.clientSecret()).isNull();
    verify(passwordEncoder, never()).encode(any());
    ArgumentCaptor<OAuthClient> captor = ArgumentCaptor.forClass(OAuthClient.class);
    verify(oauthClientRepository).save(captor.capture());
    assertThat(captor.getValue().storedSecretHash()).isNull();
    assertThat(captor.getValue().isPublicClient()).isTrue();
  }

  @Test
  @DisplayName("should reject invalid grant type")
  void shouldRejectInvalidGrantType() {
    assertThatThrownBy(
            () ->
                oauthClientService.createOAuthClient(
                    new OAuthClientService.RegisterOAuthClientCommand(
                        "Bad",
                        List.of("http://localhost:3000/callback"),
                        null,
                        List.of("openid"),
                        List.of("code"),
                        List.of("refresh_token"),
                        List.of("client_secret_basic"),
                        null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("authorization_code");
  }

  @Test
  @DisplayName("should reject invalid redirect uri")
  void shouldRejectInvalidRedirectUri() {
    assertThatThrownBy(
            () ->
                oauthClientService.createOAuthClient(
                    new OAuthClientService.RegisterOAuthClientCommand(
                        "Bad", List.of("not-a-uri"), null, null, null, null, null, null)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
