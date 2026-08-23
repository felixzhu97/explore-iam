package com.iam.identity.domain.repository;

import com.iam.identity.domain.model.Role;
import java.util.List;
import java.util.Optional;

/** Persistence port for IAM roles. */
public interface RoleRepository {

  /**
   * Finds a role by unique name.
   *
   * @param name role name
   * @return matching role when present
   */
  Optional<Role> findByName(String name);

  /**
   * Finds a role by ARN value.
   *
   * @param arn role ARN
   * @return matching role when present
   */
  Optional<Role> findByArn(String arn);

  /**
   * Finds a role by internal identifier.
   *
   * @param id internal id
   * @return matching role when present
   */
  Optional<Role> findById(String id);

  /**
   * Persists a new or updated role.
   *
   * @param role aggregate to store
   * @return stored aggregate
   */
  Role save(Role role);

  /**
   * Assigns a role to a user.
   *
   * @param userId user id
   * @param roleId role id
   */
  void assignToUser(String userId, String roleId);

  /**
   * Lists roles assigned to a user.
   *
   * @param userId user id
   * @return assigned roles
   */
  List<Role> findByUserId(String userId);

  /**
   * Lists all roles.
   *
   * @return all roles
   */
  List<Role> findAll();
}
