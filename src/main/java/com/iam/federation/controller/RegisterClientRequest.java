package com.iam.federation.controller;

import java.util.List;

/** Request body for registering a new OIDC client. */
public record RegisterClientRequest(
    String clientName,
    List<String> redirectUris,
    List<String> postLogoutRedirectUris,
    List<String> scopes,
    List<String> responseTypes,
    List<String> authorizationGrantTypes,
    List<String> clientAuthenticationMethods,
    String clientUri) {}
