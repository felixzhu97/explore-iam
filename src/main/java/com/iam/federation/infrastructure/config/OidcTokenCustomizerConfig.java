package com.iam.federation.infrastructure.config;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class OidcTokenCustomizerConfig {

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> oidcClaimsCustomizer(IamUserRepository iamUserRepository) {
        return context -> {
            if (!OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                return;
            }
            String username = context.getPrincipal().getName();
            iamUserRepository.findByUsername(username).ifPresent(user -> applyClaims(context, user));
        };
    }

    private static void applyClaims(JwtEncodingContext context, IamUser user) {
        Set<String> scopes = context.getAuthorizedScopes();
        context.getClaims().claim("sub", user.getId());
        if (scopes.contains(OidcScopes.EMAIL) && user.getEmail() != null) {
            context.getClaims().claim("email", user.getEmail());
            context.getClaims().claim("email_verified", true);
        }
        if (scopes.contains(OidcScopes.PROFILE)) {
            context.getClaims().claim("preferred_username", user.getUsername());
            context.getClaims().claim("name", user.getUsername());
        }
    }
}
