package com.iam.scim.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iam.identity.domain.model.ExternalId;
import com.iam.identity.domain.model.User;
import com.iam.identity.service.UserProfile;
import com.unboundid.scim2.common.exceptions.BadRequestException;
import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Name;
import com.unboundid.scim2.common.types.UserResource;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScimUserMapperTest {

  private final ScimUserMapper mapper = new ScimUserMapper();

  @Test
  @DisplayName("should map user to scim resource with meta and weak etag")
  void shouldMapUserToScimResourceWithMetaAndWeakEtag() {
    User user =
        User.provisionUser("alice", "alice@example.com", "Alice", new ExternalId("okta-1"));

    UserResource resource = mapper.toResource(user, "https://iam/scim/v2/Users");

    assertThat(resource.getUserName()).isEqualTo("alice");
    assertThat(resource.getExternalId()).isEqualTo("okta-1");
    assertThat(resource.getActive()).isTrue();
    assertThat(resource.getEmails())
        .extracting(Email::getValue)
        .containsExactly("alice@example.com");
    assertThat(resource.getMeta().getVersion()).isEqualTo("W/\"0\"");
    assertThat(resource.getMeta().getLocation().toString())
        .isEqualTo("https://iam/scim/v2/Users/" + user.getId());
  }

  @Test
  @DisplayName("should pick primary email and fall back to formatted name")
  void shouldPickPrimaryEmailAndFallBackToFormattedName() throws BadRequestException {
    UserResource resource = new UserResource().setUserName("bob");
    resource.setEmails(
        List.of(
            new Email().setValue("home@example.com"),
            new Email().setValue("work@example.com").setPrimary(true)));
    resource.setName(new Name().setFormatted("Bob Builder"));

    UserProfile profile = mapper.toProfile(resource);

    assertThat(profile.email()).isEqualTo("work@example.com");
    assertThat(profile.displayName()).isEqualTo("Bob Builder");
    assertThat(profile.active()).isTrue();
    assertThat(profile.externalId()).isNull();
  }

  @Test
  @DisplayName("should reject resource without user name")
  void shouldRejectResourceWithoutUserName() {
    assertThatThrownBy(() -> mapper.toProfile(new UserResource()))
        .isInstanceOf(BadRequestException.class);
  }
}
