package com.iam.federation.infra.security;

import com.iam.common.security.SecurityRoles;
import com.iam.federation.service.FederationLinkService;
import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.RoleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/** Maps external OAuth2 logins to local IAM users via federated identity links. */
@Service
@ConditionalOnBean(ClientRegistrationRepository.class)
public class FederatedLoginService extends DefaultOAuth2UserService {

  private final FederationLinkService federationLinkService;
  private final RoleRepository roleRepository;

  /**
   * Creates the federated login service.
   *
   * @param federationLinkService federation link service
   * @param roleRepository role repository
   */
  public FederatedLoginService(
      FederationLinkService federationLinkService, RoleRepository roleRepository) {
    this.federationLinkService = federationLinkService;
    this.roleRepository = roleRepository;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauthUser = super.loadUser(userRequest);
    String provider = userRequest.getClientRegistration().getRegistrationId();
    String subject = oauthUser.getName();
    String email =
        Optional.ofNullable(oauthUser.getAttribute("email")).map(Object::toString).orElse(null);
    IamUser iamUser = federationLinkService.resolveOrProvision(provider, subject, email);
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(SecurityRoles.USER));
    roleRepository
        .findByUserId(iamUser.getId())
        .forEach(role -> authorities.add(new SimpleGrantedAuthority(role.authority())));
    return new DefaultOAuth2User(authorities, oauthUser.getAttributes(), "sub");
  }
}
