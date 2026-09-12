package com.iam.federation.infra.config;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/** Customizes OIDC ID Token and access token claims from the IAM user aggregate. */
@Configuration
public class OidcTokenCustomizerConfig {

  @Bean
  OAuth2TokenCustomizer<JwtEncodingContext> oidcClaimsCustomizer(
      IamUserRepository iamUserRepository) {
    return context -> {
      String tokenType = context.getTokenType().getValue();
      boolean idToken = OidcParameterNames.ID_TOKEN.equals(tokenType);
      boolean accessToken = "access_token".equals(tokenType);
      if (!idToken && !accessToken) {
        return;
      }
      String username = context.getPrincipal().getName();
      iamUserRepository.findByUsername(username).ifPresent(user -> applyClaims(context, user));
    };
  }

  private static void applyClaims(JwtEncodingContext context, IamUser user) {
    Set<String> scopes = context.getAuthorizedScopes();
    // Resource servers (AI / Chat) validate the access token and need email/profile
    // on that JWT — not only on the ID token.
    context.getClaims().claim("sub", user.getId());
    if (scopes.contains(OidcScopes.EMAIL) && user.getEmail() != null) {
      context.getClaims().claim("email", user.getEmail());
      context.getClaims().claim("email_verified", true);
    }
    if (scopes.contains(OidcScopes.PROFILE)) {
      context.getClaims().claim("preferred_username", user.getUsername());
      context.getClaims().claim("name", user.getUsername());
    }
    if ("access_token".equals(context.getTokenType().getValue())) {
      Set<String> permissions =
          scopes.stream()
              .filter(scope -> !"openid".equals(scope))
              .filter(scope -> !"profile".equals(scope))
              .filter(scope -> !"email".equals(scope))
              .collect(Collectors.toCollection(LinkedHashSet::new));
      if (!permissions.isEmpty()) {
        context.getClaims().claim("permissions", permissions);
      }
    }
  }
}
