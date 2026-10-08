package com.iam.common.domain.converter;

import com.iam.common.domain.model.Permission;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link Permission} as a string column. */
@Converter(autoApply = false)
public class PermissionAttributeConverter implements AttributeConverter<Permission, String> {

  @Override
  public String convertToDatabaseColumn(Permission attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public Permission convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new Permission(dbData);
  }
}
