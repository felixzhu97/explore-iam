package com.iam.identity.domain.repository;

import com.iam.identity.domain.model.Group;
import java.util.List;
import java.util.Optional;

/** Persistence port for IAM groups. */
public interface GroupRepository {

  /**
   * Finds a group by unique name.
   *
   * @param name group name
   * @return matching group when present
   */
  Optional<Group> findByName(String name);

  /**
   * Finds a group by internal identifier.
   *
   * @param id internal id
   * @return matching group when present
   */
  Optional<Group> findById(String id);

  /**
   * Persists a new or updated group.
   *
   * @param group aggregate to store
   * @return stored aggregate
   */
  Group save(Group group);

  /**
   * Adds a user to a group membership.
   *
   * @param groupId group id
   * @param userId user id
   */
  void addMember(String groupId, String userId);

  /**
   * Lists all groups.
   *
   * @return all groups
   */
  List<Group> findAll();
}
