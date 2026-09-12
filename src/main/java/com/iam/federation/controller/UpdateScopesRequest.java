package com.iam.federation.controller;

import java.util.List;

/** Request body for replacing OIDC client scopes. */
public record UpdateScopesRequest(List<String> scopes) {}
