package com.iam.policy.domain.converter;

import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.Resource;
import com.iam.policy.domain.model.PolicyStatement;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** Persists policy statements as JSON in a single column. */
@Converter(autoApply = false)
public class PolicyStatementsJsonConverter
    implements AttributeConverter<List<PolicyStatement>, String> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<PolicyStatement> attribute) {
    if (attribute == null) {
      return null;
    }
    try {
      List<Map<String, Object>> payload = new ArrayList<>();
      for (PolicyStatement statement : attribute) {
        payload.add(
            Map.of(
                "effect", statement.effect().name(),
                "actions", statement.actions().stream().map(Action::value).toList(),
                "resources", statement.resources().stream().map(Resource::value).toList()));
      }
      return OBJECT_MAPPER.writeValueAsString(payload);
    } catch (JacksonException ex) {
      throw new IllegalStateException("Failed to serialize policy statements", ex);
    }
  }

  @Override
  public List<PolicyStatement> convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return List.of();
    }
    try {
      List<Map<String, Object>> payload =
          OBJECT_MAPPER.readValue(dbData, new TypeReference<List<Map<String, Object>>>() {});
      List<PolicyStatement> statements = new ArrayList<>();
      for (Map<String, Object> row : payload) {
        Effect effect = Effect.valueOf((String) row.get("effect"));
        @SuppressWarnings("unchecked")
        List<String> actions = (List<String>) row.get("actions");
        @SuppressWarnings("unchecked")
        List<String> resources = (List<String>) row.get("resources");
        statements.add(
            PolicyStatement.of(
                effect,
                actions.stream().map(Action::new).collect(Collectors.toSet()),
                resources.stream().map(Resource::new).collect(Collectors.toSet())));
      }
      return List.copyOf(statements);
    } catch (JacksonException ex) {
      throw new IllegalStateException("Failed to deserialize policy statements", ex);
    }
  }
}
