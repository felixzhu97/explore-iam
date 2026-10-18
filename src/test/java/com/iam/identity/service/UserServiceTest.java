package com.iam.identity.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.iam.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@DisplayName("UserService")
class UserServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final UserProvisioningService provisioning = mock(UserProvisioningService.class);
  private final UserService service =
      new UserService(
          userRepository,
          mock(PasswordEncoder.class),
          provisioning,
          mock(ApplicationEventPublisher.class));

  @Test
  @DisplayName("should refuse to delete when the acting admin is unknown")
  void shouldRefuseToDeleteWhenTheActingAdminIsUnknown() {
    assertThatThrownBy(() -> service.delete("user-1", null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("403");
    verifyNoInteractions(provisioning);
  }

  @Test
  @DisplayName("should refuse to disable when the acting admin is unknown")
  void shouldRefuseToDisableWhenTheActingAdminIsUnknown() {
    assertThatThrownBy(() -> service.disable("user-1", null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("403");
    verifyNoInteractions(userRepository);
  }
}
