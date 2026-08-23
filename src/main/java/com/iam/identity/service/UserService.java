package com.iam.identity.service;

import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** IAM user listing and lifecycle operations. */
@Service
@Transactional(readOnly = true)
public class UserService {

  private final IamUserRepository iamUserRepository;
  private final ManagementAuditRecorder managementAuditRecorder;

  /**
   * Creates the user service.
   *
   * @param iamUserRepository IAM user repository
   * @param managementAuditRecorder management audit recorder
   */
  public UserService(
      IamUserRepository iamUserRepository, ManagementAuditRecorder managementAuditRecorder) {
    this.iamUserRepository = iamUserRepository;
    this.managementAuditRecorder = managementAuditRecorder;
  }

  /**
   * Returns all IAM users.
   *
   * @return user list
   */
  public List<IamUser> findAll() {
    return iamUserRepository.findAll();
  }

  /**
   * Disables the user with the given id.
   *
   * @param userId internal user id
   * @return updated user
   */
  @Transactional
  public IamUser disable(String userId) {
    IamUser user =
        iamUserRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
    user.disable();
    IamUser saved = iamUserRepository.save(user);
    managementAuditRecorder.recordSuccess("identity:DisableUser", "User", userId);
    return saved;
  }
}
