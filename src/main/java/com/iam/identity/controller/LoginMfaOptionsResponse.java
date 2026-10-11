package com.iam.identity.controller;

/**
 * Second-factor options available after password authentication.
 *
 * @param totpEnabled whether the user has an enabled authenticator
 * @param passkeysAvailable whether the user has a registered passkey
 * @param ottAvailable whether one-time token (email/magic link) is offered
 */
public record LoginMfaOptionsResponse(
    boolean totpEnabled, boolean passkeysAvailable, boolean ottAvailable) {}
