package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.repository.GroupRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaGroupRepository implements GroupRepository {

  private final SpringDataGroupRepository groupRepository;

  JpaGroupRepository(SpringDataGroupRepository groupRepository) {
    this.groupRepository = groupRepository;
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
  public List<Group> findAll() {
    return groupRepository.findAll();
  }

  @Override
  public void delete(Group group) {
    groupRepository.delete(group);
  }
}
