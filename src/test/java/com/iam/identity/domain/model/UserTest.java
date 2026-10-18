package com.iam.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iam.identity.domain.event.UserDeactivated;
import com.iam.identity.domain.event.UserDeleted;
import com.iam.identity.domain.event.UserProfileChanged;
import com.iam.identity.domain.event.UserProvisioned;
import com.iam.identity.domain.event.UserReactivated;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  @DisplayName("should assign role idempotently when assigning same role twice")
  void shouldAssignRoleIdempotentlyWhenAssigningSameRoleTwice() {
    User user = User.createUser("demo", "demo@example.com", "{noop}secret");

    user.assignRole("role-1");
    user.assignRole("role-1");

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
  @DisplayName("should provision passwordless user with external id and register user provisioned")
  void shouldProvisionPasswordlessUserWithExternalIdAndRegisterUserProvisioned() {
    User user =
        User.provisionUser("alice", "alice@example.com", " Alice ", new ExternalId("okta-1"));

    assertThat(user.isPasswordless()).isTrue();
    assertThat(user.getDisplayName()).isEqualTo("Alice");
    assertThat(user.getExternalId()).isEqualTo(new ExternalId("okta-1"));
    assertThat(user.domainEvents()).containsExactly(new UserProvisioned(user.getId()));
  }

  @Test
  @DisplayName("should register deactivated and reactivated only when state changes")
  void shouldRegisterDeactivatedAndReactivatedOnlyWhenStateChanges() {
    User user = User.createUser("demo", null, "{noop}secret");
    user.clearDomainEvents();

    user.enableUser();
    user.disableUser();
    user.disableUser();
    user.enableUser();

    assertThat(user.domainEvents())
        .containsExactly(new UserDeactivated(user.getId()), new UserReactivated(user.getId()));
  }

  @Test
  @DisplayName("should register profile changed when profile differs")
  void shouldRegisterProfileChangedWhenProfileDiffers() {
    User user = User.provisionUser("alice", "a@example.com", "Alice", null);
    user.clearDomainEvents();

    user.updateProfile("alice", "a@example.com", "Alice");
    user.updateProfile("alice2", "a@example.com", "Alice");

    assertThat(user.getUsername()).isEqualTo("alice2");
    assertThat(user.domainEvents()).containsExactly(new UserProfileChanged(user.getId()));
  }

  @Test
  @DisplayName("should clear external id when null is given")
  void shouldClearExternalIdWhenNullIsGiven() {
    User user = User.provisionUser("alice", null, null, new ExternalId("okta-1"));

    user.changeExternalId(null);

    assertThat(user.getExternalId()).isNull();
  }

  @Test
  @DisplayName("should register user deleted when remove user is called")
  void shouldRegisterUserDeletedWhenRemoveUserIsCalled() {
    User user = User.createUser("demo", null, "{noop}secret");
    user.clearDomainEvents();

    user.removeUser();

    assertThat(user.domainEvents()).containsExactly(new UserDeleted(user.getId()));
  }

  @Test
  @DisplayName("should reject display name longer than 256 characters")
  void shouldRejectDisplayNameLongerThan256Characters() {
    assertThatThrownBy(() -> User.provisionUser("alice", null, "x".repeat(257), null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
