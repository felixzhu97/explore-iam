package com.iam.identity.infrastructure.config;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Seeds the demo IAM user and exposes the shared password encoder. */
@Configuration
@EnableConfigurationProperties(DemoUserProperties.class)
public class IdentityBootstrapConfig {

  private static final Logger log = LoggerFactory.getLogger(IdentityBootstrapConfig.class);

  @Bean
  PasswordEncoder passwordEncoder() {
    return org.springframework.security.crypto.factory.PasswordEncoderFactories
        .createDelegatingPasswordEncoder();
  }

  @Bean
  ApplicationRunner seedDemoUser(
      IamUserRepository repository,
      PasswordEncoder passwordEncoder,
      DemoUserProperties properties) {
    return (ApplicationArguments args) -> {
      if (!properties.isEnabled()) {
        return;
      }
      if (repository.findByUsername(properties.getUsername()).isPresent()) {
        return;
      }
      IamUser user =
          IamUser.create(
              properties.getUsername(),
              properties.getEmail(),
              passwordEncoder.encode(properties.getPassword()));
      repository.save(user);
      log.info("Seeded demo IAM User '{}'", properties.getUsername());
    };
  }
}
