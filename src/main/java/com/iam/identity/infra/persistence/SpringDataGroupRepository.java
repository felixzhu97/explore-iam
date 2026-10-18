package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataGroupRepository extends JpaRepository<Group, String> {

  Optional<Group> findByName(String name);

  Optional<Group> findByExternalIdValue(String externalId);

  @Query("select distinct g from Group g join g.members m where m.id.userId = :userId")
  List<Group> findByMemberUserId(@Param("userId") String userId);
}
