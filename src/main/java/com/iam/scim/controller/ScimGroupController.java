package com.iam.scim.controller;

import static com.iam.scim.infra.ScimJsonHttpMessageConverter.SCIM_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.iam.scim.mapper.ScimMeta;
import com.iam.scim.service.ScimLocations;
import com.iam.scim.service.ScimProvisioningService;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.messages.ListResponse;
import com.unboundid.scim2.common.messages.PatchRequest;
import com.unboundid.scim2.common.types.GroupResource;
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

/** SCIM 2.0 {@code /Groups} endpoint (RFC 7644 §3). */
@RestController
@RequestMapping(
    path = ScimEndpoints.GROUPS,
    produces = {SCIM_JSON_VALUE, APPLICATION_JSON_VALUE})
public class ScimGroupController {

  private final ScimProvisioningService provisioningService;

  /**
   * Creates the SCIM groups controller.
   *
   * @param provisioningService SCIM provisioning orchestration
   */
  public ScimGroupController(ScimProvisioningService provisioningService) {
    this.provisioningService = provisioningService;
  }

  /**
   * Lists groups.
   *
   * @param filter SCIM filter
   * @param startIndex 1-based start index
   * @param count page size
   * @param excludedAttributes comma-separated attributes to omit, e.g. {@code members}
   * @return list response
   * @throws ScimException when the filter is invalid
   */
  @GetMapping
  public ListResponse<GroupResource> list(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) Integer startIndex,
      @RequestParam(required = false) Integer count,
      @RequestParam(required = false) String excludedAttributes)
      throws ScimException {
    return provisioningService.listGroups(
        filter,
        startIndex,
        count,
        includeMembers(excludedAttributes),
        ScimEndpoints.currentLocations());
  }

  /**
   * Returns one group.
   *
   * @param id IAM group id
   * @param excludedAttributes comma-separated attributes to omit, e.g. {@code members}
   * @return SCIM group with ETag
   */
  @GetMapping("/{id}")
  public ResponseEntity<GroupResource> get(
      @PathVariable String id,
      @RequestParam(required = false) String excludedAttributes) {
    return withEtag(
        ResponseEntity.ok(),
        provisioningService.getGroup(id, includeMembers(excludedAttributes), locations()));
  }

  /**
   * Creates a group.
   *
   * @param body SCIM group
   * @return created SCIM group with Location and ETag
   * @throws ScimException when the body is invalid
   */
  @PostMapping(consumes = {SCIM_JSON_VALUE, APPLICATION_JSON_VALUE})
  public ResponseEntity<GroupResource> create(@RequestBody GroupResource body)
      throws ScimException {
    GroupResource created = provisioningService.createGroup(body, locations());
    return withEtag(
        ResponseEntity.created(URI.create(created.getMeta().getLocation().toString())), created);
  }

  /**
   * Replaces a group.
   *
   * @param id IAM group id
   * @param ifMatch optional weak ETag
   * @param body SCIM group
   * @return updated SCIM group with ETag
   * @throws ScimException when the body is invalid
   */
  @PutMapping(
      path = "/{id}",
      consumes = {SCIM_JSON_VALUE, APPLICATION_JSON_VALUE})
  public ResponseEntity<GroupResource> replace(
      @PathVariable String id,
      @RequestHeader(name = HttpHeaders.IF_MATCH, required = false) String ifMatch,
      @RequestBody GroupResource body)
      throws ScimException {
    return withEtag(
        ResponseEntity.ok(),
        provisioningService.replaceGroup(id, body, ScimMeta.parseIfMatch(ifMatch), locations()));
  }

  /**
   * Applies a PATCH request.
   *
   * @param id IAM group id
   * @param ifMatch optional weak ETag
   * @param patch SCIM PatchOp
   * @return updated SCIM group with ETag
   * @throws ScimException when the patch is invalid
   */
  @PatchMapping(
      path = "/{id}",
      consumes = {SCIM_JSON_VALUE, APPLICATION_JSON_VALUE})
  public ResponseEntity<GroupResource> patch(
      @PathVariable String id,
      @RequestHeader(name = HttpHeaders.IF_MATCH, required = false) String ifMatch,
      @RequestBody PatchRequest patch)
      throws ScimException {
    return withEtag(
        ResponseEntity.ok(),
        provisioningService.patchGroup(id, patch, ScimMeta.parseIfMatch(ifMatch), locations()));
  }

  /**
   * Deletes a group.
   *
   * @param id IAM group id
   * @param ifMatch optional weak ETag
   * @return 204
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable String id,
      @RequestHeader(name = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
    provisioningService.deleteGroup(id, ScimMeta.parseIfMatch(ifMatch));
    return ResponseEntity.noContent().build();
  }

  private static boolean includeMembers(String excludedAttributes) {
    if (excludedAttributes == null) {
      return true;
    }
    for (String attribute : excludedAttributes.split(",")) {
      if ("members".equalsIgnoreCase(attribute.trim())) {
        return false;
      }
    }
    return true;
  }

  private static ScimLocations locations() {
    return ScimEndpoints.currentLocations();
  }

  private static ResponseEntity<GroupResource> withEtag(
      ResponseEntity.BodyBuilder builder, GroupResource resource) {
    return builder.eTag(resource.getMeta().getVersion()).body(resource);
  }
}
