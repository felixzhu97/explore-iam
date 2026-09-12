package com.iam.identity.controller;

import com.iam.identity.domain.model.Role;
import com.iam.identity.service.RoleService;
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
 * AIP Identity Role API.
 *
 * @see <a href="https://google.aip.dev/136">AIP-136 Custom methods</a>
 */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

  private final RoleService roleService;

  /**
   * Creates the role API controller.
   *
   * @param roleService role service
   */
  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  /**
   * Lists all IAM roles.
   *
   * @return role summaries
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public ListRolesResponse list() {
    return new ListRolesResponse(roleService.findAll().stream().map(RoleResponse::from).toList());
  }

  /**
   * Creates a new IAM role.
   *
   * @param request role creation payload
   * @return created role
   */
  @PostMapping
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<RoleResponse> create(@RequestBody CreateRoleRequest request) {
    String displayName = request.displayName() != null ? request.displayName() : request.name();
    Role role = roleService.create(displayName, request.trustPolicyJson());
    return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.from(role));
  }

  /**
   * Assigns a role to a user (AIP-136).
   *
   * @param role role id
   * @param request member payload
   * @return empty response
   */
  @PostMapping("/{role}:assign")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<Void> assign(
      @PathVariable String role, @RequestBody MemberRequest request) {
    roleService.assignToUser(request.userId(), role);
    return ResponseEntity.noContent().build();
  }

  /**
   * Unassigns a role from a user (AIP-136).
   *
   * @param role role id
   * @param request member payload
   * @return empty response
   */
  @PostMapping("/{role}:unassign")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<Void> unassign(
      @PathVariable String role, @RequestBody MemberRequest request) {
    roleService.unassignFromUser(request.userId(), role);
    return ResponseEntity.noContent().build();
  }

  /** Request body for creating a role. */
  public record CreateRoleRequest(String name, String displayName, String trustPolicyJson) {}

  /** AIP list response. */
  public record ListRolesResponse(List<RoleResponse> roles) {}

  /** IAM role summary. */
  public record RoleResponse(String name, String id, String displayName, String arn) {
    static RoleResponse from(Role role) {
      return new RoleResponse(
          "roles/" + role.getId(), role.getId(), role.getName(), role.arn().value());
    }
  }
}
