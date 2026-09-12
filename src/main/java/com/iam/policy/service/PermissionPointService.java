package com.iam.policy.service;

import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Resource;
import com.iam.policy.domain.model.PermissionPoint;
import com.iam.policy.domain.repository.PermissionPointRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Orchestrates Permission Point catalog operations. */
@Service
public class PermissionPointService {

  private final PermissionPointRepository permissionPointRepository;
  private final ManagementAuditRecorder auditRecorder;

  /**
   * Creates the service.
   *
   * @param permissionPointRepository catalog repository
   * @param auditRecorder management audit
   */
  public PermissionPointService(
      PermissionPointRepository permissionPointRepository, ManagementAuditRecorder auditRecorder) {
    this.permissionPointRepository = permissionPointRepository;
    this.auditRecorder = auditRecorder;
  }

  /**
   * Creates a catalog entry.
   *
   * @param command create payload
   * @return saved aggregate
   */
  @Transactional
  public PermissionPoint create(CreatePermissionPointCommand command) {
    if (permissionPointRepository.existsByCode(command.code())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "permission point exists");
    }
    try {
      PermissionPoint point =
          PermissionPoint.create(
              command.code(),
              command.oauthScope(),
              command.module(),
              new Action(command.action()),
              new Resource(command.resource()),
              command.description());
      PermissionPoint saved = permissionPointRepository.save(point);
      auditRecorder.recordSuccess(
          "policy:CreatePermissionPoint", "PermissionPoint", saved.getCode());
      return saved;
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }
  }

  /**
   * Lists permission points with AIP-158 page tokens.
   *
   * @param pageSize page size (default 50, max 200)
   * @param pageToken opaque token from a prior response
   * @return page of points
   */
  @Transactional(readOnly = true)
  public PermissionPointPage list(Integer pageSize, String pageToken) {
    int size = pageSize == null ? 50 : Math.min(Math.max(pageSize, 1), 200);
    int offset = decodeOffset(pageToken);
    List<PermissionPoint> all = permissionPointRepository.findAll();
    int from = Math.min(offset, all.size());
    int to = Math.min(from + size, all.size());
    List<PermissionPoint> slice = all.subList(from, to);
    String next = to < all.size() ? encodeOffset(to) : null;
    return new PermissionPointPage(slice, next);
  }

  /**
   * Returns one permission point by code.
   *
   * @param code business key
   * @return catalog entry
   */
  @Transactional(readOnly = true)
  public PermissionPoint get(String code) {
    return permissionPointRepository
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
  public record CreatePermissionPointCommand(
      String code,
      String oauthScope,
      String module,
      String action,
      String resource,
      String description) {}

  /** AIP-158 list page. */
  public record PermissionPointPage(List<PermissionPoint> permissionPoints, String nextPageToken) {}
}
