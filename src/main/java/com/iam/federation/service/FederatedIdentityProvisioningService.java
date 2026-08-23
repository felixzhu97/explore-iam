package com.iam.federation.service;

import com.iam.federation.domain.model.FederatedIdentityLink;
import com.iam.federation.domain.repository.FederatedIdentityLinkRepository;
import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Provisions or resolves local IAM users for federated IdP logins. */
@Service
public class FederatedIdentityProvisioningService {

  private final FederatedIdentityLinkRepository linkRepository;
  private final IamUserRepository iamUserRepository;

  /**
   * Creates the provisioning service.
   *
   * @param linkRepository federated identity link repository
   * @param iamUserRepository IAM user repository
   */
  public FederatedIdentityProvisioningService(
      FederatedIdentityLinkRepository linkRepository, IamUserRepository iamUserRepository) {
    this.linkRepository = linkRepository;
    this.iamUserRepository = iamUserRepository;
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
  public IamUser resolveOrProvision(String provider, String subject, String email) {
    Optional<FederatedIdentityLink> existing =
        linkRepository.findByProviderAndExternalSubject(provider, subject);
    if (existing.isPresent()) {
      return iamUserRepository
          .findById(existing.get().getIamUserId())
          .orElseThrow(() -> new IllegalStateException("linked user missing"));
    }
    String username = provider + ":" + subject;
    IamUser user =
        iamUserRepository
            .findByUsername(username)
            .orElseGet(
                () ->
                    iamUserRepository.save(
                        IamUser.createForFederatedLogin(provider, subject, email)));
    linkRepository.save(FederatedIdentityLink.create(user.getId(), provider, subject));
    return user;
  }
}
