package com.iam.scim.domain.service;

import com.iam.identity.domain.model.User;
import com.iam.scim.domain.model.ProvisioningTarget;
import java.util.Optional;

/** Outbound calls to a Provisioning Target's SCIM {@code /Users} endpoint. */
public interface ScimTargetGateway {

  /**
   * Finds the target-assigned id of the account whose {@code externalId} is the IAM user id.
   *
   * @param target Provisioning Target
   * @param iamUserId IAM user id
   * @return remote id when the account exists
   */
  Optional<String> findRemoteId(ProvisioningTarget target, String iamUserId);

  /**
   * Creates the account, or replaces it when the target reports it already exists.
   *
   * @param target Provisioning Target
   * @param user IAM user
   * @return remote id assigned by the target
   */
  String createAccount(ProvisioningTarget target, User user);

  /**
   * Replaces the account with the user's current state.
   *
   * @param target Provisioning Target
   * @param remoteId target-assigned id
   * @param user IAM user
   * @return false when the account no longer exists
   */
  boolean replaceAccount(ProvisioningTarget target, String remoteId, User user);

  /**
   * Sets only the account's {@code active} attribute.
   *
   * @param target Provisioning Target
   * @param remoteId target-assigned id
   * @param active desired state
   * @return false when the account no longer exists
   */
  boolean setAccountActive(ProvisioningTarget target, String remoteId, boolean active);

  /**
   * Deletes the account; a missing account counts as deleted.
   *
   * @param target Provisioning Target
   * @param remoteId target-assigned id
   */
  void deleteAccount(ProvisioningTarget target, String remoteId);
}
