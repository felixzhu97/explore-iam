package com.iam.policy.infra.config;

import com.iam.policy.domain.service.PolicyEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers domain services as Spring beans. */
@Configuration
public class PolicyConfig {

  @Bean
  PolicyEngine policyEngine() {
    return new PolicyEngine();
  }
}
