package com.iam.identity.service;

import com.iam.identity.domain.event.RoleCreatedEvent;
import com.iam.identity.domain.event.UserRoleAssignedEvent;
import com.iam.identity.domain.event.UserRoleUnassignedEvent;
import com.iam.identity.domain.model.Role;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.RoleRepository;
import com.iam.identity.domain.repository.UserRepository;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** IAM role listing and assignment operations. */
@Service
@Transactional(readOnly = true)
public class RoleService {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Creates the role service.
   *
   * @param roleRepository role repository
   * @param userRepository IAM user repository
   * @param eventPublisher domain event publisher
   */
  public RoleService(
      RoleRepository roleRepository,
      UserRepository userRepository,
      ApplicationEventPublisher eventPublisher) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Returns all IAM roles.
   *
   * @return role list
   */
  public List<Role> findAll() {
    return roleRepository.findAll();
  }

  /**
   * Creates a role with the given name and trust policy JSON.
   *
   * @param name role name
   * @param trustPolicyJson optional trust policy JSON
   * @return persisted role
   */
  @Transactional
  public Role createRole(String name, String trustPolicyJson) {
    Role role = roleRepository.save(Role.createRole(name, trustPolicyJson));
    eventPublisher.publishEvent(new RoleCreatedEvent(role.getId()));
    return role;
  }

  /**
   * Assigns a role to a user.
   *
   * @param userId user id
   * @param roleId role id
   */
  @Transactional
  public void assignToUser(String userId, String roleId) {
    roleRepository
        .findById(roleId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "role not found"));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    if (user.assignRole(roleId)) {
      userRepository.save(user);
      eventPublisher.publishEvent(new UserRoleAssignedEvent(userId, roleId));
    }
  }

  /**
   * Removes a role assignment from a user.
   *
   * @param userId user id
   * @param roleId role id
   */
  @Transactional
  public void unassignFromUser(String userId, String roleId) {
    roleRepository
        .findById(roleId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "role not found"));
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    if (user.unassignRole(roleId)) {
      userRepository.save(user);
      eventPublisher.publishEvent(new UserRoleUnassignedEvent(userId, roleId));
    }
  }
}
