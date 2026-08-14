package com.iam.identity.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.StringUtils;

/**
 * Sends unauthenticated browser users to the Angular login SPA, forwarding {@code client_id} from
 * the OAuth authorize request so the shared login page can show RP context.
 *
 * <p>Never forwards {@code client_secret} — secrets belong only on the token endpoint.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
final class OAuthAwareLoginEntryPoint implements AuthenticationEntryPoint {

  private final LoginUrlAuthenticationEntryPoint delegate =
      new LoginUrlAuthenticationEntryPoint("/login");

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    String clientId = request.getParameter("client_id");
    if (!StringUtils.hasText(clientId)) {
      this.delegate.commence(request, response, authException);
      return;
    }
    String target = "/login?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8);
    response.sendRedirect(target);
  }
}
