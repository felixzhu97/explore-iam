package com.iam.federation.controller;

import com.iam.federation.service.OAuthClientService;
import com.iam.federation.service.OAuthClientService.OAuthClientView;
import com.iam.federation.service.OAuthClientService.CreateOAuthClientCommand;
import com.iam.federation.service.OAuthClientService.RegisteredOAuthClientResult;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP API for listing and registering OIDC clients. */
@RestController
@RequestMapping("/api/v1/oauthClients")
public class OAuthClientController {

  private final OAuthClientService oauthClientService;

  /**
   * Creates the OIDC client API controller.
   *
   * @param oauthClientService OIDC client service
   */
  public OAuthClientController(OAuthClientService oauthClientService) {
    this.oauthClientService = oauthClientService;
  }

  /**
   * Registers a new OIDC client and returns the one-time plaintext secret.
   *
   * @param request registration payload
   * @return created client response
   */
  @PostMapping
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<OAuthClientResponse> createOAuthClient(@RequestBody CreateOAuthClientRequest request) {
    RegisteredOAuthClientResult result =
        oauthClientService.createOAuthClient(
            new CreateOAuthClientCommand(
                request.clientName(),
                request.redirectUris(),
                request.postLogoutRedirectUris(),
                request.scopes(),
                request.responseTypes(),
                request.authorizationGrantTypes(),
                request.clientAuthenticationMethods(),
                request.clientUri()));
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
  }

  /**
   * Lists all registered OIDC clients (secrets omitted).
   *
   * @return client summaries
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public List<OAuthClientResponse> listOAuthClients() {
    return oauthClientService.findAll().stream().map(OAuthClientController::toResponse).toList();
  }

  /**
   * Returns one client by public client_id.
   *
   * @param clientId public client_id
   * @return client when found
   */
  @GetMapping("/{clientId}")
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public ResponseEntity<OAuthClientResponse> getOAuthClient(@PathVariable String clientId) {
    return oauthClientService
        .findByClientId(clientId)
        .map(view -> ResponseEntity.ok(toResponse(view)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Replaces client scopes (AIP-136).
   *
   * @param clientId public client_id
   * @param request scopes payload
   * @return updated client
   */
  @PostMapping("/{clientId}:replaceScopes")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public OAuthClientResponse replaceOAuthClientScopes(
      @PathVariable String clientId, @RequestBody UpdateScopesRequest request) {
    return toResponse(oauthClientService.replaceOAuthClientScopes(clientId, request.scopes()));
  }

  private static OAuthClientResponse toResponse(RegisteredOAuthClientResult result) {
    return new OAuthClientResponse(
        result.id(),
        result.clientId(),
        result.clientName(),
        result.clientSecret(),
        result.clientUri(),
        result.redirectUris(),
        result.postLogoutRedirectUris(),
        result.scopes(),
        result.responseTypes(),
        result.authorizationGrantTypes(),
        result.clientAuthenticationMethods());
  }

  private static OAuthClientResponse toResponse(OAuthClientView view) {
    return new OAuthClientResponse(
        view.id(),
        view.clientId(),
        view.clientName(),
        null,
        view.clientUri(),
        view.redirectUris(),
        view.postLogoutRedirectUris(),
        view.scopes(),
        view.responseTypes(),
        view.authorizationGrantTypes(),
        view.clientAuthenticationMethods());
  }
}
