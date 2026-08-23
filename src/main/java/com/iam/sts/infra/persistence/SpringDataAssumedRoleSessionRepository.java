package com.iam.sts.infra.persistence;

import com.iam.sts.domain.model.AssumedRoleSession;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAssumedRoleSessionRepository
    extends JpaRepository<AssumedRoleSession, String> {}
