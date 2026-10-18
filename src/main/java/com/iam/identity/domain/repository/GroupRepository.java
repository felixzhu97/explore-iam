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
   * Lists all groups.
   *
   * @return all groups
   */
  List<Group> findAll();

  /**
   * Deletes a group aggregate.
   *
   * @param group aggregate to remove
   */
  void delete(Group group);

  /**
   * Finds a group by External ID.
   *
   * @param externalId identifier from the provisioning client
   * @return matching group when present
   */
  Optional<Group> findByExternalId(String externalId);

  /**
   * Lists groups that contain the user.
   *
   * @param userId IAM user id
   * @return groups with the user as a member
   */
  List<Group> findByMemberUserId(String userId);
}
