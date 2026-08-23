package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.IamUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataIamUserRepository extends JpaRepository<IamUser, String> {

  Optional<IamUser> findByUsername(String username);
}
