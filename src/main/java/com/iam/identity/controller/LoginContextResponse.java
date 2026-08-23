package com.iam.identity.controller;

/**
 * Public login context for the shared Angular login SPA.
 *
 * @param clientId registered OAuth client id, or {@code null} for direct IAM login
 * @param clientName human-readable RP name when known
 * @param oauth whether this login continues an OAuth authorization request
 */
public record LoginContextResponse(String clientId, String clientName, boolean oauth) {}
