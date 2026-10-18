package com.iam.identity.service;

import com.iam.identity.domain.event.UserDeactivatedEvent;
import com.iam.identity.domain.event.UserPasswordResetEvent;
import com.iam.identity.domain.event.UserProvisionedEvent;
import com.iam.identity.domain.event.UserReactivatedEvent;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.UserRepository;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
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
  private final PasswordEncoder passwordEncoder;
  private final UserProvisioningService userProvisioningService;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Creates the user service.
   *
   * @param userRepository IAM user repository
   * @param passwordEncoder password encoder
   * @param userProvisioningService user lifecycle shared with SCIM
   * @param eventPublisher domain event publisher
   */
  public UserService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      UserProvisioningService userProvisioningService,
      ApplicationEventPublisher eventPublisher) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userProvisioningService = userProvisioningService;
    this.eventPublisher = eventPublisher;
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
  public User createUser(String username, String email, String password) {
    if (userRepository.findByUsername(username).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "username exists");
    }
    User user =
        userRepository.save(
            User.createUser(username, email, passwordEncoder.encode(password)));
    eventPublisher.publishEvent(new UserProvisionedEvent(user.getId()));
    return user;
  }

  /**
   * Disables the user with the given id.
   *
   * @param userId internal user id
   * @param actorUserId IAM user id of the signed-in admin
   * @return updated user
   */
  @Transactional
  public User disable(String userId, String actorUserId) {
    requireNotSelf(userId, actorUserId);
    User user = get(userId);
    boolean deactivated = user.disableUser();
    User saved = userRepository.save(user);
    if (deactivated) {
      eventPublisher.publishEvent(new UserDeactivatedEvent(userId));
    }
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
    boolean reactivated = user.enableUser();
    User saved = userRepository.save(user);
    if (reactivated) {
      eventPublisher.publishEvent(new UserReactivatedEvent(userId));
    }
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
    eventPublisher.publishEvent(new UserPasswordResetEvent(userId));
    return saved;
  }

  /**
   * Deletes the user with memberships and credentials.
   *
   * @param userId internal user id
   * @param actorUserId IAM user id of the signed-in admin
   */
  @Transactional
  public void delete(String userId, String actorUserId) {
    requireNotSelf(userId, actorUserId);
    userProvisioningService.deleteUser(userId, null);
  }

  private static void requireNotSelf(String userId, String actorUserId) {
    if (actorUserId == null) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "unknown actor");
    }
    if (userId.equals(actorUserId)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot change own account");
    }
  }
}
