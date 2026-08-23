package com.iam.identity.domain.model;

import com.iam.common.domain.base.AbstractNamedEntity;
import com.iam.common.domain.vo.Arn;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Named collection of IAM users. */
@Entity
@Table(name = "iam_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class Group extends AbstractNamedEntity {

  private Group(String id, String name, Instant createdAt, Instant updatedAt) {
    super(id, name, createdAt, updatedAt);
  }

  /**
   * Creates a new IAM group.
   *
   * @param name unique group name
   * @return new aggregate
   */
  public static Group create(String name) {
    Instant now = Instant.now();
    return new Group(UUID.randomUUID().toString(), name, now, now);
  }

  /**
   * Rebuilds a group from persistence.
   *
   * @param id internal id
   * @param name group name
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @return reconstituted aggregate
   */
  public static Group reconstitute(
      String id, String name, Instant createdAt, Instant updatedAt) {
    return new Group(id, name, createdAt, updatedAt);
  }

  /** Returns the ARN for this group. */
  public Arn arn() {
    return new Arn("arn:iam::explore-iam:group/" + getName());
  }
}
