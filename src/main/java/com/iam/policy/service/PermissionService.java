package com.iam.policy.service;

import com.iam.audit.service.AdminActivityRecorder;
import com.iam.common.domain.model.Resource;
import com.iam.policy.domain.model.Permission;
import com.iam.policy.domain.repository.PermissionRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Orchestrates Permission catalog operations. */
@Service
public class PermissionService {

  private final PermissionRepository permissionRepository;
  private final AdminActivityRecorder auditRecorder;

  /**
   * Creates the service.
   *
   * @param permissionRepository catalog repository
   * @param auditRecorder management audit
   */
  public PermissionService(
      PermissionRepository permissionRepository, AdminActivityRecorder auditRecorder) {
    this.permissionRepository = permissionRepository;
    this.auditRecorder = auditRecorder;
  }

  /**
   * Creates a catalog entry.
   *
   * @param command create payload
   * @return saved aggregate
   */
  @Transactional
  public Permission create(CreatePermissionCommand command) {
    if (permissionRepository.existsByCode(command.code())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "permission exists");
    }
    try {
      Permission point =
          Permission.create(
              command.code(),
              command.oauthScope(),
              command.module(),
              new com.iam.common.domain.model.Permission(command.action()),
              new Resource(command.resource()),
              command.description());
      Permission saved = permissionRepository.save(point);
      auditRecorder.recordSuccess(
          "policy:CreatePermission", "Permission", saved.getCode());
      return saved;
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }
  }

  /**
   * Lists permissions with AIP-158 page tokens.
   *
   * @param pageSize page size (default 50, max 200)
   * @param pageToken opaque token from a prior response
   * @return page of points
   */
  @Transactional(readOnly = true)
  public PermissionPage list(Integer pageSize, String pageToken) {
    int size = pageSize == null ? 50 : Math.min(Math.max(pageSize, 1), 200);
    int offset = decodeOffset(pageToken);
    List<Permission> all = permissionRepository.findAll();
    int from = Math.min(offset, all.size());
    int to = Math.min(from + size, all.size());
    List<Permission> slice = all.subList(from, to);
    String next = to < all.size() ? encodeOffset(to) : null;
    return new PermissionPage(slice, next);
  }

  /**
   * Returns one permission by code.
   *
   * @param code business key
   * @return catalog entry
   */
  @Transactional(readOnly = true)
  public Permission get(String code) {
    return permissionRepository
        .findByCode(code)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
  }

  private static int decodeOffset(String pageToken) {
    if (pageToken == null || pageToken.isBlank()) {
      return 0;
    }
    try {
      String raw = new String(Base64.getUrlDecoder().decode(pageToken), StandardCharsets.UTF_8);
      return Integer.parseInt(raw);
    } catch (RuntimeException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid page_token", ex);
    }
  }

  private static String encodeOffset(int offset) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(Integer.toString(offset).getBytes(StandardCharsets.UTF_8));
  }

  /** Create command. */
  public record CreatePermissionCommand(
      String code,
      String oauthScope,
      String module,
      String action,
      String resource,
      String description) {}

  /** AIP-158 list page. */
  public record PermissionPage(List<Permission> permissions, String nextPageToken) {}
}
