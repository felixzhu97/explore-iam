package com.iam.identity.infra.security;

import com.iam.common.security.SecurityRoles;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.RoleRepository;
import com.iam.identity.domain.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads IAM users for Spring Security form login. */
@Service
public class DirectoryUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  /**
   * Creates the user-details adapter.
   *
   * @param userRepository IAM user repository
   * @param roleRepository role repository for RBAC authorities
   */
  public DirectoryUserDetailsService(
      UserRepository userRepository, RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(SecurityRoles.USER));
    roleRepository
        .findByUserId(user.getId())
        .forEach(role -> authorities.add(new SimpleGrantedAuthority(role.authority())));
    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUsername())
        .password(user.encodedPasswordHash())
        .disabled(!user.isLoginEnabled())
        .authorities(authorities)
        .build();
  }
}
