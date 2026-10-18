package com.iam.scim.infra;

import static org.springframework.security.config.Customizer.withDefaults;

import com.unboundid.scim2.common.messages.ErrorResponse;
import com.unboundid.scim2.common.utils.JsonUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Stateless Bearer JWT chain for {@code /scim/v2/**}; requires {@code write:iam_scim}. Ordered
 * before the default session chain so SCIM never falls back to form login or CSRF.
 */
@Configuration
public class ScimSecurityConfig {

  /** Authority granted for the {@code write:iam_scim} OAuth scope. */
  public static final String SCIM_AUTHORITY = "SCOPE_write:iam_scim";

  @Bean
  @Order(2)
  SecurityFilterChain scimSecurityFilterChain(HttpSecurity http) throws Exception {
    BearerTokenAuthenticationEntryPoint bearerEntryPoint =
        new BearerTokenAuthenticationEntryPoint();
    BearerTokenAccessDeniedHandler bearerDenied = new BearerTokenAccessDeniedHandler();
    http.securityMatcher("/scim/v2/**")
        .authorizeHttpRequests(authorize -> authorize.anyRequest().hasAuthority(SCIM_AUTHORITY))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(csrf -> csrf.disable())
        .headers(withDefaults())
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer
                    .jwt(withDefaults())
                    .authenticationEntryPoint(
                        (request, response, ex) -> {
                          bearerEntryPoint.commence(request, response, ex);
                          writeError(response, 401, "authentication required");
                        })
                    .accessDeniedHandler(
                        (request, response, ex) -> {
                          bearerDenied.handle(request, response, ex);
                          writeError(response, 403, "insufficient scope");
                        }));
    return http.build();
  }

  private static void writeError(HttpServletResponse response, int status, String detail)
      throws IOException {
    ErrorResponse error = ScimErrors.error(status, null, detail);
    response.setStatus(status);
    response.setContentType(ScimJsonHttpMessageConverter.SCIM_JSON_VALUE);
    JsonUtils.createObjectMapper().writeValue(response.getOutputStream(), error);
  }
}
