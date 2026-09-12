package com.iam.identity.controller;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.service.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AIP Identity User API.
 *
 * @see <a href="https://google.aip.dev/131">AIP-131 Get</a>
 * @see <a href="https://google.aip.dev/132">AIP-132 List</a>
 * @see <a href="https://google.aip.dev/133">AIP-133 Create</a>
 * @see <a href="https://google.aip.dev/136">AIP-136 Custom methods</a>
 */
@RestController
@RequestMapping("/api/v1/users")
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
  public ListUsersResponse list() {
    List<UserResponse> users = userService.findAll().stream().map(UserResponse::from).toList();
    return new ListUsersResponse(users);
  }

  /**
   * Gets one IAM user.
   *
   * @param user user id
   * @return user
   */
  @GetMapping("/{user}")
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public UserResponse get(@PathVariable String user) {
    return UserResponse.from(userService.get(user));
  }

  /**
   * Creates an IAM user.
   *
   * @param request create payload
   * @return created user
   */
  @PostMapping
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest request) {
    IamUser created =
        userService.create(request.username(), request.email(), request.password());
    return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(created));
  }

  /**
   * Disables an IAM user (AIP-136).
   *
   * @param user user id
   * @return updated user
   */
  @PostMapping("/{user}:disable")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public UserResponse disable(@PathVariable String user) {
    return UserResponse.from(userService.disable(user));
  }

  /**
   * Enables an IAM user (AIP-136).
   *
   * @param user user id
   * @return updated user
   */
  @PostMapping("/{user}:enable")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public UserResponse enable(@PathVariable String user) {
    return UserResponse.from(userService.enable(user));
  }

  /**
   * Resets an IAM user password (AIP-136).
   *
   * @param user user id
   * @param request password payload
   * @return updated user
   */
  @PostMapping("/{user}:resetPassword")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public UserResponse resetPassword(
      @PathVariable String user, @RequestBody ResetPasswordRequest request) {
    return UserResponse.from(userService.resetPassword(user, request.password()));
  }

  /** Create user request. */
  public record CreateUserRequest(String username, String email, String password) {}

  /** Reset password request. */
  public record ResetPasswordRequest(String password) {}

  /** AIP list response. */
  public record ListUsersResponse(List<UserResponse> users) {}

  /** IAM user resource. */
  public record UserResponse(
      String name, String id, String username, String email, boolean enabled) {
    static UserResponse from(IamUser user) {
      return new UserResponse(
          "users/" + user.getId(),
          user.getId(),
          user.getUsername(),
          user.getEmail(),
          user.isLoginEnabled());
    }
  }
}
