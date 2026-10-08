package com.iam.sts.controller;

import com.iam.sts.service.ShortLivedCredentialService;
import com.iam.sts.service.ShortLivedCredentialService.AssumeRoleCommand;
import com.iam.sts.service.ShortLivedCredentialService.AssumeRoleResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** STS AssumeRole API. */
@RestController
@RequestMapping("/api/v1/shortLivedCredentials")
public class StsController {

  private final ShortLivedCredentialService shortLivedCredentialService;

  /**
   * Creates the STS API controller.
   *
   * @param shortLivedCredentialService assume-role service
   */
  public StsController(ShortLivedCredentialService shortLivedCredentialService) {
    this.shortLivedCredentialService = shortLivedCredentialService;
  }

  /**
   * Issues temporary credentials for an assumed IAM role.
   *
   * @param request assume-role payload
   * @return access token and session metadata
   */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public AssumeRoleResponse createShortLivedCredential(@RequestBody AssumeRoleRequest request) {
    AssumeRoleResult result =
        shortLivedCredentialService.createShortLivedCredential(
            new AssumeRoleCommand(request.roleArn(), request.sessionName()));
    return new AssumeRoleResponse(
        result.accessToken(), result.expiration().toString(), result.sessionId());
  }

  /** Request body for AssumeRole. */
  public record AssumeRoleRequest(String roleArn, String sessionName) {}

  /** Temporary credentials returned by AssumeRole. */
  public record AssumeRoleResponse(String accessToken, String expiration, String sessionId) {}
}
