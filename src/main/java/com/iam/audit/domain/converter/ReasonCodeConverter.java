package com.iam.audit.domain.converter;

import com.iam.common.domain.vo.ReasonCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link ReasonCode} as a string column. */
@Converter(autoApply = false)
public class ReasonCodeConverter implements AttributeConverter<ReasonCode, String> {

  @Override
  public String convertToDatabaseColumn(ReasonCode attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public ReasonCode convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new ReasonCode(dbData);
  }
}
