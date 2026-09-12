package com.iam.common.domain.converter;

import com.iam.common.domain.vo.Action;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link Action} as a string column. */
@Converter(autoApply = false)
public class ActionAttributeConverter implements AttributeConverter<Action, String> {

  @Override
  public String convertToDatabaseColumn(Action attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public Action convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new Action(dbData);
  }
}
