package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.Group;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGroupRepository extends JpaRepository<Group, String> {

  Optional<Group> findByName(String name);
}
