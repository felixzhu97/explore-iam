package com.iam.identity.service;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Disables an IAM user so form login is rejected. */
@Service
public class DisableUserService {

  private final IamUserRepository iamUserRepository;

  /**
   * Creates the use case.
   *
   * @param iamUserRepository IAM user repository
   */
  public DisableUserService(IamUserRepository iamUserRepository) {
    this.iamUserRepository = iamUserRepository;
  }

  /**
   * Disables the user with the given id.
   *
   * @param userId internal user id
   * @return updated user
   */
  @Transactional
  public IamUser execute(String userId) {
    IamUser user =
        iamUserRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
    user.disable();
    return iamUserRepository.save(user);
  }
}
