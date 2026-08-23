package com.iam.common.infra.persistence;

import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers IAM-specific Hibernate physical naming for legacy {@code iam_*} tables. */
@Configuration
public class IamJpaConfig {

  @Bean
  HibernatePropertiesCustomizer iamPhysicalNamingStrategyCustomizer() {
    return hibernateProperties ->
        hibernateProperties.put(
            "hibernate.physical_naming_strategy", IamPhysicalNamingStrategy.class.getName());
  }
}
