package com.iam.identity.infra.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iam.common.security.IamUserDetails;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("Active user session filter")
class ActiveUserSessionFilterTest {

  private final UserRepository users = mock(UserRepository.class);

  private final ActiveUserSessionFilter filter = new ActiveUserSessionFilter(users);

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("should keep the session when the user is still active")
  void shouldKeepTheSessionWhenTheUserIsStillActive() throws Exception {
    User alice = User.createUser("alice", null, "{noop}x");
    when(users.findById(alice.getId())).thenReturn(Optional.of(alice));
    MockHttpSession session = signIn(alice.getId(), "alice");

    filter.doFilter(request(session), new MockHttpServletResponse(), new MockFilterChain());

    assertThat(session.isInvalid()).isFalse();
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
  }

  @Test
  @DisplayName("should end the session when the user was disabled")
  void shouldEndTheSessionWhenTheUserWasDisabled() throws Exception {
    User bob = User.createUser("bob", null, "{noop}x");
    bob.disableUser();
    when(users.findById(bob.getId())).thenReturn(Optional.of(bob));
    MockHttpSession session = signIn(bob.getId(), "bob");

    filter.doFilter(request(session), new MockHttpServletResponse(), new MockFilterChain());

    assertThat(session.isInvalid()).isTrue();
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("should end the session when the user was deleted")
  void shouldEndTheSessionWhenTheUserWasDeleted() throws Exception {
    when(users.findById("gone-id")).thenReturn(Optional.empty());
    MockHttpSession session = signIn("gone-id", "gone");

    filter.doFilter(request(session), new MockHttpServletResponse(), new MockFilterChain());

    assertThat(session.isInvalid()).isTrue();
  }

  private static MockHttpSession signIn(String userId, String username) {
    IamUserDetails details = new IamUserDetails(userId, username, "{noop}x", true, List.of());
    SecurityContextHolder.getContext()
        .setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(details, null, List.of()));
    return new MockHttpSession();
  }

  private static MockHttpServletRequest request(MockHttpSession session) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setSession(session);
    return request;
  }
}
