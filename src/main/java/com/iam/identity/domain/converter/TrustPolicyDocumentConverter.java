package com.iam.identity.domain.converter;

import com.iam.identity.domain.model.TrustPolicyDocument;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persists {@link TrustPolicyDocument} as JSON text. */
@Converter(autoApply = false)
public class TrustPolicyDocumentConverter
    implements AttributeConverter<TrustPolicyDocument, String> {

  @Override
  public String convertToDatabaseColumn(TrustPolicyDocument attribute) {
    return attribute == null ? null : attribute.json();
  }

  @Override
  public TrustPolicyDocument convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new TrustPolicyDocument(dbData);
  }
}
