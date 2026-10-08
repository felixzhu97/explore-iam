package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserRepository extends JpaRepository<User, String> {

  Optional<User> findByUsername(String username);
}
