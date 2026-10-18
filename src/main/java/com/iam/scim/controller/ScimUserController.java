package com.iam.scim.controller;

import static com.iam.scim.infra.ScimJsonHttpMessageConverter.SCIM_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.iam.scim.mapper.ScimMeta;
import com.iam.scim.service.ScimLocations;
import com.iam.scim.service.ScimProvisioningService;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.messages.ListResponse;
import com.unboundid.scim2.common.messages.PatchRequest;
import com.unboundid.scim2.common.types.UserResource;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** SCIM 2.0 {@code /Users} endpoint (RFC 7644 §3). */
@RestController
@RequestMapping(
    path = ScimEndpoints.USERS,
    produces = {SCIM_JSON_VALUE, APPLICATION_JSON_VALUE})
public class ScimUserController {

  private final ScimProvisioningService provisioningService;

  /**
   * Creates the SCIM users controller.
   *
   * @param provisioningService SCIM provisioning orchestration
   */
  public ScimUserController(ScimProvisioningService provisioningService) {
    this.provisioningService = provisioningService;
  }

  /**
   * Lists users.
   *
   * @param filter SCIM filter
   * @param startIndex 1-based start index
   * @param count page size
   * @return list response
   * @throws ScimException when the filter is invalid
   */
  @GetMapping
  public ListResponse<UserResource> list(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) Integer startIndex,
      @RequestParam(required = false) Integer count)
      throws ScimException {
    return provisioningService.listUsers(
        filter, startIndex, count, ScimEndpoints.currentLocations());
  }

  /**
   * Returns one user.
   *
   * @param id IAM user id
   * @return SCIM user with ETag
   */
  @GetMapping("/{id}")
  public ResponseEntity<UserResource> get(@PathVariable String id) {
    return withEtag(ResponseEntity.ok(), provisioningService.getUser(id, locations()));
  }

  /**
   * Creates a user.
   *
   * @param body SCIM user
   * @return created SCIM user with Location and ETag
   * @throws ScimException when the body is invalid
   */
  @PostMapping(consumes = {SCIM_JSON_VALUE, APPLICATION_JSON_VALUE})
  public ResponseEntity<UserResource> create(@RequestBody UserResource body)
      throws ScimException {
    UserResource created = provisioningService.createUser(body, locations());
    return withEtag(
        ResponseEntity.created(URI.create(created.getMeta().getLocation().toString())), created);
  }

  /**
   * Replaces a user.
   *
   * @param id IAM user id
   * @param ifMatch optional weak ETag
   * @param body SCIM user
   * @return updated SCIM user with ETag
   * @throws ScimException when the body is invalid
   */
  @PutMapping(
      path = "/{id}",
      consumes = {SCIM_JSON_VALUE, APPLICATION_JSON_VALUE})
  public ResponseEntity<UserResource> replace(
      @PathVariable String id,
      @RequestHeader(name = HttpHeaders.IF_MATCH, required = false) String ifMatch,
      @RequestBody UserResource body)
      throws ScimException {
    return withEtag(
        ResponseEntity.ok(),
        provisioningService.replaceUser(id, body, ScimMeta.parseIfMatch(ifMatch), locations()));
  }

  /**
   * Applies a PATCH request.
   *
   * @param id IAM user id
   * @param ifMatch optional weak ETag
   * @param patch SCIM PatchOp
   * @return updated SCIM user with ETag
   * @throws ScimException when the patch is invalid
   */
  @PatchMapping(
      path = "/{id}",
      consumes = {SCIM_JSON_VALUE, APPLICATION_JSON_VALUE})
  public ResponseEntity<UserResource> patch(
      @PathVariable String id,
      @RequestHeader(name = HttpHeaders.IF_MATCH, required = false) String ifMatch,
      @RequestBody PatchRequest patch)
      throws ScimException {
    return withEtag(
        ResponseEntity.ok(),
        provisioningService.patchUser(id, patch, ScimMeta.parseIfMatch(ifMatch), locations()));
  }

  /**
   * Deletes a user.
   *
   * @param id IAM user id
   * @param ifMatch optional weak ETag
   * @return 204
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable String id,
      @RequestHeader(name = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
    provisioningService.deleteUser(id, ScimMeta.parseIfMatch(ifMatch));
    return ResponseEntity.noContent().build();
  }

  private static ScimLocations locations() {
    return ScimEndpoints.currentLocations();
  }

  private static ResponseEntity<UserResource> withEtag(
      ResponseEntity.BodyBuilder builder, UserResource resource) {
    return builder.eTag(resource.getMeta().getVersion()).body(resource);
  }
}
