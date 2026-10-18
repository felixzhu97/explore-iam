package com.iam.scim.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProvisioningTarget")
class ProvisioningTargetTest {

  @Test
  @DisplayName("should strip trailing slash from base url when created")
  void shouldStripTrailingSlashFromBaseUrlWhenCreated() {
    ProvisioningTarget target =
        ProvisioningTarget.createProvisioningTarget(
            "explore-ai", "https://ai.example/scim/v2/", "iam-scim-outbound", " ");

    assertThat(target.getBaseUrl()).isEqualTo("https://ai.example/scim/v2");
    assertThat(target.isEnabled()).isTrue();
    assertThat(target.requiresAssignment()).isFalse();
  }

  @Test
  @DisplayName("should reject base url when scheme is not http")
  void shouldRejectBaseUrlWhenSchemeIsNotHttp() {
    assertThatThrownBy(
            () ->
                ProvisioningTarget.createProvisioningTarget(
                    "explore-ai", "ftp://ai.example/scim/v2", "iam-scim-outbound", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("should reject plain http when host is not localhost")
  void shouldRejectPlainHttpWhenHostIsNotLocalhost() {
    assertThatThrownBy(
            () ->
                ProvisioningTarget.createProvisioningTarget(
                    "explore-ai", "http://ai.example/scim/v2", "iam-scim-outbound", null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(
            ProvisioningTarget.createProvisioningTarget(
                    "explore-ai", "http://localhost:8080/scim/v2", "iam-scim-outbound", null)
                .getBaseUrl())
        .isEqualTo("http://localhost:8080/scim/v2");
  }

  @Test
  @DisplayName("should skip push when version was already synced")
  void shouldSkipPushWhenVersionWasAlreadySynced() {
    ProvisioningTarget target = target(null);

    target.recordProvisionedAccount("user-1", "remote-1", 3);

    ProvisionedAccount account = target.findProvisionedAccount("user-1").orElseThrow();
    assertThat(account.getRemoteId()).isEqualTo("remote-1");
    assertThat(account.isSyncedAt(3)).isTrue();
    assertThat(account.isSyncedAt(4)).isFalse();
  }

  @Test
  @DisplayName("should update existing account when recorded again")
  void shouldUpdateExistingAccountWhenRecordedAgain() {
    ProvisioningTarget target = target(null);
    target.recordProvisionedAccount("user-1", "remote-1", 1);

    target.recordProvisionedAccount("user-1", "remote-2", 2);

    ProvisionedAccount account = target.findProvisionedAccount("user-1").orElseThrow();
    assertThat(account.getRemoteId()).isEqualTo("remote-2");
    assertThat(account.getLastSyncedVersion()).isEqualTo(2);
  }

  @Test
  @DisplayName("should require full push when account marked stale")
  void shouldRequireFullPushWhenAccountMarkedStale() {
    ProvisioningTarget target = target("group-1");
    target.recordProvisionedAccount("user-1", "remote-1", 5);

    target.markProvisionedAccountStale("user-1");

    assertThat(target.findProvisionedAccount("user-1").orElseThrow().isSyncedAt(0)).isFalse();
  }

  @Test
  @DisplayName("should forget account when deleted on target")
  void shouldForgetAccountWhenDeletedOnTarget() {
    ProvisioningTarget target = target(null);
    target.recordProvisionedAccount("user-1", "remote-1", 1);

    target.forgetProvisionedAccount("user-1");

    assertThat(target.findProvisionedAccount("user-1")).isEmpty();
  }

  @Test
  @DisplayName("should match only its assignment group")
  void shouldMatchOnlyItsAssignmentGroup() {
    ProvisioningTarget target = target("group-1");

    assertThat(target.requiresAssignment()).isTrue();
    assertThat(target.isAssignmentGroup("group-1")).isTrue();
    assertThat(target.isAssignmentGroup("group-2")).isFalse();
  }

  @Test
  @DisplayName("should keep version when enabling an enabled target")
  void shouldKeepStateWhenEnablingAnEnabledTarget() {
    ProvisioningTarget target = target(null);

    target.enableTarget();
    target.disableTarget();
    target.disableTarget();

    assertThat(target.isEnabled()).isFalse();
  }

  private static ProvisioningTarget target(String assignmentGroupId) {
    return ProvisioningTarget.createProvisioningTarget(
        "explore-ai", "https://ai.example/scim/v2", "iam-scim-outbound", assignmentGroupId);
  }
}
