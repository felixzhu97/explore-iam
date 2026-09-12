package com.iam.identity.service;

import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** IAM user listing and lifecycle operations. */
@Service
@Transactional(readOnly = true)
public class UserService {

  private final IamUserRepository iamUserRepository;
  private final ManagementAuditRecorder managementAuditRecorder;
  private final PasswordEncoder passwordEncoder;

  /**
   * Creates the user service.
   *
   * @param iamUserRepository IAM user repository
   * @param managementAuditRecorder management audit recorder
   * @param passwordEncoder password encoder
   */
  public UserService(
      IamUserRepository iamUserRepository,
      ManagementAuditRecorder managementAuditRecorder,
      PasswordEncoder passwordEncoder) {
    this.iamUserRepository = iamUserRepository;
    this.managementAuditRecorder = managementAuditRecorder;
    this.passwordEncoder = passwordEncoder;
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
   * Returns one user by id.
   *
   * @param userId internal user id
   * @return user
   */
  public IamUser get(String userId) {
    return iamUserRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
  }

  /**
   * Creates an enabled IAM user.
   *
   * @param username unique username
   * @param email email
   * @param password plaintext password
   * @return created user
   */
  @Transactional
  public IamUser create(String username, String email, String password) {
    if (iamUserRepository.findByUsername(username).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "username exists");
    }
    IamUser user =
        iamUserRepository.save(
            IamUser.create(username, email, passwordEncoder.encode(password)));
    managementAuditRecorder.recordSuccess("identity:CreateUser", "User", user.getId());
    return user;
  }

  /**
   * Disables the user with the given id.
   *
   * @param userId internal user id
   * @return updated user
   */
  @Transactional
  public IamUser disable(String userId) {
    IamUser user = get(userId);
    user.disable();
    IamUser saved = iamUserRepository.save(user);
    managementAuditRecorder.recordSuccess("identity:DisableUser", "User", userId);
    return saved;
  }

  /**
   * Enables the user with the given id.
   *
   * @param userId internal user id
   * @return updated user
   */
  @Transactional
  public IamUser enable(String userId) {
    IamUser user = get(userId);
    user.enable();
    IamUser saved = iamUserRepository.save(user);
    managementAuditRecorder.recordSuccess("identity:EnableUser", "User", userId);
    return saved;
  }

  /**
   * Resets the user password.
   *
   * @param userId internal user id
   * @param password plaintext password
   * @return updated user
   */
  @Transactional
  public IamUser resetPassword(String userId, String password) {
    IamUser user = get(userId);
    user.resetPassword(passwordEncoder.encode(password));
    IamUser saved = iamUserRepository.save(user);
    managementAuditRecorder.recordSuccess("identity:ResetPassword", "User", userId);
    return saved;
  }
}
