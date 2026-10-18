package com.iam.identity.infra.security;

import static org.springframework.security.config.Customizer.withDefaults;

import com.iam.identity.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Replaces Boot {@code ConditionalOnDefaultWebSecurity} chains so form login can serve the Angular
 * SPA at {@code /login} while keeping the Authorization Server endpoints.
 *
 * @see <a
 *     href="https://docs.spring.io/spring-boot/reference/web/spring-security.html#web.security.oauth2.authorization-server">Authorization
 *     Server</a>
 * @see <a
 *     href="https://docs.spring.io/spring-security/reference/servlet/authentication/mfa.html">Multi-Factor
 *     Authentication</a>
 * @see <a
 *     href="https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa">CSRF
 *     SPA</a>
 */
@Configuration
@EnableWebSecurity
// Empty authorities: enable MFA factor merging on auth filters only; HTTP rules use
// MfaAuthorizationManagers.anyOf(...) so the SAS token endpoint is not forced to MFA.
@EnableMultiFactorAuthentication(authorities = {})
public class WebSecurityConfig {

  @Bean
  @Order(1)
  SecurityFilterChain authorizationServerSecurityFilterChain(
      HttpSecurity http, MfaAuthorizationManagers mfa, UserRepository userRepository)
      throws Exception {
    http.oauth2AuthorizationServer(
        (authorizationServer) -> {
          http.securityMatcher(authorizationServer.getEndpointsMatcher());
          authorizationServer.oidc(withDefaults());
        });
    // Authorize needs a full MFA session; token/client auth stays plain authenticated.
    http.authorizeHttpRequests(
        (authorize) ->
            authorize
                .requestMatchers("/oauth2/authorize")
                .access(mfa.authenticated())
                .anyRequest()
                .authenticated());
    http.oauth2ResourceServer((resourceServer) -> resourceServer.jwt(withDefaults()));
    http.addFilterBefore(new ActiveUserSessionFilter(userRepository), AuthorizationFilter.class);
    http.exceptionHandling(
        (exceptions) ->
            exceptions.defaultAuthenticationEntryPointFor(
                new OAuthAwareLoginEntryPoint(), createHtmlRequestMatcher()));
    return http.build();
  }

  @Bean
  @Order(3)
  SecurityFilterChain defaultSecurityFilterChain(
      HttpSecurity http,
      MfaAuthorizationManagers mfa,
      WebAuthnConfig.WebAuthnProperties webAuthnProperties,
      ObjectProvider<ClientRegistrationRepository> clientRegistrations,
      ObjectProvider<OAuth2UserService<OAuth2UserRequest, OAuth2User>> oauth2UserService,
      UserRepository userRepository)
      throws Exception {
    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfTokenRepository.setCookiePath("/");

    ContinueUrlAuthenticationSuccessHandler successHandler =
        new ContinueUrlAuthenticationSuccessHandler();

    http.authorizeHttpRequests(
            (authorize) ->
                authorize
                    .requestMatchers(
                        "/login",
                        "/login/verify",
                        "/login/ott",
                        "/login/totp",
                        "/login/webauthn",
                        "/ott/sent",
                        "/ott/generate",
                        "/webauthn/authenticate/**",
                        "/api/login/**",
                        "/oauth2/**",
                        "/index.html",
                        "/assets/**",
                        "/*.js",
                        "/*.css",
                        "/*.ico",
                        "/*.svg",
                        "/*.woff2",
                        "/actuator/health",
                        "/actuator/health/**")
                    .permitAll()
                    .anyRequest()
                    .access(mfa.authenticated()))
        .headers(withDefaults())
        .formLogin(
            (form) ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(successHandler)
                    .failureHandler(new LoginAuthenticationFailureHandler())
                    .permitAll())
        .oneTimeTokenLogin(
            (ott) ->
                ott.loginPage("/login/ott")
                    .loginProcessingUrl("/login/ott")
                    .tokenGeneratingUrl("/ott/generate")
                    .showDefaultSubmitPage(false)
                    .successHandler(successHandler))
        .webAuthn(
            (webAuthn) ->
                webAuthn
                    .rpId(webAuthnProperties.rpId())
                    .rpName(webAuthnProperties.rpName())
                    .allowedOrigins(webAuthnProperties.allowedOrigins())
                    .disableDefaultRegistrationPage(false))
        .logout((logout) -> logout.logoutUrl("/logout").logoutSuccessUrl("/login").permitAll())
        .csrf(
            (csrf) ->
                csrf.csrfTokenRepository(csrfTokenRepository)
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
        .addFilterBefore(new ActiveUserSessionFilter(userRepository), AuthorizationFilter.class);

    clientRegistrations.ifAvailable(
        repo -> {
          if (!hasLoginRegistration(repo)) {
            return;
          }
          oauth2UserService.ifAvailable(
              service ->
                  http.oauth2Login(
                      (oauth2) ->
                          oauth2
                              .loginPage("/login")
                              .userInfoEndpoint((userInfo) -> userInfo.userService(service))));
        });

    return http.build();
  }

  private static boolean hasLoginRegistration(ClientRegistrationRepository repo) {
    if (!(repo instanceof Iterable<?> registrations)) {
      return true;
    }
    for (Object registration : registrations) {
      if (registration instanceof ClientRegistration client
          && AuthorizationGrantType.AUTHORIZATION_CODE.equals(
              client.getAuthorizationGrantType())) {
        return true;
      }
    }
    return false;
  }

  private static MediaTypeRequestMatcher createHtmlRequestMatcher() {
    MediaTypeRequestMatcher matcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
    matcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
    return matcher;
  }

  private static final class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
      CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
      if (csrfToken != null) {
        csrfToken.getToken();
      }
      filterChain.doFilter(request, response);
    }
  }

  private static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestAttributeHandler plain = new CsrfTokenRequestAttributeHandler();
    private final XorCsrfTokenRequestAttributeHandler xor =
        new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
        HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
      this.xor.handle(request, response, csrfToken);
      csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
      // CookieCsrfTokenRepository stores the raw token; the SPA copies it into
      // X-XSRF-TOKEN or _csrf. XOR decoding applies only to BREACH-masked body
      // tokens, so always resolve with the plain handler for cookie-based clients.
      return this.plain.resolveCsrfTokenValue(request, csrfToken);
    }
  }
}
