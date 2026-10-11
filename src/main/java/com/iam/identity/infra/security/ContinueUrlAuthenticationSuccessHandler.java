package com.iam.identity.infra.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

/**
 * Prefers an explicit safe relative {@code continue} form field (SPA deep link), otherwise the
 * saved request (e.g. OAuth authorize or {@code /clients}).
 *
 * <p>After password-only authentication, redirects to {@code /login/verify} so the second factor
 * runs immediately (Apple ID / Google 2SV style) instead of landing on the console shell.
 */
public final class ContinueUrlAuthenticationSuccessHandler
    extends SavedRequestAwareAuthenticationSuccessHandler {

  /** Session key for a safe relative continue path across MFA steps. */
  public static final String MFA_CONTINUE_ATTR = "explore-iam.mfa-continue";

  private static final String FACTOR_TOTP =
      FactorGrantedAuthority.fromFactor("TOTP").getAuthority();

  /** Creates the default continue-aware success handler. */
  public ContinueUrlAuthenticationSuccessHandler() {
    setDefaultTargetUrl("/");
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws ServletException, IOException {
    String continueUrl = resolveContinueUrl(request);

    if (!hasSecondFactor(authentication)) {
      rememberContinue(request, continueUrl);
      clearAuthenticationAttributes(request);
      getRedirectStrategy().sendRedirect(request, response, buildVerifyUrl(continueUrl));
      return;
    }

    if (isSafeRelativePath(continueUrl)) {
      clearMfaContinue(request);
      clearAuthenticationAttributes(request);
      getRedirectStrategy().sendRedirect(request, response, continueUrl.trim());
      return;
    }
    clearMfaContinue(request);
    super.onAuthenticationSuccess(request, response, authentication);
  }

  private static String buildVerifyUrl(String continueUrl) {
    if (!isSafeRelativePath(continueUrl)) {
      return "/login/verify";
    }
    return "/login/verify?continue="
        + URLEncoder.encode(continueUrl.trim(), StandardCharsets.UTF_8);
  }

  private static String resolveContinueUrl(HttpServletRequest request) {
    String continueUrl = request.getParameter("continue");
    if (isSafeRelativePath(continueUrl)) {
      return continueUrl.trim();
    }
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }
    Object stored = session.getAttribute(MFA_CONTINUE_ATTR);
    return stored instanceof String value ? value : null;
  }

  private static void rememberContinue(HttpServletRequest request, String continueUrl) {
    if (!isSafeRelativePath(continueUrl)) {
      return;
    }
    request.getSession(true).setAttribute(MFA_CONTINUE_ATTR, continueUrl.trim());
  }

  private static void clearMfaContinue(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.removeAttribute(MFA_CONTINUE_ATTR);
    }
  }

  private static boolean hasSecondFactor(Authentication authentication) {
    if (authentication == null) {
      return false;
    }
    for (GrantedAuthority authority : authentication.getAuthorities()) {
      String value = authority.getAuthority();
      if (FactorGrantedAuthority.OTT_AUTHORITY.equals(value)
          || FactorGrantedAuthority.WEBAUTHN_AUTHORITY.equals(value)
          || FACTOR_TOTP.equals(value)) {
        return true;
      }
    }
    return false;
  }

  static boolean isSafeRelativePath(String path) {
    if (!StringUtils.hasText(path)) {
      return false;
    }
    String value = path.trim();
    return value.startsWith("/")
        && !value.startsWith("//")
        && !value.contains("://")
        && !value.contains("\\");
  }
}
