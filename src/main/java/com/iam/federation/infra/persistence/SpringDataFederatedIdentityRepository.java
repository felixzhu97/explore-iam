package com.iam.federation.infra.persistence;

import com.iam.federation.domain.model.FederatedIdentity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataFederatedIdentityRepository
    extends JpaRepository<FederatedIdentity, String> {

  Optional<FederatedIdentity> findByProviderAndExternalSubject(
      String provider, String externalSubject);

  @Modifying
  @Query("delete from FederatedIdentity f where f.userId = :userId")
  void deleteByUserId(@Param("userId") String userId);
}
