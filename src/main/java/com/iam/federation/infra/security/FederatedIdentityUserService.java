package com.iam.federation.infra.security;

import com.iam.common.security.SecurityRoles;
import com.iam.federation.domain.model.FederatedIdentityLink;
import com.iam.federation.domain.repository.FederatedIdentityLinkRepository;
import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
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

/** Resolves external IdP logins to local IAM users via federated identity links. */
@Service
@ConditionalOnBean(ClientRegistrationRepository.class)
public class FederatedIdentityUserService extends DefaultOAuth2UserService {

  private final FederatedIdentityLinkRepository linkRepository;
  private final IamUserRepository iamUserRepository;
  private final RoleRepository roleRepository;

  /**
   * Creates the federated user service.
   *
   * @param linkRepository federated identity link repository
   * @param iamUserRepository IAM user repository
   * @param roleRepository role repository
   */
  public FederatedIdentityUserService(
      FederatedIdentityLinkRepository linkRepository,
      IamUserRepository iamUserRepository,
      RoleRepository roleRepository) {
    this.linkRepository = linkRepository;
    this.iamUserRepository = iamUserRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauthUser = super.loadUser(userRequest);
    String provider = userRequest.getClientRegistration().getRegistrationId();
    String subject = oauthUser.getName();
    IamUser iamUser = resolveIamUser(provider, subject, oauthUser);
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(SecurityRoles.USER));
    roleRepository
        .findByUserId(iamUser.getId())
        .forEach(role -> authorities.add(new SimpleGrantedAuthority(role.authority())));
    return new DefaultOAuth2User(authorities, oauthUser.getAttributes(), "sub");
  }

  private IamUser resolveIamUser(String provider, String subject, OAuth2User oauthUser) {
    Optional<FederatedIdentityLink> existing =
        linkRepository.findByProviderAndExternalSubject(provider, subject);
    if (existing.isPresent()) {
      return iamUserRepository
          .findById(existing.get().getIamUserId())
          .orElseThrow(() -> new OAuth2AuthenticationException("linked user missing"));
    }
    String username = provider + ":" + subject;
    String email =
        Optional.ofNullable(oauthUser.getAttribute("email")).map(Object::toString).orElse(null);
    IamUser user =
        iamUserRepository
            .findByUsername(username)
            .orElseGet(
                () ->
                    iamUserRepository.save(
                        IamUser.create(username, email, "{noop}federated-no-password")));
    linkRepository.save(FederatedIdentityLink.create(user.getId(), provider, subject));
    return user;
  }
}
