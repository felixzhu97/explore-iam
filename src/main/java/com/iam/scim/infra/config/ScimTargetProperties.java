package com.iam.scim.infra.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Provisioning Targets IAM pushes users to, and resubmission settings. */
@ConfigurationProperties(prefix = "app.scim")
@Getter
@Setter
public class ScimTargetProperties {

  private List<Target> targets = new ArrayList<>();
  private Duration resubmitOlderThan = Duration.ofMinutes(5);

  public void setTargets(List<Target> targets) {
    this.targets = targets == null ? new ArrayList<>() : targets;
  }

  /** One downstream SCIM Service Provider. */
  @Getter
  @Setter
  public static class Target {
    private String name;
    private String baseUrl;
    private String registrationId;
    private String assignmentGroup;
    private boolean enabled = true;
  }
}
