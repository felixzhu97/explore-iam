package com.iam.policy.domain.model;

import com.iam.common.domain.base.AbstractNamedEntity;
import com.iam.policy.domain.converter.PolicyStatementsJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Policy document aggregate: named collection of statements. */
@Entity
@Table(name = "iam_policy_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class PolicyDocument extends AbstractNamedEntity {

  @Column(name = "document_json", nullable = false, columnDefinition = "clob")
  @Convert(converter = PolicyStatementsJsonConverter.class)
  private List<PolicyStatement> statements;

  private PolicyDocument(
      String id,
      String name,
      List<PolicyStatement> statements,
      Instant createdAt,
      Instant updatedAt) {
    super(id, name, createdAt, updatedAt);
    this.statements = List.copyOf(new ArrayList<>(requireStatements(statements)));
  }

  /**
   * Creates a new policy document.
   *
   * @param name display name
   * @param statements policy statements
   * @return new aggregate
   */
  public static PolicyDocument create(String name, List<PolicyStatement> statements) {
    Instant now = Instant.now();
    return new PolicyDocument(UUID.randomUUID().toString(), name, statements, now, now);
  }

  /**
   * Rebuilds from persistence.
   *
   * @param id internal id
   * @param name display name
   * @param statements policy statements
   * @param createdAt creation time
   * @param updatedAt last update time
   * @return reconstituted aggregate
   */
  public static PolicyDocument reconstitute(
      String id,
      String name,
      List<PolicyStatement> statements,
      Instant createdAt,
      Instant updatedAt) {
    return new PolicyDocument(id, name, statements, createdAt, updatedAt);
  }

  private static List<PolicyStatement> requireStatements(List<PolicyStatement> statements) {
    if (statements == null || statements.isEmpty()) {
      throw new IllegalArgumentException("policy must contain at least one statement");
    }
    return statements;
  }
}
