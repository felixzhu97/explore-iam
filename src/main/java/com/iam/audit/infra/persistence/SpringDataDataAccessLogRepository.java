package com.iam.audit.infra.persistence;

import com.iam.audit.domain.model.DataAccessLog;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataDataAccessLogRepository
    extends JpaRepository<DataAccessLog, String> {

  List<DataAccessLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
