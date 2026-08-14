package com.iam.identity.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes safe, non-secret client metadata so the shared login page can distinguish direct IAM
 * login from OAuth authorize redirects.
 */
@RestController
@RequestMapping("/api/login")
public class LoginContextController {

  private final RegisteredClientRepository registeredClientRepository;

  /**
   * Creates the login-context API.
   *
   * @param registeredClientRepository Authorization Server client store
   */
  public LoginContextController(RegisteredClientRepository registeredClientRepository) {
    this.registeredClientRepository = registeredClientRepository;
  }

  /**
   * Returns safe client metadata for the login page.
   *
   * @param clientId optional OAuth client_id from the authorize redirect
   * @return login context payload
   */
  @GetMapping("/context")
  public ResponseEntity<LoginContextResponse> context(
      @RequestParam(value = "client_id", required = false) String clientId) {
    if (!StringUtils.hasText(clientId)) {
      return ResponseEntity.ok(new LoginContextResponse(null, null, false));
    }

    RegisteredClient client = this.registeredClientRepository.findByClientId(clientId.trim());
    if (client == null) {
      return ResponseEntity.ok(new LoginContextResponse(clientId.trim(), null, true));
    }

    String name =
        StringUtils.hasText(client.getClientName()) ? client.getClientName() : client.getClientId();
    return ResponseEntity.ok(new LoginContextResponse(client.getClientId(), name, true));
  }
}
