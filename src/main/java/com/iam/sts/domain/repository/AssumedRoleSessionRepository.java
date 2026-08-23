package com.iam.sts.domain.repository;

import com.iam.sts.domain.model.AssumedRoleSession;
import java.util.Optional;

/** Persistence port for assumed-role sessions. */
public interface AssumedRoleSessionRepository {

  /**
   * Persists an assumed-role session.
   *
   * @param session session to store
   * @return stored session
   */
  AssumedRoleSession save(AssumedRoleSession session);

  /**
   * Finds a session by id.
   *
   * @param id session id
   * @return matching session when present
   */
  Optional<AssumedRoleSession> findById(String id);
}
