package com.iam.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IamUser")
class IamUserTest {

    @Test
    @DisplayName("should create enabled user when factory is used")
    void shouldCreateEnabledUserWhenFactoryIsUsed() {
        IamUser user = IamUser.create("demo", "demo@example.com", "hash");

        assertThat(user.getId()).isNotBlank();
        assertThat(user.getUsername()).isEqualTo("demo");
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("should disable user when disable is called")
    void shouldDisableUserWhenDisableIsCalled() {
        IamUser user = IamUser.create("demo", "demo@example.com", "hash");

        user.disable();

        assertThat(user.isEnabled()).isFalse();
    }
}
