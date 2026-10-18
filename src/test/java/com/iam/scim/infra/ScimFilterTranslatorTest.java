package com.iam.scim.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.unboundid.scim2.common.exceptions.BadRequestException;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.filters.Filter;
import com.unboundid.scim2.common.types.UserResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScimFilterTranslatorTest {

  private final ScimFilterTranslator translator = new ScimFilterTranslator();

  @Test
  @DisplayName("should expose equality value for indexed lookup")
  void shouldExposeEqualityValueForIndexedLookup() throws BadRequestException {
    Filter filter = translator.parse("userName eq \"alice\"").orElseThrow();

    assertThat(translator.equalityValue(filter, "username")).contains("alice");
    assertThat(translator.equalityValue(filter, "externalId")).isEmpty();
  }

  @Test
  @DisplayName("should evaluate and filter in memory")
  void shouldEvaluateAndFilterInMemory() throws ScimException {
    Filter filter = translator.parse("userName eq \"alice\" and active eq true").orElseThrow();
    UserResource alice = new UserResource().setUserName("alice").setActive(true);
    UserResource inactive = new UserResource().setUserName("alice").setActive(false);

    assertThat(translator.matches(filter, alice)).isTrue();
    assertThat(translator.matches(filter, inactive)).isFalse();
  }

  @Test
  @DisplayName("should return empty for blank filter and reject oversized filter")
  void shouldReturnEmptyForBlankFilterAndRejectOversizedFilter() throws BadRequestException {
    assertThat(translator.parse(" ")).isEmpty();
    assertThatThrownBy(() -> translator.parse("userName eq \"" + "x".repeat(2000) + "\""))
        .isInstanceOf(BadRequestException.class);
  }
}
