package com.iam.identity.domain.converter;

import com.iam.identity.domain.model.ImpersonationPolicy;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link ImpersonationPolicy} as JSON text. */
@Converter(autoApply = false)
public class ImpersonationPolicyConverter
    implements AttributeConverter<ImpersonationPolicy, String> {

  @Override
  public String convertToDatabaseColumn(ImpersonationPolicy attribute) {
    return attribute == null ? null : attribute.json();
  }

  @Override
  public ImpersonationPolicy convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new ImpersonationPolicy(dbData);
  }
}
