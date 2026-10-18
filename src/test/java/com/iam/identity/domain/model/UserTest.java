package com.iam.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  @DisplayName("should assign role idempotently when assigning same role twice")
  void shouldAssignRoleIdempotentlyWhenAssigningSameRoleTwice() {
    User user = User.createUser("demo", "demo@example.com", "{noop}secret");

    assertThat(user.assignRole("role-1")).isTrue();
    assertThat(user.assignRole("role-1")).isFalse();

    assertThat(user.assignedRoleIds()).containsExactly("role-1");
  }

  @Test
  @DisplayName("should derive federated username from provider and subject")
  void shouldDeriveFederatedUsernameFromProviderAndSubject() {
    User user = User.createFederatedUser("google", "sub-123", "user@example.com");

    assertThat(user.getUsername()).isEqualTo("google:sub-123");
    assertThat(user.getEmail()).isEqualTo("user@example.com");
  }

  @Test
  @DisplayName("should provision passwordless user with external id")
  void shouldProvisionPasswordlessUserWithExternalId() {
    User user =
        User.provisionUser("alice", "alice@example.com", " Alice ", new ExternalId("okta-1"));

    assertThat(user.isPasswordless()).isTrue();
    assertThat(user.getDisplayName()).isEqualTo("Alice");
    assertThat(user.getExternalId()).isEqualTo(new ExternalId("okta-1"));
  }

  @Test
  @DisplayName("should report deactivation and reactivation only when state changes")
  void shouldReportDeactivationAndReactivationOnlyWhenStateChanges() {
    User user = User.createUser("demo", null, "{noop}secret");

    assertThat(user.enableUser()).isFalse();
    assertThat(user.disableUser()).isTrue();
    assertThat(user.disableUser()).isFalse();
    assertThat(user.enableUser()).isTrue();
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  @DisplayName("should report profile change only when profile differs")
  void shouldReportProfileChangeOnlyWhenProfileDiffers() {
    User user = User.provisionUser("alice", "a@example.com", "Alice", null);

    assertThat(user.updateProfile("alice", "a@example.com", "Alice")).isFalse();
    assertThat(user.updateProfile("alice2", "a@example.com", "Alice")).isTrue();

    assertThat(user.getUsername()).isEqualTo("alice2");
  }

  @Test
  @DisplayName("should clear external id when null is given")
  void shouldClearExternalIdWhenNullIsGiven() {
    User user = User.provisionUser("alice", null, null, new ExternalId("okta-1"));

    assertThat(user.changeExternalId(null)).isTrue();

    assertThat(user.getExternalId()).isNull();
    assertThat(user.changeExternalId(null)).isFalse();
  }

  @Test
  @DisplayName("should report unassign only when role was assigned")
  void shouldReportUnassignOnlyWhenRoleWasAssigned() {
    User user = User.createUser("demo", null, "{noop}secret");
    user.assignRole("role-1");

    assertThat(user.unassignRole("role-1")).isTrue();
    assertThat(user.unassignRole("role-1")).isFalse();
  }

  @Test
  @DisplayName("should reject display name longer than 256 characters")
  void shouldRejectDisplayNameLongerThan256Characters() {
    assertThatThrownBy(() -> User.provisionUser("alice", null, "x".repeat(257), null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
