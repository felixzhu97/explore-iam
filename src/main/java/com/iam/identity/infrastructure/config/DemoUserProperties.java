package com.iam.identity.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Local demo user credentials used to bootstrap development identity. */
@ConfigurationProperties(prefix = "app.demo-user")
public class DemoUserProperties {

  private boolean enabled = true;
  private String username = "demo";
  private String password = "demo-password";
  private String email = "demo@explore-iam.local";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
