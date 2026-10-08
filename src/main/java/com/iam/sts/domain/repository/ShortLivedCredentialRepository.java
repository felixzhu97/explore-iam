package com.iam.sts.domain.repository;

import com.iam.sts.domain.model.ShortLivedCredential;
import java.util.Optional;

/** Persistence port for assumed-role sessions. */
public interface ShortLivedCredentialRepository {

  /**
   * Persists an assumed-role session.
   *
   * @param session session to store
   * @return stored session
   */
  ShortLivedCredential save(ShortLivedCredential session);

  /**
   * Finds a session by id.
   *
   * @param id session id
   * @return matching session when present
   */
  Optional<ShortLivedCredential> findById(String id);
}
