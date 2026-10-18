package com.iam.scim.service;

import com.iam.identity.domain.model.Group;
import com.iam.identity.domain.model.User;
import com.iam.identity.service.GroupProvisioningService;
import com.iam.identity.service.UserProvisioningService;
import com.iam.scim.infra.ScimFilterTranslator;
import com.iam.scim.mapper.ScimGroupMapper;
import com.iam.scim.mapper.ScimUserMapper;
import com.unboundid.scim2.common.GenericScimResource;
import com.unboundid.scim2.common.ScimResource;
import com.unboundid.scim2.common.exceptions.BadRequestException;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.filters.Filter;
import com.unboundid.scim2.common.messages.ListResponse;
import com.unboundid.scim2.common.messages.PatchRequest;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.types.Member;
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.common.utils.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates inbound SCIM calls: maps protocol resources, delegates to the identity provisioning
 * services.
 */
@Service
public class ScimProvisioningService {

  /** Default and maximum page size. */
  public static final int MAX_RESULTS = 200;

  private final UserProvisioningService userProvisioningService;
  private final GroupProvisioningService groupProvisioningService;
  private final ScimUserMapper userMapper;
  private final ScimGroupMapper groupMapper;
  private final ScimFilterTranslator filterTranslator;

  /**
   * Creates the SCIM provisioning service.
   *
   * @param userProvisioningService user provisioning service
   * @param groupProvisioningService group provisioning service
   * @param userMapper SCIM user mapper
   * @param groupMapper SCIM group mapper
   * @param filterTranslator SCIM filter translator
   */
  public ScimProvisioningService(
      UserProvisioningService userProvisioningService,
      GroupProvisioningService groupProvisioningService,
      ScimUserMapper userMapper,
      ScimGroupMapper groupMapper,
      ScimFilterTranslator filterTranslator) {
    this.userProvisioningService = userProvisioningService;
    this.groupProvisioningService = groupProvisioningService;
    this.userMapper = userMapper;
    this.groupMapper = groupMapper;
    this.filterTranslator = filterTranslator;
  }

  /**
   * Lists users matching an optional filter.
   *
   * @param filter SCIM filter, may be blank
   * @param startIndex 1-based start index, may be {@code null}
   * @param count page size, may be {@code null}
   * @param locations endpoint URLs
   * @return list response
   * @throws ScimException when the filter is invalid
   */
  @Transactional(readOnly = true)
  public ListResponse<UserResource> listUsers(
      String filter, Integer startIndex, Integer count, ScimLocations locations)
      throws ScimException {
    Optional<Filter> parsed = filterTranslator.parse(filter);
    List<User> candidates =
        candidates(
            parsed,
            "userName",
            userProvisioningService::findByUsername,
            userProvisioningService::findByExternalId,
            userProvisioningService::findAll);
    List<UserResource> resources = new ArrayList<>();
    for (User user : candidates) {
      resources.add(userMapper.toResource(user, locations.users()));
    }
    return page(filter(resources, parsed), startIndex, count);
  }

  /**
   * Returns one user.
   *
   * @param userId IAM user id
   * @param locations endpoint URLs
   * @return SCIM user
   */
  public UserResource getUser(String userId, ScimLocations locations) {
    return userMapper.toResource(userProvisioningService.get(userId), locations.users());
  }

  /**
   * Creates a user.
   *
   * @param body SCIM user from the client
   * @param locations endpoint URLs
   * @return created SCIM user
   * @throws ScimException when the body is invalid
   */
  public UserResource createUser(UserResource body, ScimLocations locations)
      throws ScimException {
    User user = userProvisioningService.provisionUser(userMapper.toProfile(body));
    return userMapper.toResource(user, locations.users());
  }

