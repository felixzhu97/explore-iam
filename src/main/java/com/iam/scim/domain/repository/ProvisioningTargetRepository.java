package com.iam.scim.domain.repository;

import com.iam.scim.domain.model.ProvisioningTarget;
import java.util.List;
import java.util.Optional;

/** Persistence for Provisioning Targets. */
public interface ProvisioningTargetRepository {

  /**
   * Finds a target by unique name.
   *
   * @param name target name
   * @return matching target when present
   */
  Optional<ProvisioningTarget> findByName(String name);

  /**
   * Lists enabled targets.
   *
   * @return enabled targets
   */
  List<ProvisioningTarget> findEnabled();

  /**
   * Lists every target, enabled or not.
   *
   * @return all targets
   */
  List<ProvisioningTarget> findAll();

  /**
   * Persists a new or updated target.
   *
   * @param target aggregate to store
   * @return stored aggregate
   */
  ProvisioningTarget save(ProvisioningTarget target);
}
