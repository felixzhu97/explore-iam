package com.iam.scim.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScimMetaTest {

  @Test
  @DisplayName("should parse weak and strong etags into versions")
  void shouldParseWeakAndStrongEtagsIntoVersions() {
    assertThat(ScimMeta.parseIfMatch("W/\"3\"")).isEqualTo(3L);
    assertThat(ScimMeta.parseIfMatch("\"4\"")).isEqualTo(4L);
  }

  @Test
  @DisplayName("should skip check when if match is absent or wildcard")
  void shouldSkipCheckWhenIfMatchIsAbsentOrWildcard() {
    assertThat(ScimMeta.parseIfMatch(null)).isNull();
    assertThat(ScimMeta.parseIfMatch("*")).isNull();
  }

  @Test
  @DisplayName("should fail precondition when etag is not a version")
  void shouldFailPreconditionWhenEtagIsNotAVersion() {
    assertThat(ScimMeta.parseIfMatch("W/\"abc\"")).isEqualTo(-1L);
  }
}
