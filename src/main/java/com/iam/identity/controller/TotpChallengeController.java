package com.iam.identity.controller;

import com.iam.identity.infra.security.ContinueUrlAuthenticationSuccessHandler;
import com.iam.identity.service.TotpService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Completes MFA with a TOTP (or backup) code after password authentication. */
@Controller
public class TotpChallengeController {

  private final TotpService totpService;
  private final ContinueUrlAuthenticationSuccessHandler successHandler =
      new ContinueUrlAuthenticationSuccessHandler();
  private final SecurityContextRepository securityContextRepository =
      new HttpSessionSecurityContextRepository();

  TotpChallengeController(TotpService totpService) {
    this.totpService = totpService;
  }

  /**
   * Verifies the TOTP challenge and grants {@code FACTOR_TOTP}.
   *
   * @param code authenticator or backup code
   * @param authentication current authentication (must include password factor)
   * @param request HTTP request
   * @param response HTTP response
   */
  @PostMapping("/login/totp")
  public void challenge(
      @RequestParam("code") String code,
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {
    if (authentication == null || !authentication.isAuthenticated()) {
      response.sendRedirect("/login/totp?error");
      return;
    }
    if (!totpService.verifyChallenge(authentication.getName(), code)) {
      response.sendRedirect("/login/totp?error");
      return;
    }
    Authentication upgraded =
        authentication
            .toBuilder()
            .authorities(
                (current) -> {
                  Set<GrantedAuthority> merged = new LinkedHashSet<>(current);
                  merged.add(FactorGrantedAuthority.fromFactor("TOTP"));
                  current.clear();
                  current.addAll(merged);
                })
            .build();
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(upgraded);
    SecurityContextHolder.setContext(context);
    this.securityContextRepository.saveContext(context, request, response);
    this.successHandler.onAuthenticationSuccess(request, response, upgraded);
  }
}
