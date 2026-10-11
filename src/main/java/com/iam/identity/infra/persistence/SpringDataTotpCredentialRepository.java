package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.TotpCredential;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataTotpCredentialRepository extends JpaRepository<TotpCredential, String> {

  Optional<TotpCredential> findByUserId(String userId);
}
