package com.iam.scim.mapper;

import com.iam.identity.domain.model.ExternalId;
import com.iam.identity.domain.model.Group;
import com.iam.identity.service.GroupProfile;
import com.unboundid.scim2.common.exceptions.BadRequestException;
import com.unboundid.scim2.common.types.GroupResource;
import com.unboundid.scim2.common.types.Member;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Maps IAM groups to SCIM Group resources and back; holds no business rules. */
@Component
public class ScimGroupMapper {

  public static final String RESOURCE_TYPE = "Group";

  /**
   * Maps a group to a SCIM Group resource.
   *
   * @param group IAM group with members loaded
   * @param groupsLocation absolute URL of the {@code /Groups} endpoint
   * @param usersLocation absolute URL of the {@code /Users} endpoint
   * @param includeMembers whether to include {@code members}
   * @return SCIM resource
   */
  public GroupResource toResource(
      Group group, String groupsLocation, String usersLocation, boolean includeMembers) {
    GroupResource resource = new GroupResource();
    resource.setId(group.getId());
    if (group.getExternalId() != null) {
      resource.setExternalId(group.getExternalId().value());
    }
    resource.setDisplayName(group.getName());
    if (includeMembers) {
      resource.setMembers(
          group.memberUserIds().stream()
              .map(
                  userId ->
                      new Member()
                          .setValue(userId)
                          .setType(ScimUserMapper.RESOURCE_TYPE)
                          .setRef(URI.create(usersLocation + "/" + userId)))
              .toList());
    }
    resource.setMeta(
        ScimMeta.create(
            RESOURCE_TYPE,
            groupsLocation + "/" + group.getId(),
            group.getCreatedAt(),
            group.getUpdatedAt(),
            group.getVersion()));
    return resource;
  }

  /**
   * Maps a SCIM Group resource to the desired group state.
   *
   * @param resource SCIM resource from the client
   * @return desired state
   * @throws BadRequestException when {@code displayName} or a member value is missing
   */
  public GroupProfile toProfile(GroupResource resource) throws BadRequestException {
    if (resource.getDisplayName() == null || resource.getDisplayName().isBlank()) {
      throw BadRequestException.invalidValue("displayName is required");
    }
    return new GroupProfile(
        resource.getDisplayName(),
        ExternalId.ofNullable(resource.getExternalId()),
        memberIds(resource.getMembers()));
  }

  private static Set<String> memberIds(List<Member> members) throws BadRequestException {
    Set<String> ids = new LinkedHashSet<>();
    if (members == null) {
      return ids;
    }
    for (Member member : members) {
      if (member.getValue() == null || member.getValue().isBlank()) {
        throw BadRequestException.invalidValue("members[].value is required");
      }
      ids.add(member.getValue().trim());
    }
    return ids;
  }
}
