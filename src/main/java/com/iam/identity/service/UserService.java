package com.iam.identity.service;

import com.iam.audit.service.AdminActivityRecorder;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.UserRepository;
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

  private final UserRepository userRepository;
  private final AdminActivityRecorder adminActivityRecorder;
  private final PasswordEncoder passwordEncoder;

  /**
   * Creates the user service.
   *
   * @param userRepository IAM user repository
   * @param adminActivityRecorder management audit recorder
   * @param passwordEncoder password encoder
   */
  public UserService(
      UserRepository userRepository,
      AdminActivityRecorder adminActivityRecorder,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.adminActivityRecorder = adminActivityRecorder;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Returns all IAM users.
   *
   * @return user list
   */
  public List<User> findAll() {
    return userRepository.findAll();
  }

  /**
   * Returns one user by id.
   *
   * @param userId internal user id
   * @return user
   */
  public User get(String userId) {
    return userRepository
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
  public User create(String username, String email, String password) {
    if (userRepository.findByUsername(username).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "username exists");
    }
    User user =
        userRepository.save(
            User.create(username, email, passwordEncoder.encode(password)));
    adminActivityRecorder.recordSuccess("identity:CreateUser", "User", user.getId());
    return user;
  }

  /**
   * Disables the user with the given id.
   *
   * @param userId internal user id
   * @return updated user
   */
  @Transactional
  public User disable(String userId) {
    User user = get(userId);
    user.disable();
    User saved = userRepository.save(user);
    adminActivityRecorder.recordSuccess("identity:DisableUser", "User", userId);
    return saved;
  }

  /**
   * Enables the user with the given id.
   *
   * @param userId internal user id
   * @return updated user
   */
  @Transactional
  public User enable(String userId) {
    User user = get(userId);
    user.enable();
    User saved = userRepository.save(user);
    adminActivityRecorder.recordSuccess("identity:EnableUser", "User", userId);
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
  public User resetPassword(String userId, String password) {
    User user = get(userId);
    user.resetPassword(passwordEncoder.encode(password));
    User saved = userRepository.save(user);
    adminActivityRecorder.recordSuccess("identity:ResetPassword", "User", userId);
    return saved;
  }
}
