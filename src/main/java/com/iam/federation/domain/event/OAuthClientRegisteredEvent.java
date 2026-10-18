package com.iam.federation.domain.event;

/**
 * OAuth client registered event.
 *
 * @param clientId public OAuth client_id
 */
public record OAuthClientRegisteredEvent(String clientId) {}
