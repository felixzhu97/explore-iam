package com.iam.common.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/** Session principal for local sign-in that remembers the IAM user id, not only the username. */
public class IamUserDetails extends User {

  private final String userId;

  /**
   * Creates the principal.
   *
   * @param userId IAM user id
   * @param username username at sign-in
   * @param password password hash
   * @param enabled whether the user may sign in
   * @param authorities granted authorities
   */
  public IamUserDetails(
      String userId,
      String username,
      String password,
      boolean enabled,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, enabled, true, true, true, authorities);
    this.userId = userId;
  }

  /**
   * IAM user id bound at sign-in.
   *
   * @return user id
   */
  public String getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof IamUserDetails details
        && super.equals(other)
        && userId.equals(details.userId);
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + userId.hashCode();
  }
}
