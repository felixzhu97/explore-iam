package com.iam.federation.service;

import com.iam.federation.domain.model.FederatedIdentity;
import com.iam.federation.domain.repository.FederatedIdentityRepository;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Resolves or creates IAM users and federated identity links for external IdP logins. */
@Service
@Transactional(readOnly = true)
public class FederationLinkService {

  private final FederatedIdentityRepository linkRepository;
  private final UserRepository userRepository;

  /**
   * Creates the federation link service.
   *
   * @param linkRepository federated identity link repository
   * @param userRepository IAM user repository
   */
  public FederationLinkService(
      FederatedIdentityRepository linkRepository, UserRepository userRepository) {
    this.linkRepository = linkRepository;
    this.userRepository = userRepository;
  }

  /**
   * Resolves an existing linked user or provisions a new user and link.
   *
   * @param provider external identity provider id
   * @param subject external subject identifier
   * @param email optional email from the IdP
   * @return local IAM user
   */
  @Transactional
  public User resolveOrProvision(String provider, String subject, String email) {
    Optional<FederatedIdentity> existing =
        linkRepository.findByProviderAndExternalSubject(provider, subject);
    if (existing.isPresent()) {
      return userRepository
          .findById(existing.get().linkedUserId())
          .orElseThrow(() -> new IllegalStateException("linked user missing"));
    }
    String username = provider + ":" + subject;
    User user =
        userRepository
            .findByUsername(username)
            .orElseGet(
                () ->
                    userRepository.save(
                        User.createForFederatedLogin(provider, subject, email)));
    linkRepository.save(FederatedIdentity.create(user.getId(), provider, subject));
    return user;
  }
}
