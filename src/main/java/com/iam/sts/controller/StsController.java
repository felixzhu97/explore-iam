package com.iam.sts.controller;

import com.iam.sts.service.AssumeRoleService;
import com.iam.sts.service.AssumeRoleService.AssumeRoleCommand;
import com.iam.sts.service.AssumeRoleService.AssumeRoleResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** STS AssumeRole API. */
@RestController
@RequestMapping("/api/sts")
public class StsController {

  private final AssumeRoleService assumeRoleUseCase;

  /**
   * Creates the STS API controller.
   *
   * @param assumeRoleUseCase assume-role use case
   */
  public StsController(AssumeRoleService assumeRoleUseCase) {
    this.assumeRoleUseCase = assumeRoleUseCase;
  }

  /**
   * Issues temporary credentials for an assumed IAM role.
   *
   * @param request assume-role payload
   * @return access token and session metadata
   */
  @PostMapping("/assume-role")
  @PreAuthorize("isAuthenticated()")
  public AssumeRoleResponse assumeRole(@RequestBody AssumeRoleRequest request) {
    AssumeRoleResult result =
        assumeRoleUseCase.execute(new AssumeRoleCommand(request.roleArn(), request.sessionName()));
    return new AssumeRoleResponse(
        result.accessToken(), result.expiration().toString(), result.sessionId());
  }

  /** Request body for AssumeRole. */
  public record AssumeRoleRequest(String roleArn, String sessionName) {}

  /** Temporary credentials returned by AssumeRole. */
  public record AssumeRoleResponse(String accessToken, String expiration, String sessionId) {}
}
