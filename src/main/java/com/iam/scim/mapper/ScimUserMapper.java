package com.iam.scim.mapper;

import com.iam.identity.domain.model.ExternalId;
import com.iam.identity.domain.model.User;
import com.iam.identity.service.UserProfile;
import com.unboundid.scim2.common.exceptions.BadRequestException;
import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Name;
import com.unboundid.scim2.common.types.UserResource;
import java.util.List;
import org.springframework.stereotype.Component;

/** Maps IAM users to SCIM User resources and back; holds no business rules. */
@Component
public class ScimUserMapper {

  public static final String RESOURCE_TYPE = "User";

  /**
   * Maps a user to a SCIM User resource.
   *
   * @param user IAM user
   * @param usersLocation absolute URL of the {@code /Users} endpoint
   * @return SCIM resource
   */
  public UserResource toResource(User user, String usersLocation) {
    UserResource resource = new UserResource();
    resource.setId(user.getId());
    if (user.getExternalId() != null) {
      resource.setExternalId(user.getExternalId().value());
    }
    resource.setUserName(user.getUsername());
    resource.setDisplayName(user.getDisplayName());
    resource.setActive(user.isEnabled());
    if (user.getEmail() != null) {
      resource.setEmails(
          List.of(new Email().setValue(user.getEmail()).setType("work").setPrimary(true)));
    }
    resource.setMeta(
        ScimMeta.create(
            RESOURCE_TYPE,
            usersLocation + "/" + user.getId(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getVersion()));
    return resource;
  }

  /**
   * Maps a user to the resource IAM pushes to a Provisioning Target. The target's {@code
   * externalId} is the IAM user id, so repeated pushes can find the account again.
   *
   * @param user IAM user
   * @return SCIM resource without target-assigned id or meta
   */
  public UserResource toTargetResource(User user) {
    UserResource resource = new UserResource();
    resource.setExternalId(user.getId());
    resource.setUserName(user.getUsername());
    resource.setDisplayName(user.getDisplayName());
    resource.setActive(user.isEnabled());
    if (user.getEmail() != null) {
      resource.setEmails(
          List.of(new Email().setValue(user.getEmail()).setType("work").setPrimary(true)));
    }
    return resource;
  }

  /**
   * Maps a SCIM User resource to the desired user state.
   *
   * @param resource SCIM resource from the client
   * @return desired state
   * @throws BadRequestException when {@code userName} is missing
   */
  public UserProfile toProfile(UserResource resource) throws BadRequestException {
    if (resource.getUserName() == null || resource.getUserName().isBlank()) {
      throw BadRequestException.invalidValue("userName is required");
    }
    return new UserProfile(
        resource.getUserName(),
        primaryEmail(resource.getEmails()),
        displayName(resource),
        ExternalId.ofNullable(resource.getExternalId()),
        resource.getActive() == null || resource.getActive());
  }

  private static String primaryEmail(List<Email> emails) {
    if (emails == null || emails.isEmpty()) {
      return null;
    }
    return emails.stream()
        .filter(email -> Boolean.TRUE.equals(email.getPrimary()))
        .findFirst()
        .orElse(emails.get(0))
        .getValue();
  }

  private static String displayName(UserResource resource) {
    if (resource.getDisplayName() != null && !resource.getDisplayName().isBlank()) {
      return resource.getDisplayName();
    }
    Name name = resource.getName();
    return name == null ? null : name.getFormatted();
  }
}
