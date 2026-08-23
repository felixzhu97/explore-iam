package com.iam.common.domain.converter;

import com.iam.common.domain.vo.Arn;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link Arn} value objects as a single ARN column. */
@Converter(autoApply = false)
public class ArnAttributeConverter implements AttributeConverter<Arn, String> {

  @Override
  public String convertToDatabaseColumn(Arn attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public Arn convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new Arn(dbData);
  }
}
