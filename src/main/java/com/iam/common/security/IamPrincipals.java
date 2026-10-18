package com.iam.common.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

/**
 * Reads the IAM user id bound to a signed-in principal. Users are resolved by id, never by
 * username, so a renamed or reused username cannot move a session to another person.
 */
public final class IamPrincipals {

  /** OAuth2 login attribute carrying the IAM user id of a federated sign-in. */
  public static final String USER_ID_ATTRIBUTE = "iam_user_id";

  private IamPrincipals() {}

  /**
   * Returns the IAM user id of a signed-in principal.
   *
   * @param authentication current authentication, may be {@code null}
   * @return user id when the principal was created by IAM sign-in
   */
  public static Optional<String> userId(Authentication authentication) {
    if (authentication == null) {
      return Optional.empty();
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof IamUserDetails details) {
      return Optional.of(details.getUserId());
    }
    if (principal instanceof OAuth2AuthenticatedPrincipal oauth) {
      return Optional.ofNullable(oauth.getAttribute(USER_ID_ATTRIBUTE)).map(Object::toString);
    }
    return Optional.empty();
  }
}
