package com.iam.identity.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.iam.identity.domain.model.ExternalId;
import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.GroupRepository;
import com.iam.identity.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("Identity repositories")
class IdentityRepositoryTest {

  @Autowired private UserRepository userRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("should find user by external id when saved")
  void shouldFindUserByExternalIdWhenSaved() {
    String externalId = "okta-" + UUID.randomUUID();
    User user =
        userRepository.save(
            User.provisionUser(unique("alice"), null, "Alice", new ExternalId(externalId)));
    entityManager.flush();
    entityManager.clear();

    assertThat(userRepository.findByExternalId(externalId))
        .get()
        .extracting(User::getId)
        .isEqualTo(user.getId());
  }

  @Test
  @DisplayName("should read null external id back as null")
  void shouldReadNullExternalIdBackAsNull() {
    User user = userRepository.save(User.createUser(unique("bob"), null, "{noop}secret"));
    entityManager.flush();
    entityManager.clear();

    assertThat(userRepository.findById(user.getId()).orElseThrow().getExternalId()).isNull();
  }

  @Test
  @DisplayName("should find groups by member")
  void shouldFindGroupsByMember() {
    User user = userRepository.save(User.createUser(unique("carol"), null, "{noop}secret"));
    Group group = Group.createGroup(unique("team"));
    group.addMember(user.getId());
    groupRepository.save(group);
    entityManager.flush();
    entityManager.clear();

    assertThat(groupRepository.findByMemberUserId(user.getId()))
        .extracting(Group::getId)
        .containsExactly(group.getId());
  }

  private static String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }
}
