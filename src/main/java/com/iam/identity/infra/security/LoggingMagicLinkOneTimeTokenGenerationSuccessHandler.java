package com.iam.identity.infra.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Delivers the OTT as a magic link. Local/dev logs the link; production can swap in mail
 * without changing the Security DSL.
 *
 * @see <a
 *     href="https://docs.spring.io/spring-security/reference/servlet/authentication/onetimetoken.html">One-Time
 *     Token Login</a>
 */
@Component
class LoggingMagicLinkOneTimeTokenGenerationSuccessHandler
    implements OneTimeTokenGenerationSuccessHandler {

  private static final Logger log =
      LoggerFactory.getLogger(LoggingMagicLinkOneTimeTokenGenerationSuccessHandler.class);

  private final OneTimeTokenGenerationSuccessHandler redirectHandler =
      new RedirectOneTimeTokenGenerationSuccessHandler("/ott/sent");

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken)
      throws IOException, ServletException {
    String magicLink =
        UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
            .replacePath(request.getContextPath())
            .replaceQuery(null)
            .fragment(null)
            .path("/login/ott")
            .queryParam("token", oneTimeToken.getTokenValue())
            .toUriString();
    log.info(
        "OTT magic link for username={} (dev delivery): {}",
        oneTimeToken.getUsername(),
        magicLink);
    this.redirectHandler.handle(request, response, oneTimeToken);
  }
}
