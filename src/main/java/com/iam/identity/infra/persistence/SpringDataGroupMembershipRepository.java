package com.iam.identity.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataGroupMembershipRepository
    extends JpaRepository<GroupMembershipEntity, GroupMembershipEntity.Pk> {

  boolean existsByGroupIdAndUserId(String groupId, String userId);
}
