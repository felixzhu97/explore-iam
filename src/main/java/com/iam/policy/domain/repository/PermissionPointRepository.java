package com.iam.policy.domain.repository;

import com.iam.policy.domain.model.PermissionPoint;
import java.util.List;
import java.util.Optional;

/** Persistence for Permission Point catalog entries. */
public interface PermissionPointRepository {

  /**
   * Saves a permission point.
   *
   * @param permissionPoint aggregate
   * @return saved aggregate
   */
  PermissionPoint save(PermissionPoint permissionPoint);

  /**
   * Finds by business code / oauth scope.
   *
   * @param code catalog code
   * @return optional aggregate
   */
  Optional<PermissionPoint> findByCode(String code);

  /**
   * Lists all catalog entries.
   *
   * @return all permission points
   */
  List<PermissionPoint> findAll();

  /**
   * Returns whether a code already exists.
   *
   * @param code catalog code
   * @return true when present
   */
  boolean existsByCode(String code);
}
