package com.iam.identity.service;

import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates a new IAM group. */
@Service
public class CreateGroupService {

  private final GroupRepository groupRepository;

  /**
   * Creates the use case.
   *
   * @param groupRepository group repository
   */
  public CreateGroupService(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  /**
   * Creates a group with the given name.
   *
   * @param name unique group name
   * @return persisted group
   */
  @Transactional
  public Group execute(String name) {
    return groupRepository.save(Group.create(name));
  }
}
