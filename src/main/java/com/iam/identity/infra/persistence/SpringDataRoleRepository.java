package com.iam.identity.infra.persistence;

import com.iam.common.domain.vo.Arn;
import com.iam.identity.domain.model.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataRoleRepository extends JpaRepository<Role, String> {

  Optional<Role> findByName(String name);

  Optional<Role> findByArn(Arn arn);

  @Query(
      """
      select r from Role r
      join UserRoleAssignment ur on ur.id.roleId = r.id
      where ur.id.userId = :userId
      """)
  List<Role> findRolesByUserId(@Param("userId") String userId);
}
