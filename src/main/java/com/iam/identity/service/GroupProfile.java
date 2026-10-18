package com.iam.identity.service;

import com.iam.identity.domain.model.ExternalId;
import java.util.Set;

/**
 * Desired state of a provisioned group.
 *
 * @param name unique group name
 * @param externalId identifier from the provisioning client, may be {@code null}
 * @param memberUserIds IAM user ids that belong to the group
 */
public record GroupProfile(String name, ExternalId externalId, Set<String> memberUserIds) {

  /** Copies the member id set. */
  public GroupProfile {
    memberUserIds = Set.copyOf(memberUserIds);
  }
}
