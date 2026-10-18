package com.iam.federation.domain.event;

/**
 * OAuth client scopes changed event.
 *
 * @param clientId public OAuth client_id
 */
public record OAuthClientScopesChangedEvent(String clientId) {}
