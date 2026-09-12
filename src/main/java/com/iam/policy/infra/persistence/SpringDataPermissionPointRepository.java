package com.iam.policy.infra.persistence;

import com.iam.policy.domain.model.PermissionPoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPermissionPointRepository extends JpaRepository<PermissionPoint, String> {

  Optional<PermissionPoint> findByCode(String code);

  boolean existsByCode(String code);
}
