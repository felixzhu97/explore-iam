package com.iam.audit.infra.persistence;

import com.iam.audit.domain.model.ManagementEvent;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataManagementEventRepository extends JpaRepository<ManagementEvent, String> {

  List<ManagementEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
