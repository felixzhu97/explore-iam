package com.iam.identity.service;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lists all IAM users. */
@Service
public class ListUsersService {

  private final IamUserRepository iamUserRepository;

  /**
   * Creates the use case.
   *
   * @param iamUserRepository IAM user repository
   */
  public ListUsersService(IamUserRepository iamUserRepository) {
    this.iamUserRepository = iamUserRepository;
  }

  /**
   * Returns all IAM users.
   *
   * @return user list
   */
  @Transactional(readOnly = true)
  public List<IamUser> execute() {
    return iamUserRepository.findAll();
  }
}
