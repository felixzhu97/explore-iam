package com.iam.identity.domain.repository;

import com.iam.identity.domain.model.TotpCredential;
import java.util.Optional;

/** Persistence port for {@link TotpCredential}. */
public interface TotpCredentialRepository {

  /**
   * Saves the aggregate.
   *
   * @param credential credential to persist
   * @return saved instance
   */
  TotpCredential save(TotpCredential credential);

  /**
   * Finds by owning user id.
   *
   * @param userId user id
   * @return optional credential
   */
  Optional<TotpCredential> findByUserId(String userId);
}
