package com.iam.policy.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iam.policy.domain.model.Permission;
import com.iam.policy.service.PermissionService;
import com.iam.policy.service.PermissionService.CreatePermissionCommand;
import com.iam.policy.service.PermissionService.PermissionPage;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AIP-oriented Permission catalog API ({@code /api/v1/permissions}).
 *
 * @see <a href="https://google.aip.dev/121">AIP-121</a>
 * @see <a href="https://google.aip.dev/158">AIP-158</a>
 */
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

  private final PermissionService permissionService;

  /**
   * Creates the controller.
   *
   * @param permissionService catalog service
   */
  public PermissionController(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  /**
   * Lists permissions.
   *
   * @param pageSize AIP-158 page_size
   * @param pageToken AIP-158 page_token
   * @return list response
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public ListPermissionsResponse list(
      @RequestParam(name = "page_size", required = false) Integer pageSize,
      @RequestParam(name = "page_token", required = false) String pageToken) {
    PermissionPage page = permissionService.list(pageSize, pageToken);
    return new ListPermissionsResponse(
        page.permissions().stream().map(PermissionResponse::from).toList(),
        page.nextPageToken());
  }

  /**
   * Gets one permission by code.
   *
   * @param permission code (resource id)
   * @return permission
   */
  @GetMapping("/{permissionPoint:.+}")
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public PermissionResponse get(@PathVariable String permission) {
    return PermissionResponse.from(permissionService.get(permission));
  }

  /**
   * Creates a permission.
   *
   * @param request create body
   * @return created resource
   */
  @PostMapping
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<PermissionResponse> createPermission(
      @RequestBody CreatePermissionRequest request) {
    String oauthScope = request.oauthScope() != null ? request.oauthScope() : request.code();
    Permission created =
        permissionService.createPermission(
            new CreatePermissionCommand(
                request.code(),
                oauthScope,
                request.module(),
                request.action(),
                request.resource(),
                request.description()));
    return ResponseEntity.status(HttpStatus.CREATED).body(PermissionResponse.from(created));
  }

  /** AIP create request. */
  public record CreatePermissionRequest(
      String code,
      @JsonProperty("oauth_scope") String oauthScope,
      String module,
      String action,
      String resource,
      String description) {}

  /** AIP-158 list response. */
  public record ListPermissionsResponse(
      @JsonProperty("permission_points") List<PermissionResponse> permissions,
      @JsonProperty("next_page_token") String nextPageToken) {}

  /** Permission point resource. */
  public record PermissionResponse(
      String name,
      String code,
      @JsonProperty("oauth_scope") String oauthScope,
      String module,
      String action,
      String resource,
      String description) {

    static PermissionResponse from(Permission point) {
      return new PermissionResponse(
          "permissionPoints/" + point.getCode(),
          point.getCode(),
          point.getOauthScope().value(),
          point.getModule(),
          point.getAction().value(),
          point.getResource().value(),
          point.getDescription());
    }
  }
}
