package com.iam.identity.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Local demo user credentials used to bootstrap development identity. */
@ConfigurationProperties(prefix = "app.demo-user")
@Getter
@Setter
public class DemoUserProperties {

  private boolean enabled = true;
  private String username = "demo";
  private String password = "demo-password";
  private String email = "demo@explore-iam.local";
}
