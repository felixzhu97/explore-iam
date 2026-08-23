package com.iam.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  @DisplayName("should use allow-all trust policy when json is blank")
  void shouldUseAllowAllTrustPolicyWhenJsonIsBlank() {
    Role role = Role.create("IAM_ADMIN", "  ");

    assertThat(role.getTrustPolicy().json()).contains("sts:AssumeRole");
  }
}
