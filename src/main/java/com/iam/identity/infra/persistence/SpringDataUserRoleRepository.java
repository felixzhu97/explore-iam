package com.iam.identity.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleEntity.Pk> {}
