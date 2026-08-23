package com.iam.common.infra.persistence;

import java.util.Map;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/** Maps entity class names to existing {@code iam_*} Liquibase table names. */
public class IamPhysicalNamingStrategy implements PhysicalNamingStrategy {

  private static final Map<String, String> TABLE_NAMES =
      Map.ofEntries(
          Map.entry("IamUser", "iam_users"),
          Map.entry("Group", "iam_groups"),
          Map.entry("GroupMember", "iam_group_memberships"),
          Map.entry("UserRoleAssignment", "iam_user_roles"),
          Map.entry("Role", "iam_roles"),
          Map.entry("ManagementEvent", "iam_management_events"),
          Map.entry("AuthorizationDecisionLog", "iam_authorization_decision_logs"),
          Map.entry("FederatedIdentityLink", "iam_federated_identity_links"),
          Map.entry("PolicyDocument", "iam_policy_documents"),
          Map.entry("PolicyAttachment", "iam_policy_attachments"),
          Map.entry("AssumedRoleSession", "iam_assumed_role_sessions"),
          Map.entry("SigningKeyEntity", "iam_signing_keys"));

  @Override
  public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    return identifier;
  }

  @Override
  public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    return identifier;
  }

  @Override
  public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    if (identifier == null) {
      return null;
    }
    String mapped = TABLE_NAMES.get(identifier.getText());
    if (mapped != null) {
      return Identifier.toIdentifier(mapped);
    }
    return Identifier.toIdentifier(camelToSnake(identifier.getText()));
  }

  @Override
  public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    return identifier;
  }

  @Override
  public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    if (identifier == null) {
      return null;
    }
    return Identifier.toIdentifier(camelToSnake(identifier.getText()));
  }

  private static String camelToSnake(String name) {
    return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }
}
