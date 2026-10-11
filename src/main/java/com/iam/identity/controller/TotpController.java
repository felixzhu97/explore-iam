package com.iam.identity.controller;

import com.iam.identity.service.TotpService;
import com.iam.identity.service.TotpService.TotpEnrollment;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Management API for TOTP enrollment. */
@RestController
@RequestMapping("/api/v1/totp")
public class TotpController {

  private final TotpService totpService;

  TotpController(TotpService totpService) {
    this.totpService = totpService;
  }

  /**
   * Returns whether TOTP is enabled for the current user.
   *
   * @param authentication current authentication
   * @return status payload
   */
  @GetMapping("/status")
  TotpStatusResponse status(Authentication authentication) {
    return new TotpStatusResponse(totpService.isEnabled(authentication.getName()));
  }

  /**
   * Starts TOTP enrollment and returns one-time secret material.
   *
   * @param authentication current authentication
   * @return enrollment payload
   */
  @PostMapping("/enrollments")
  @ResponseStatus(HttpStatus.CREATED)
  TotpEnrollmentResponse beginEnrollment(Authentication authentication) {
    TotpEnrollment enrollment = totpService.beginEnrollment(authentication.getName());
    return new TotpEnrollmentResponse(
        enrollment.secret(), enrollment.otpAuthUri(), enrollment.backupCodes());
  }

  /**
   * Confirms enrollment with a TOTP code.
   *
   * @param request confirmation code
   * @param authentication current authentication
   */
  @PostMapping("/enrollments/confirm")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void confirm(@RequestBody ConfirmTotpRequest request, Authentication authentication) {
    totpService.confirmEnrollment(authentication.getName(), request.code());
  }

  /** TOTP enabled flag. */
  public record TotpStatusResponse(boolean enabled) {}

  /** One-time enrollment material. */
  public record TotpEnrollmentResponse(
      String secret, String otpAuthUri, List<String> backupCodes) {}

  /** Enrollment confirmation body. */
  public record ConfirmTotpRequest(String code) {}
}
