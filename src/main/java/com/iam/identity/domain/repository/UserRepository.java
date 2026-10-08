package com.iam.identity.domain.repository;

import com.iam.identity.domain.model.User;
import java.util.List;
import java.util.Optional;

/** Persistence port for IAM users. */
public interface UserRepository {

  /**
   * Finds a user by unique username.
   *
   * @param username login name
   * @return matching user when present
   */
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by internal identifier.
   *
   * @param id internal id
   * @return matching user when present
   */
  Optional<User> findById(String id);

  /**
   * Persists a new or updated IAM user.
   *
   * @param user aggregate to store
   * @return stored aggregate
   */
  User save(User user);

  /**
   * Lists all IAM users.
   *
   * @return all users
   */
  List<User> findAll();
}
