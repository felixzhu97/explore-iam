package com.iam.audit.infra.persistence;

import com.iam.audit.domain.model.AdminActivity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAdminActivityRepository extends JpaRepository<AdminActivity, String> {

  List<AdminActivity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
