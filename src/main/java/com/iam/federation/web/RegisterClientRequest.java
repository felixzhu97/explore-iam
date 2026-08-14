package com.iam.federation.web;

import java.util.List;

public record RegisterClientRequest(
        String clientName,
        List<String> redirectUris,
        List<String> postLogoutRedirectUris,
        List<String> scopes,
        List<String> responseTypes,
        List<String> authorizationGrantTypes,
        List<String> clientAuthenticationMethods,
        String clientUri) {}
