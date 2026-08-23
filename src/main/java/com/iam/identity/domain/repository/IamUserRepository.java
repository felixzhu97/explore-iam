package com.iam.identity.domain.repository;

import com.iam.identity.domain.model.IamUser;
import java.util.List;
import java.util.Optional;

/** Persistence port for IAM users. */
public interface IamUserRepository {

  /**
   * Finds a user by unique username.
   *
   * @param username login name
   * @return matching user when present
   */
  Optional<IamUser> findByUsername(String username);

  /**
   * Finds a user by internal identifier.
   *
   * @param id internal id
   * @return matching user when present
   */
  Optional<IamUser> findById(String id);

  /**
   * Persists a new or updated IAM user.
   *
   * @param user aggregate to store
   * @return stored aggregate
   */
  IamUser save(IamUser user);

  /**
   * Lists all IAM users.
   *
   * @return all users
   */
  List<IamUser> findAll();
}
