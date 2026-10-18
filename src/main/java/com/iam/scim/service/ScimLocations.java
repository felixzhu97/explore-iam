package com.iam.scim.service;

/**
 * Absolute endpoint URLs used for {@code meta.location} and member {@code $ref}.
 *
 * @param users URL of {@code /scim/v2/Users}
 * @param groups URL of {@code /scim/v2/Groups}
 */
public record ScimLocations(String users, String groups) {}
