package com.iam.identity.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataIamUserRepository extends JpaRepository<IamUserEntity, String> {

    Optional<IamUserEntity> findByUsername(String username);
}
