package com.iam.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IamUserTest {

  @Test
  @DisplayName("should assign role idempotently when assigning same role twice")
  void shouldAssignRoleIdempotentlyWhenAssigningSameRoleTwice() {
    IamUser user = IamUser.create("demo", "demo@example.com", "{noop}secret");

    user.assignRole("role-1");
    user.assignRole("role-1");

    assertThat(user.assignedRoleIds()).containsExactly("role-1");
  }

  @Test
  @DisplayName("should derive federated username from provider and subject")
  void shouldDeriveFederatedUsernameFromProviderAndSubject() {
    IamUser user = IamUser.createForFederatedLogin("google", "sub-123", "user@example.com");

    assertThat(user.getUsername()).isEqualTo("google:sub-123");
    assertThat(user.getEmail()).isEqualTo("user@example.com");
  }
}
