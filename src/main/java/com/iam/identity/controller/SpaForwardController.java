package com.iam.identity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the Angular SPA for browser console routes. */
@Controller
public class SpaForwardController {

  /**
   * Forwards console browser routes to the Angular {@code index.html}.
   *
   * @return forward view name
   */
  @GetMapping({
    "/",
    "/login",
    "/apps",
    "/apps/**",
    "/users-and-access",
    "/users-and-access/**",
    "/permissions",
    "/permissions/**",
    "/activity",
    "/activity/**"
  })
  public String forwardSpaRoutes() {
    return "forward:/index.html";
  }
}
