package com.iam.identity.controller;

import com.iam.identity.service.TotpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes safe, non-secret client metadata so the shared login page can distinguish direct IAM
 * login from OAuth authorize redirects, plus MFA factor availability after password sign-in.
 */
@RestController
@RequestMapping("/api/login")
public class LoginContextController {

  private final RegisteredClientRepository registeredClientRepository;
  private final TotpService totpService;
  private final PublicKeyCredentialUserEntityRepository userEntityRepository;
  private final UserCredentialRepository userCredentialRepository;

  /**
   * Creates the login-context API.
   *
   * @param registeredClientRepository Authorization Server client store
   * @param totpService TOTP enrollment status
   * @param userEntityRepository WebAuthn user entities
   * @param userCredentialRepository WebAuthn credentials
   */
  public LoginContextController(
      RegisteredClientRepository registeredClientRepository,
      TotpService totpService,
      PublicKeyCredentialUserEntityRepository userEntityRepository,
      UserCredentialRepository userCredentialRepository) {
    this.registeredClientRepository = registeredClientRepository;
    this.totpService = totpService;
    this.userEntityRepository = userEntityRepository;
    this.userCredentialRepository = userCredentialRepository;
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

  /**
   * Returns which second factors the signed-in (password) user can use.
   *
   * @param authentication current authentication (password factor required)
   * @return factor availability
   */
  @GetMapping("/mfa-options")
  public ResponseEntity<LoginMfaOptionsResponse> mfaOptions(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || !StringUtils.hasText(authentication.getName())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String username = authentication.getName();
    boolean totpEnabled = this.totpService.isEnabled(username);
    boolean passkeysAvailable = hasPasskey(username);
    return ResponseEntity.ok(new LoginMfaOptionsResponse(totpEnabled, passkeysAvailable, true));
  }

  private boolean hasPasskey(String username) {
    PublicKeyCredentialUserEntity entity = this.userEntityRepository.findByUsername(username);
    if (entity == null || entity.getId() == null) {
      return false;
    }
    return !this.userCredentialRepository.findByUserId(entity.getId()).isEmpty();
  }
}
