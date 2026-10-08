package com.iam.identity.infra.config;

import com.iam.identity.domain.model.ImpersonationPolicy;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.RoleRepository;
import com.iam.identity.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      DemoUserProperties properties,
      PlatformTransactionManager transactionManager) {
    TransactionTemplate tx = new TransactionTemplate(transactionManager);
    return (ApplicationArguments args) ->
        tx.executeWithoutResult(
            status ->
                seedDemoUserInTransaction(
                    userRepository, roleRepository, passwordEncoder, properties));
  }

  private static void seedDemoUserInTransaction(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      DemoUserProperties properties) {
    Role adminRole = ensureAdminRole(roleRepository);
    if (!properties.isEnabled()) {
      return;
    }
    User user =
        userRepository
            .findByUsername(properties.getUsername())
            .orElseGet(
                () -> {
                  User created =
                      User.create(
                          properties.getUsername(),
                          properties.getEmail(),
                          passwordEncoder.encode(properties.getPassword()));
                  User saved = userRepository.save(created);
                  log.info("Seeded demo IAM User '{}'", properties.getUsername());
                  return saved;
                });
    user.assignRole(adminRole.getId());
    userRepository.save(user);
  }

  private static Role ensureAdminRole(RoleRepository roleRepository) {
    return roleRepository.findAll().stream()
        .filter(role -> IAM_ADMIN_ROLE.equals(role.getName()))
        .findFirst()
        .orElseGet(
            () -> {
              Role created =
                  roleRepository.save(
                      Role.create(IAM_ADMIN_ROLE, ImpersonationPolicy.allowAll()));
              log.info("Seeded IAM role '{}'", IAM_ADMIN_ROLE);
              return created;
            });
  }
}
