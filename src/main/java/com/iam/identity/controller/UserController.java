package com.iam.identity.controller;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.service.UserService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** IAM user management API. */
@RestController
@RequestMapping("/api/identity/users")
public class UserController {

  private final UserService userService;

  /**
   * Creates the user API controller.
   *
   * @param userService user service
   */
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Lists all IAM users.
   *
   * @return user summaries
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public List<UserResponse> list() {
    return userService.findAll().stream().map(UserResponse::from).toList();
  }

  /**
   * Disables an IAM user.
   *
   * @param userId user id
   * @return updated user
   */
  @PostMapping("/{userId}/disable")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public UserResponse disable(@PathVariable String userId) {
    return UserResponse.from(userService.disable(userId));
  }

  /** IAM user summary. */
  public record UserResponse(String id, String username, String email, boolean enabled) {
    static UserResponse from(IamUser user) {
      return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.isEnabled());
    }
  }
}
