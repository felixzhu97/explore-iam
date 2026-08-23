package com.iam.identity.service;

import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates a new IAM group. */
@Service
public class CreateGroupService {

  private final GroupRepository groupRepository;
  private final ManagementAuditRecorder managementAuditRecorder;

  /**
   * Creates the use case.
   *
   * @param groupRepository group repository
   * @param managementAuditRecorder management audit recorder
   */
  public CreateGroupService(
      GroupRepository groupRepository, ManagementAuditRecorder managementAuditRecorder) {
    this.groupRepository = groupRepository;
    this.managementAuditRecorder = managementAuditRecorder;
  }

  /**
   * Creates a group with the given name.
   *
   * @param name unique group name
   * @return persisted group
   */
  @Transactional
  public Group execute(String name) {
    Group group = groupRepository.save(Group.create(name));
    managementAuditRecorder.recordSuccess("identity:CreateGroup", "Group", group.getId());
    return group;
  }
}
