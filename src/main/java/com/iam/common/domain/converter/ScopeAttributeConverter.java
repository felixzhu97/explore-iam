package com.iam.common.domain.converter;

import com.iam.common.domain.vo.Scope;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link Scope} as a string column. */
@Converter(autoApply = false)
public class ScopeAttributeConverter implements AttributeConverter<Scope, String> {

  @Override
  public String convertToDatabaseColumn(Scope attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public Scope convertToEntityAttribute(String dbData) {
    return dbData == null ? null : Scope.of(dbData);
  }
}
