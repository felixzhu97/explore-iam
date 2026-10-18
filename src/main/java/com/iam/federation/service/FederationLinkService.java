package com.iam.federation.service;

import com.iam.federation.domain.model.FederatedIdentity;
import com.iam.federation.domain.repository.FederatedIdentityRepository;
import com.iam.identity.domain.event.UserProvisionedEvent;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.UserRepository;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Resolves or creates IAM users and federated identity links for external IdP logins. */
@Service
@Transactional(readOnly = true)
public class FederationLinkService {

  private final FederatedIdentityRepository linkRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Creates the federation link service.
   *
   * @param linkRepository federated identity link repository
   * @param userRepository IAM user repository
   * @param eventPublisher domain event publisher
   */
  public FederationLinkService(
      FederatedIdentityRepository linkRepository,
      UserRepository userRepository,
      ApplicationEventPublisher eventPublisher) {
    this.linkRepository = linkRepository;
    this.userRepository = userRepository;
    this.eventPublisher = eventPublisher;
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
          .findById(existing.get().getUserId())
          .orElseThrow(() -> new IllegalStateException("linked user missing"));
    }
    String username = provider + ":" + subject;
    User user =
        userRepository
            .findByUsername(username)
            .orElseGet(() -> provisionUser(provider, subject, email));
    linkRepository.save(FederatedIdentity.createFederatedIdentity(user.getId(), provider, subject));
    return user;
  }

  private User provisionUser(String provider, String subject, String email) {
    User user = userRepository.save(User.createFederatedUser(provider, subject, email));
    eventPublisher.publishEvent(new UserProvisionedEvent(user.getId()));
    return user;
  }
}
