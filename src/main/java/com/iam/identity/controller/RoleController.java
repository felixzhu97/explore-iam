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

/** IAM role management API. */
@RestController
@RequestMapping("/api/identity/roles")
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
  public List<RoleResponse> list() {
    return roleService.findAll().stream().map(RoleResponse::from).toList();
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
    Role role = roleService.create(request.name(), request.trustPolicyJson());
    return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.from(role));
  }

  /**
   * Assigns a role to a user.
   *
   * @param roleId role id
   * @param request member payload
   * @return empty response
   */
  @PostMapping("/{roleId}/assign")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<Void> assign(
      @PathVariable String roleId, @RequestBody MemberRequest request) {
    roleService.assignToUser(request.userId(), roleId);
    return ResponseEntity.noContent().build();
  }

  /** Request body for creating a role. */
  public record CreateRoleRequest(String name, String trustPolicyJson) {}

  /** IAM role summary. */
  public record RoleResponse(String id, String name, String arn) {
    static RoleResponse from(Role role) {
      return new RoleResponse(role.getId(), role.getName(), role.arn().value());
    }
  }
}
