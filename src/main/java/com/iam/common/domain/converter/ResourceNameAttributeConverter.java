package com.iam.common.domain.converter;

import com.iam.common.domain.model.ResourceName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link ResourceName} value objects as a single ARN column. */
@Converter(autoApply = false)
public class ResourceNameAttributeConverter implements AttributeConverter<ResourceName, String> {

  @Override
  public String convertToDatabaseColumn(ResourceName attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public ResourceName convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new ResourceName(dbData);
  }
}
