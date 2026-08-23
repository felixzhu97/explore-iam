package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.repository.GroupRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaGroupRepository implements GroupRepository {

  private final SpringDataGroupRepository groupRepository;
  private final SpringDataGroupMembershipRepository membershipRepository;

  JpaGroupRepository(
      SpringDataGroupRepository groupRepository,
      SpringDataGroupMembershipRepository membershipRepository) {
    this.groupRepository = groupRepository;
    this.membershipRepository = membershipRepository;
  }

  @Override
  public Optional<Group> findByName(String name) {
    return groupRepository.findByName(name);
  }

  @Override
  public Optional<Group> findById(String id) {
    return groupRepository.findById(id);
  }

  @Override
  public Group save(Group group) {
    return groupRepository.save(group);
  }

  @Override
  public void addMember(String groupId, String userId) {
    if (!membershipRepository.existsByGroupIdAndUserId(groupId, userId)) {
      membershipRepository.save(new GroupMembershipEntity(groupId, userId));
    }
  }

  @Override
  public List<Group> findAll() {
    return groupRepository.findAll();
  }
}
