package com.iam.identity.infra.security;

import com.iam.common.security.SecurityRoles;
import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import com.iam.identity.domain.repository.RoleRepository;
import java.util.ArrayList;
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
  private final RoleRepository roleRepository;

  /**
   * Creates the user-details adapter.
   *
   * @param iamUserRepository IAM user repository
   * @param roleRepository role repository for RBAC authorities
   */
  public IamUserDetailsService(
      IamUserRepository iamUserRepository, RoleRepository roleRepository) {
    this.iamUserRepository = iamUserRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    IamUser user =
        iamUserRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(SecurityRoles.USER));
    roleRepository
        .findByUserId(user.getId())
        .forEach(role -> authorities.add(new SimpleGrantedAuthority(role.authority())));
    return User.builder()
        .username(user.getUsername())
        .password(user.encodedPasswordHash())
        .disabled(!user.isLoginEnabled())
        .authorities(authorities)
        .build();
  }
}