  /**
   * Replaces a user.
   *
   * @param userId IAM user id
   * @param body SCIM user from the client
   * @param expectedVersion version from {@code If-Match}, may be {@code null}
   * @param locations endpoint URLs
   * @return updated SCIM user
   * @throws ScimException when the body is invalid
   */
  public UserResource replaceUser(
      String userId, UserResource body, Long expectedVersion, ScimLocations locations)
      throws ScimException {
    User user =
        userProvisioningService.replaceUser(userId, userMapper.toProfile(body), expectedVersion);
    return userMapper.toResource(user, locations.users());
  }

  /**
   * Applies a PATCH request to a user.
   *
   * @param userId IAM user id
   * @param patch SCIM PatchOp
   * @param expectedVersion version from {@code If-Match}, may be {@code null}
   * @param locations endpoint URLs
   * @return updated SCIM user
   * @throws ScimException when the patch is invalid
   */
  public UserResource patchUser(
      String userId, PatchRequest patch, Long expectedVersion, ScimLocations locations)
      throws ScimException {
    User current = userProvisioningService.get(userId);
    UserResource patched =
        applyPatch(patch, userMapper.toResource(current, locations.users()), UserResource.class);
    Long version = expectedVersion == null ? current.getVersion() : expectedVersion;
    User user =
        userProvisioningService.replaceUser(userId, userMapper.toProfile(patched), version);
    return userMapper.toResource(user, locations.users());
  }

  /**
   * Deletes a user.
   *
   * @param userId IAM user id
   * @param expectedVersion version from {@code If-Match}, may be {@code null}
   */
  public void deleteUser(String userId, Long expectedVersion) {
    userProvisioningService.deleteUser(userId, expectedVersion);
  }

  /**
   * Lists groups matching an optional filter.
   *
   * @param filter SCIM filter, may be blank
   * @param startIndex 1-based start index, may be {@code null}
   * @param count page size, may be {@code null}
   * @param includeMembers whether to include {@code members}
   * @param locations endpoint URLs
   * @return list response
   * @throws ScimException when the filter is invalid
   */
  @Transactional(readOnly = true)
  public ListResponse<GroupResource> listGroups(
      String filter,
      Integer startIndex,
      Integer count,
      boolean includeMembers,
      ScimLocations locations)
      throws ScimException {
    Optional<Filter> parsed = filterTranslator.parse(filter);
    List<Group> candidates =
        candidates(
            parsed,
            "displayName",
            groupProvisioningService::findByName,
            groupProvisioningService::findByExternalId,
            groupProvisioningService::findAll);
    List<GroupResource> resources = new ArrayList<>();
    for (Group group : candidates) {
      resources.add(toGroupResource(group, includeMembers || parsed.isPresent(), locations));
    }
    List<GroupResource> matched = filter(resources, parsed);
    if (!includeMembers) {
      matched.forEach(resource -> resource.setMembers((List<Member>) null));
    }
    return page(matched, startIndex, count);
  }

  /**
   * Returns one group.
   *
   * @param groupId IAM group id
   * @param includeMembers whether to include {@code members}
   * @param locations endpoint URLs
   * @return SCIM group
   */
  public GroupResource getGroup(String groupId, boolean includeMembers, ScimLocations locations) {
    return toGroupResource(groupProvisioningService.get(groupId), includeMembers, locations);
  }

  /**
   * Creates a group.
   *
   * @param body SCIM group from the client
   * @param locations endpoint URLs
   * @return created SCIM group
   * @throws ScimException when the body is invalid
   */
  public GroupResource createGroup(GroupResource body, ScimLocations locations)
      throws ScimException {
    Group group = groupProvisioningService.provisionGroup(groupMapper.toProfile(body));
    return toGroupResource(group, true, locations);
  }

