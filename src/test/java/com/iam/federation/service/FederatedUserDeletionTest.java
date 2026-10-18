package com.iam.federation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.federation.domain.repository.FederatedIdentityRepository;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.UserRepository;
import com.iam.identity.service.UserProvisioningService;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("Federated user deletion")
class FederatedUserDeletionTest {

  @Autowired private FederationLinkService federationLinkService;

  @Autowired private UserProvisioningService userProvisioningService;

  @Autowired private FederatedIdentityRepository linkRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("should remove federated links when the linked user is deleted")
  void shouldRemoveFederatedLinksWhenTheLinkedUserIsDeleted() {
    String subject = "sub-" + UUID.randomUUID();
    User user = federationLinkService.resolveOrProvision("google", subject, null);
    entityManager.flush();

    userProvisioningService.deleteUser(user.getId(), null);
    entityManager.flush();

    assertThat(linkRepository.findByProviderAndExternalSubject("google", subject)).isEmpty();
    assertThat(userRepository.findById(user.getId())).isEmpty();
  }
}
