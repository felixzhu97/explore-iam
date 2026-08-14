package com.iam.federation.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RedirectUri")
class RedirectUriTest {

  @Test
  @DisplayName("should accept absolute http and https URIs")
  void shouldAcceptAbsoluteHttpAndHttpsUris() {
    assertThat(new RedirectUri("http://localhost:4200/callback").value())
        .isEqualTo("http://localhost:4200/callback");
    assertThat(new RedirectUri("https://app.example.com/oauth/callback").value())
        .isEqualTo("https://app.example.com/oauth/callback");
  }

  @Test
  @DisplayName("should reject blank fragment javascript and relative URIs")
  void shouldRejectUnsafeUris() {
    assertThatThrownBy(() -> new RedirectUri(" ")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new RedirectUri("javascript:alert(1)"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new RedirectUri("/relative"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new RedirectUri("http://localhost/cb#frag"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
