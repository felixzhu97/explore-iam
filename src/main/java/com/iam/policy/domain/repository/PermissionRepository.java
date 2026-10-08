package com.iam.policy.domain.repository;

import com.iam.policy.domain.model.Permission;
import java.util.List;
import java.util.Optional;

/** Persistence for Permission catalog entries. */
public interface PermissionRepository {

  /**
   * Saves a permission.
   *
   * @param permission aggregate
   * @return saved aggregate
   */
  Permission save(Permission permission);

  /**
   * Finds by business code / oauth scope.
   *
   * @param code catalog code
   * @return optional aggregate
   */
  Optional<Permission> findByCode(String code);

  /**
   * Lists all catalog entries.
   *
   * @return all permissions
   */
  List<Permission> findAll();

  /**
   * Returns whether a code already exists.
   *
   * @param code catalog code
   * @return true when present
   */
  boolean existsByCode(String code);
}
