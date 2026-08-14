package com.iam.identity.infrastructure.security;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads IAM users for Spring Security form login. */
@Service
public class IamUserDetailsService implements UserDetailsService {

  private final IamUserRepository iamUserRepository;

  /**
   * Creates the user-details adapter.
   *
   * @param iamUserRepository IAM user repository
   */
  public IamUserDetailsService(IamUserRepository iamUserRepository) {
    this.iamUserRepository = iamUserRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    IamUser user =
        iamUserRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    return User.builder()
        .username(user.getUsername())
        .password(user.getPasswordHash())
        .disabled(!user.isEnabled())
        .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
        .build();
  }
}
