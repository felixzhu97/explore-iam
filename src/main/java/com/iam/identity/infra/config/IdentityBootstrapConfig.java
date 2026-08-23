package com.iam.identity.infra.config;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.model.TrustPolicyDocument;
import com.iam.identity.domain.repository.IamUserRepository;
import com.iam.identity.domain.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Seeds the demo IAM user, admin role, and exposes the shared password encoder. */
@Configuration
@EnableConfigurationProperties(DemoUserProperties.class)
public class IdentityBootstrapConfig {

  private static final Logger log = LoggerFactory.getLogger(IdentityBootstrapConfig.class);
  private static final String IAM_ADMIN_ROLE = "IAM_ADMIN";

  @Bean
  PasswordEncoder passwordEncoder() {
    return org.springframework.security.crypto.factory.PasswordEncoderFactories
        .createDelegatingPasswordEncoder();
  }

  @Bean
  ApplicationRunner seedDemoUser(
      IamUserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      DemoUserProperties properties) {
    return (ApplicationArguments args) -> {
      Role adminRole = ensureAdminRole(roleRepository);
      if (!properties.isEnabled()) {
        return;
      }
      IamUser user =
          userRepository
              .findByUsername(properties.getUsername())
              .orElseGet(
                  () -> {
                    IamUser created =
                        IamUser.create(
                            properties.getUsername(),
                            properties.getEmail(),
                            passwordEncoder.encode(properties.getPassword()));
                    IamUser saved = userRepository.save(created);
                    log.info("Seeded demo IAM User '{}'", properties.getUsername());
                    return saved;
                  });
      roleRepository.assignToUser(user.getId(), adminRole.getId());
    };
  }

  private static Role ensureAdminRole(RoleRepository roleRepository) {
    return roleRepository.findAll().stream()
        .filter(role -> IAM_ADMIN_ROLE.equals(role.getName()))
        .findFirst()
        .orElseGet(
            () -> {
              Role created =
                  roleRepository.save(
                      Role.create(IAM_ADMIN_ROLE, TrustPolicyDocument.allowAll()));
              log.info("Seeded IAM role '{}'", IAM_ADMIN_ROLE);
              return created;
            });
  }
}
