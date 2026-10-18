package com.iam.scim.infra.config;

import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.repository.GroupRepository;
import com.iam.scim.domain.model.ProvisioningTarget;
import com.iam.scim.domain.repository.ProvisioningTargetRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

/**
 * Outbound SCIM wiring: async Event Publication Registry listeners, method retries, the {@code
 * client_credentials} client manager, Provisioning Target bootstrap, and resubmission of failed
 * pushes.
 */
@Configuration
@EnableAsync
@EnableScheduling
@EnableResilientMethods
@EnableConfigurationProperties(ScimTargetProperties.class)
public class ScimOutboundConfig {

  private static final Logger log = LoggerFactory.getLogger(ScimOutboundConfig.class);

  /**
   * Client manager usable outside a servlet request (async listeners).
   *
   * @param registrations client registrations
   * @param authorizedClients authorized client store
   * @return manager for {@code client_credentials}
   */
  @Bean
  OAuth2AuthorizedClientManager scimAuthorizedClientManager(
      ClientRegistrationRepository registrations,
      OAuth2AuthorizedClientService authorizedClients) {
    AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
        new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, authorizedClients);
    manager.setAuthorizedClientProvider(
        OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
    return manager;
  }

  @Bean
  ApplicationRunner seedProvisioningTargets(
      ScimTargetProperties properties,
      ProvisioningTargetRepository targetRepository,
      GroupRepository groupRepository,
      TransactionTemplate transactionTemplate) {
    return args ->
        transactionTemplate.executeWithoutResult(
            status -> {
              for (ScimTargetProperties.Target config : properties.getTargets()) {
                seedTarget(config, targetRepository, groupRepository);
              }
            });
  }

  @Bean
  ProvisioningResubmitter provisioningResubmitter(
      IncompleteEventPublications incompletePublications, ScimTargetProperties properties) {
    return new ProvisioningResubmitter(incompletePublications, properties);
  }

  private static void seedTarget(
      ScimTargetProperties.Target config,
      ProvisioningTargetRepository targetRepository,
      GroupRepository groupRepository) {
    if (!StringUtils.hasText(config.getName()) || !StringUtils.hasText(config.getBaseUrl())) {
      log.info("Skipping Provisioning Target without name or base-url");
      return;
    }
    Optional<String> assignmentGroupId = Optional.empty();
    boolean assignmentMissing = false;
    if (StringUtils.hasText(config.getAssignmentGroup())) {
      assignmentGroupId =
          groupRepository.findByName(config.getAssignmentGroup()).map(Group::getId);
      assignmentMissing = assignmentGroupId.isEmpty();
    }
    String groupId = assignmentGroupId.orElse(null);
    ProvisioningTarget target =
        targetRepository
            .findByName(config.getName())
            .map(
                existing -> {
                  existing.reconfigure(config.getBaseUrl(), config.getRegistrationId(), groupId);
                  return existing;
                })
            .orElseGet(
                () ->
                    ProvisioningTarget.createProvisioningTarget(
                        config.getName(),
                        config.getBaseUrl(),
                        config.getRegistrationId(),
                        groupId));
    if (config.isEnabled() && !assignmentMissing) {
      target.enableTarget();
    } else {
      target.disableTarget();
    }
    if (assignmentMissing) {
      log.warn(
          "Provisioning Target '{}' disabled: assignment group '{}' not found",
          config.getName(),
          config.getAssignmentGroup());
    }
    targetRepository.save(target);
    log.info("Provisioning Target '{}' enabled={}", target.getName(), target.isEnabled());
  }

  /** Resubmits pushes whose listener failed, e.g. while a target was down. */
  static class ProvisioningResubmitter {

    private final IncompleteEventPublications incompletePublications;
    private final ScimTargetProperties properties;

    ProvisioningResubmitter(
        IncompleteEventPublications incompletePublications, ScimTargetProperties properties) {
      this.incompletePublications = incompletePublications;
      this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.scim.resubmit-interval:PT5M}")
    void resubmit() {
      incompletePublications.resubmitIncompletePublicationsOlderThan(
          properties.getResubmitOlderThan());
    }
  }
}
