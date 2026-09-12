package com.iam.policy.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iam.policy.domain.model.PermissionPoint;
import com.iam.policy.service.PermissionPointService;
import com.iam.policy.service.PermissionPointService.CreatePermissionPointCommand;
import com.iam.policy.service.PermissionPointService.PermissionPointPage;
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
 * AIP-oriented Permission Point catalog API ({@code /api/v1/permissionPoints}).
 *
 * @see <a href="https://google.aip.dev/121">AIP-121</a>
 * @see <a href="https://google.aip.dev/158">AIP-158</a>
 */
@RestController
@RequestMapping("/api/v1/permissionPoints")
public class PermissionPointController {

  private final PermissionPointService permissionPointService;

  /**
   * Creates the controller.
   *
   * @param permissionPointService catalog service
   */
  public PermissionPointController(PermissionPointService permissionPointService) {
    this.permissionPointService = permissionPointService;
  }

  /**
   * Lists permission points.
   *
   * @param pageSize AIP-158 page_size
   * @param pageToken AIP-158 page_token
   * @return list response
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public ListPermissionPointsResponse list(
      @RequestParam(name = "page_size", required = false) Integer pageSize,
      @RequestParam(name = "page_token", required = false) String pageToken) {
    PermissionPointPage page = permissionPointService.list(pageSize, pageToken);
    return new ListPermissionPointsResponse(
        page.permissionPoints().stream().map(PermissionPointResponse::from).toList(),
        page.nextPageToken());
  }

  /**
   * Gets one permission point by code.
   *
   * @param permissionPoint code (resource id)
   * @return permission point
   */
  @GetMapping("/{permissionPoint:.+}")
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public PermissionPointResponse get(@PathVariable String permissionPoint) {
    return PermissionPointResponse.from(permissionPointService.get(permissionPoint));
  }

  /**
   * Creates a permission point.
   *
   * @param request create body
   * @return created resource
   */
  @PostMapping
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<PermissionPointResponse> create(
      @RequestBody CreatePermissionPointRequest request) {
    String oauthScope = request.oauthScope() != null ? request.oauthScope() : request.code();
    PermissionPoint created =
        permissionPointService.create(
            new CreatePermissionPointCommand(
                request.code(),
                oauthScope,
                request.module(),
                request.action(),
                request.resource(),
                request.description()));
    return ResponseEntity.status(HttpStatus.CREATED).body(PermissionPointResponse.from(created));
  }

  /** AIP create request. */
  public record CreatePermissionPointRequest(
      String code,
      @JsonProperty("oauth_scope") String oauthScope,
      String module,
      String action,
      String resource,
      String description) {}

  /** AIP-158 list response. */
  public record ListPermissionPointsResponse(
      @JsonProperty("permission_points") List<PermissionPointResponse> permissionPoints,
      @JsonProperty("next_page_token") String nextPageToken) {}

  /** Permission point resource. */
  public record PermissionPointResponse(
      String name,
      String code,
      @JsonProperty("oauth_scope") String oauthScope,
      String module,
      String action,
      String resource,
      String description) {

    static PermissionPointResponse from(PermissionPoint point) {
      return new PermissionPointResponse(
          "permissionPoints/" + point.getCode(),
          point.getCode(),
          point.oauthScopeValue(),
          point.getModule(),
          point.getAction().value(),
          point.getResource().value(),
          point.getDescription());
    }
  }
}
