package com.iam.federation.infra.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iam.common.security.IamPrincipals;
import com.iam.common.security.IamUserDetails;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@DisplayName("OIDC token customizer")
class OidcTokenCustomizerConfigTest {

  private final UserRepository users = mock(UserRepository.class);

  private final OAuth2TokenCustomizer<JwtEncodingContext> customizer =
      new OidcTokenCustomizerConfig().oidcClaimsCustomizer(users);

  @Test
  @DisplayName("should put the user id in sub when the user signs in")
  void shouldPutTheUserIdInSubWhenTheUserSignsIn() {
    User alice = User.createUser("alice", "alice@example.com", "{noop}x");
    when(users.findById(alice.getId())).thenReturn(Optional.of(alice));
    JwtClaimsSet.Builder claims = JwtClaimsSet.builder();

    customizer.customize(
        context(
            AuthorizationGrantType.AUTHORIZATION_CODE, signedIn(alice.getId(), "alice"), claims));

    assertThat(claims.build().getSubject()).isEqualTo(alice.getId());
  }

  @Test
  @DisplayName("should resolve a federated sign-in by its IAM user id")
  void shouldResolveAFederatedSignInByItsIamUserId() {
    User fed = User.createUser("google:123", null, "{noop}x");
    when(users.findById(fed.getId())).thenReturn(Optional.of(fed));
    DefaultOAuth2User oauthUser =
        new DefaultOAuth2User(
            List.of(), Map.of("sub", "123", IamPrincipals.USER_ID_ATTRIBUTE, fed.getId()), "sub");
    JwtClaimsSet.Builder claims = JwtClaimsSet.builder();

    customizer.customize(
        context(
            AuthorizationGrantType.AUTHORIZATION_CODE,
            new OAuth2AuthenticationToken(oauthUser, List.of(), "google"),
            claims));

    assertThat(claims.build().getSubject()).isEqualTo(fed.getId());
  }

  @Test
  @DisplayName("should refuse a token when the user was deleted")
  void shouldRefuseATokenWhenTheUserWasDeleted() {
    when(users.findById("gone-id")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                customizer.customize(
                    context(
                        AuthorizationGrantType.AUTHORIZATION_CODE,
                        signedIn("gone-id", "gone"),
                        JwtClaimsSet.builder())))
        .isInstanceOf(OAuth2AuthenticationException.class);
  }

  @Test
  @DisplayName("should refuse a refresh when the user was deactivated")
  void shouldRefuseARefreshWhenTheUserWasDeactivated() {
    User bob = User.createUser("bob", null, "{noop}x");
    bob.disableUser();
    when(users.findById(bob.getId())).thenReturn(Optional.of(bob));

    assertThatThrownBy(
            () ->
                customizer.customize(
                    context(
                        AuthorizationGrantType.REFRESH_TOKEN,
                        signedIn(bob.getId(), "bob"),
                        JwtClaimsSet.builder())))
        .isInstanceOf(OAuth2AuthenticationException.class);
  }

  @Test
  @DisplayName("should refuse a token when the username now belongs to someone else")
  void shouldRefuseATokenWhenTheUsernameNowBelongsToSomeoneElse() {
    User newcomer = User.createUser("carol", null, "{noop}x");
    when(users.findByUsername("carol")).thenReturn(Optional.of(newcomer));
    when(users.findById("old-carol-id")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                customizer.customize(
                    context(
                        AuthorizationGrantType.AUTHORIZATION_CODE,
                        signedIn("old-carol-id", "carol"),
                        JwtClaimsSet.builder())))
        .isInstanceOf(OAuth2AuthenticationException.class);
  }

  private static Authentication signedIn(String userId, String username) {
    IamUserDetails details = new IamUserDetails(userId, username, "{noop}x", true, List.of());
    return UsernamePasswordAuthenticationToken.authenticated(details, null, List.of());
  }

  private static JwtEncodingContext context(
      AuthorizationGrantType grant, Authentication principal, JwtClaimsSet.Builder claims) {
    JwtEncodingContext context = mock(JwtEncodingContext.class);
    when(context.getTokenType()).thenReturn(OAuth2TokenType.ACCESS_TOKEN);
    when(context.getAuthorizationGrantType()).thenReturn(grant);
    when(context.getPrincipal()).thenReturn(principal);
    when(context.getAuthorizedScopes()).thenReturn(Set.of("openid"));
    when(context.getClaims()).thenReturn(claims);
    return context;
  }
}
