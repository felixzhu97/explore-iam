package com.iam.federation.web;

import java.util.Set;

/** API representation of a registered OIDC client. */
public record ClientResponse(
    String id,
    String clientId,
    String clientName,
    String clientSecret,
    String clientUri,
    Set<String> redirectUris,
    Set<String> postLogoutRedirectUris,
    Set<String> scopes,
    Set<String> responseTypes,
    Set<String> authorizationGrantTypes,
    Set<String> clientAuthenticationMethods) {}
