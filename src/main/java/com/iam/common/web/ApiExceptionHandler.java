package com.iam.common.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps domain validation failures to HTTP error responses. */
@RestControllerAdvice
public class ApiExceptionHandler {

  /**
   * Handles illegal arguments as HTTP 400 responses.
   *
   * @param ex the validation failure
   * @return error payload with {@code invalid_request}
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("error", "invalid_request", "message", ex.getMessage()));
  }
}
