package com.iam.common.domain.converter;

import com.iam.common.domain.vo.Resource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link Resource} as a string column. */
@Converter(autoApply = false)
public class ResourceAttributeConverter implements AttributeConverter<Resource, String> {

  @Override
  public String convertToDatabaseColumn(Resource attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public Resource convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new Resource(dbData);
  }
}
