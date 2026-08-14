package com.iam.identity.web;

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
  @GetMapping({"/", "/login", "/clients", "/clients/**"})
  public String forwardSpaRoutes() {
    return "forward:/index.html";
  }
}
