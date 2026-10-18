package com.iam.scim.controller;

import com.iam.scim.service.ScimLocations;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** SCIM endpoint paths and absolute URL helpers. */
final class ScimEndpoints {

  static final String BASE = "/scim/v2";
  static final String USERS = BASE + "/Users";
  static final String GROUPS = BASE + "/Groups";

  private ScimEndpoints() {}

  static ScimLocations currentLocations() {
    String root = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
    return new ScimLocations(root + USERS, root + GROUPS);
  }

  static String base() {
    return ServletUriComponentsBuilder.fromCurrentContextPath().path(BASE).toUriString();
  }
}