  /**
   * Replaces a group.
   *
   * @param groupId IAM group id
   * @param body SCIM group from the client
   * @param expectedVersion version from {@code If-Match}, may be {@code null}
   * @param locations endpoint URLs
   * @return updated SCIM group
   * @throws ScimException when the body is invalid
   */
  public GroupResource replaceGroup(
      String groupId, GroupResource body, Long expectedVersion, ScimLocations locations)
      throws ScimException {
    Group group =
        groupProvisioningService.replaceGroup(
            groupId, groupMapper.toProfile(body), expectedVersion);
    return toGroupResource(group, true, locations);
  }

  /**
   * Applies a PATCH request to a group.
   *
   * @param groupId IAM group id
   * @param patch SCIM PatchOp
   * @param expectedVersion version from {@code If-Match}, may be {@code null}
   * @param locations endpoint URLs
   * @return updated SCIM group
   * @throws ScimException when the patch is invalid
   */
  public GroupResource patchGroup(
      String groupId, PatchRequest patch, Long expectedVersion, ScimLocations locations)
      throws ScimException {
    Group current = groupProvisioningService.get(groupId);
    GroupResource patched =
        applyPatch(patch, toGroupResource(current, true, locations), GroupResource.class);
    Long version = expectedVersion == null ? current.getVersion() : expectedVersion;
    Group group =
        groupProvisioningService.replaceGroup(groupId, groupMapper.toProfile(patched), version);
    return toGroupResource(group, true, locations);
  }

  /**
   * Deletes a group.
   *
   * @param groupId IAM group id
   * @param expectedVersion version from {@code If-Match}, may be {@code null}
   */
  public void deleteGroup(String groupId, Long expectedVersion) {
    groupProvisioningService.deleteGroup(groupId, expectedVersion);
  }

  private GroupResource toGroupResource(
      Group group, boolean includeMembers, ScimLocations locations) {
    return groupMapper.toResource(group, locations.groups(), locations.users(), includeMembers);
  }

  private <T> List<T> candidates(
      Optional<Filter> filter,
      String nameAttribute,
      Function<String, Optional<T>> byName,
      Function<String, Optional<T>> byExternalId,
      Supplier<List<T>> all) {
    if (filter.isEmpty()) {
      return all.get();
    }
    Optional<String> name = filterTranslator.equalityValue(filter.get(), nameAttribute);
    if (name.isPresent()) {
      return byName.apply(name.get()).stream().toList();
    }
    Optional<String> externalId = filterTranslator.equalityValue(filter.get(), "externalId");
    if (externalId.isPresent()) {
      return byExternalId.apply(externalId.get()).stream().toList();
    }
    return all.get();
  }

  private <T extends ScimResource> List<T> filter(List<T> resources, Optional<Filter> filter)
      throws ScimException {
    if (filter.isEmpty()) {
      return resources;
    }
    List<T> matched = new ArrayList<>();
    for (T resource : resources) {
      if (filterTranslator.matches(filter.get(), resource)) {
        matched.add(resource);
      }
    }
    return matched;
  }

  private static <T> ListResponse<T> page(List<T> resources, Integer startIndex, Integer count) {
    int start = startIndex == null || startIndex < 1 ? 1 : startIndex;
    int size = count == null ? MAX_RESULTS : Math.max(0, Math.min(count, MAX_RESULTS));
    int from = Math.min(start - 1, resources.size());
    int to = Math.min(from + size, resources.size());
    List<T> pageItems = List.copyOf(resources.subList(from, to));
    return new ListResponse<>(resources.size(), pageItems, start, pageItems.size());
  }

  private static <T> T applyPatch(PatchRequest patch, ScimResource current, Class<T> type)
      throws ScimException {
    if (patch.getOperations() == null || patch.getOperations().isEmpty()) {
      throw BadRequestException.invalidSyntax("Operations is required");
    }
    GenericScimResource patched = patch.apply(current.asGenericScimResource());
    try {
      return JsonUtils.nodeToValue(patched.getObjectNode(), type);
    } catch (RuntimeException ex) {
      throw BadRequestException.invalidValue("patched resource is invalid");
    }
  }
}
