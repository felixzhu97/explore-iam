package com.iam.scim.controller;

import com.iam.scim.infra.ScimErrors;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.messages.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Translates failures on SCIM endpoints into RFC 7644 §3.12 error bodies. */
@RestControllerAdvice(assignableTypes = {
    ScimUserController.class, ScimGroupController.class, ScimDiscoveryController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScimExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ScimExceptionHandler.class);

  /**
   * Handles SDK exceptions.
   *
   * @param ex SCIM exception
   * @return SCIM error
   */
  @ExceptionHandler(ScimException.class)
  public ResponseEntity<ErrorResponse> handleScim(ScimException ex) {
    return ScimErrors.toResponse(ex.getScimError());
  }

  /**
   * Handles service-layer status exceptions.
   *
   * @param ex status exception
   * @return SCIM error
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException ex) {
    return ScimErrors.toResponse(ScimErrors.fromStatus(ex.getStatusCode().value(), ex.getReason()));
  }

  /**
   * Handles domain validation failures.
   *
   * @param ex validation failure
   * @return 400 invalidValue
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleInvalid(IllegalArgumentException ex) {
    return ScimErrors.toResponse(ScimErrors.error(400, "invalidValue", ex.getMessage()));
  }

  /**
   * Handles unreadable request bodies.
   *
   * @param ex parse failure
   * @return 400 invalidSyntax
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
    return ScimErrors.toResponse(
        ScimErrors.error(400, "invalidSyntax", "request body is not valid SCIM JSON"));
  }

  /**
   * Handles unexpected failures without leaking details.
   *
   * @param ex failure
   * @return 500
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(RuntimeException ex) {
    log.error("SCIM request failed: {}", ex.getClass().getName());
    return ScimErrors.toResponse(ScimErrors.error(500, null, "internal error"));
  }
}
