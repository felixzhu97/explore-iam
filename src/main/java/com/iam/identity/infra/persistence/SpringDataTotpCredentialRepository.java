package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.TotpCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataTotpCredentialRepository extends JpaRepository<TotpCredential, String> {

  Optional<TotpCredential> findByUserId(String userId);

  @Modifying
  @Query("delete from TotpCredential t where t.userId = :userId")
  void deleteByUserId(@Param("userId") String userId);
}
