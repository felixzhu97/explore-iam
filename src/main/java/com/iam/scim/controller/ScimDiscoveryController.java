package com.iam.scim.controller;

import static com.iam.scim.infra.ScimJsonHttpMessageConverter.SCIM_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.iam.scim.service.ScimProvisioningService;
import com.unboundid.scim2.common.exceptions.ResourceNotFoundException;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.exceptions.ServerErrorException;
import com.unboundid.scim2.common.messages.ListResponse;
import com.unboundid.scim2.common.types.AuthenticationScheme;
import com.unboundid.scim2.common.types.BulkConfig;
import com.unboundid.scim2.common.types.ChangePasswordConfig;
import com.unboundid.scim2.common.types.ETagConfig;
import com.unboundid.scim2.common.types.FilterConfig;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.types.PatchConfig;
import com.unboundid.scim2.common.types.ResourceTypeResource;
import com.unboundid.scim2.common.types.SchemaResource;
import com.unboundid.scim2.common.types.ServiceProviderConfigResource;
import com.unboundid.scim2.common.types.SortConfig;
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.common.utils.SchemaUtils;
import java.beans.IntrospectionException;
import java.net.URI;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** SCIM 2.0 discovery endpoints (RFC 7644 §4). */
@RestController
@RequestMapping(
    path = ScimEndpoints.BASE,
    produces = {SCIM_JSON_VALUE, APPLICATION_JSON_VALUE})
public class ScimDiscoveryController {

  private static final String DOCUMENTATION =
      "https://datatracker.ietf.org/doc/html/rfc7644";

  /**
   * Returns supported features: PATCH, filter, ETag; no Bulk, sort, or password change.
   *
   * @return service provider configuration
   */
  @GetMapping("/ServiceProviderConfig")
  public ServiceProviderConfigResource serviceProviderConfig() {
    return new ServiceProviderConfigResource(
        DOCUMENTATION,
        new PatchConfig(true),
        new BulkConfig(false, 0, 0),
        new FilterConfig(true, ScimProvisioningService.MAX_RESULTS),
        new ChangePasswordConfig(false),
        new SortConfig(false),
        new ETagConfig(true),
        List.of(AuthenticationScheme.createOAuth2BearerToken(true)));
  }

  /**
   * Lists resource types.
   *
   * @return User and Group resource types
   * @throws ScimException when schema metadata cannot be built
   */
  @GetMapping("/ResourceTypes")
  public ListResponse<ResourceTypeResource> resourceTypes() throws ScimException {
    return new ListResponse<>(List.of(userType(), groupType()));
  }

  /**
   * Returns one resource type.
   *
   * @param name {@code User} or {@code Group}
   * @return resource type
   * @throws ScimException when the name is unknown
   */
  @GetMapping("/ResourceTypes/{name}")
  public ResourceTypeResource resourceType(@PathVariable String name) throws ScimException {
    if ("User".equals(name)) {
      return userType();
    }
    if ("Group".equals(name)) {
      return groupType();
    }
    throw new ResourceNotFoundException("unknown resource type " + name);
  }

  /**
   * Lists schemas.
   *
   * @return User and Group schemas
   * @throws ScimException when schema metadata cannot be built
   */
  @GetMapping("/Schemas")
  public ListResponse<SchemaResource> schemas() throws ScimException {
    return new ListResponse<>(List.of(schema(UserResource.class), schema(GroupResource.class)));
  }

  /**
   * Returns one schema.
   *
   * @param id schema URN
   * @return schema
   * @throws ScimException when the URN is unknown
   */
  @GetMapping("/Schemas/{id}")
  public SchemaResource schemaById(@PathVariable String id) throws ScimException {
    for (Class<?> type : List.of(UserResource.class, GroupResource.class)) {
      if (SchemaUtils.getSchemaUrn(type).equals(id)) {
        return schema(type);
      }
    }
    throw new ResourceNotFoundException("unknown schema " + id);
  }

  private static ResourceTypeResource userType() {
    return new ResourceTypeResource(
        "User",
        "User",
        "IAM user",
        URI.create("/Users"),
        URI.create(SchemaUtils.getSchemaUrn(UserResource.class)),
        null);
  }

  private static ResourceTypeResource groupType() {
    return new ResourceTypeResource(
        "Group",
        "Group",
        "IAM group",
        URI.create("/Groups"),
        URI.create(SchemaUtils.getSchemaUrn(GroupResource.class)),
        null);
  }

  private static SchemaResource schema(Class<?> type) throws ScimException {
    try {
      return SchemaUtils.getSchema(type);
    } catch (IntrospectionException ex) {
      throw new ServerErrorException("schema metadata unavailable");
    }
  }
}
