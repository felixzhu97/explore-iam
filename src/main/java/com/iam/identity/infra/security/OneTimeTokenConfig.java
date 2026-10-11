package com.iam.identity.infra.security;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.ott.JdbcOneTimeTokenService;
import org.springframework.security.authentication.ott.OneTimeTokenService;

/** JDBC-backed One-Time Token persistence (Spring Security production recommendation). */
@Configuration
class OneTimeTokenConfig {

  @Bean
  OneTimeTokenService oneTimeTokenService(DataSource dataSource) {
    JdbcOperations jdbc = new JdbcTemplate(dataSource);
    return new JdbcOneTimeTokenService(jdbc);
  }
}
