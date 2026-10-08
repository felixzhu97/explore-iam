package com.iam.sts.infra.persistence;

import com.iam.sts.domain.model.ShortLivedCredential;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataShortLivedCredentialRepository
    extends JpaRepository<ShortLivedCredential, String> {}
