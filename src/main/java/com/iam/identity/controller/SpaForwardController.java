package com.iam.identity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the Angular SPA for browser routes (login + client registration). */
@Controller
public class SpaForwardController {

  /**
   * Forwards browser console routes to the Angular {@code index.html}.
   *
   * @return forward view name
   */
  @GetMapping({
    "/",
    "/login",
    "/login/verify",
    "/login/ott",
    "/login/totp",
    "/ott/sent",
    "/oauth-clients",
    "/oauth-clients/**",
    "/clients",
    "/clients/**",
    "/users",
    "/security/passkeys",
    "/security/totp"
  })
  public String forwardSpaRoutes() {
    return "forward:/index.html";
  }
}
