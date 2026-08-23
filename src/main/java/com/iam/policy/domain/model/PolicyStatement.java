package com.iam.policy.domain.model;

import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.Resource;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

/** Single Allow or Deny statement within a Policy Document. */
@Getter
public class PolicyStatement {

  private final Effect effect;
  private final Set<Action> actions;
  private final Set<Resource> resources;

  private PolicyStatement(Effect effect, Set<Action> actions, Set<Resource> resources) {
    this.effect = Objects.requireNonNull(effect, "effect");
    this.actions = Set.copyOf(Objects.requireNonNull(actions, "actions"));
    this.resources = Set.copyOf(Objects.requireNonNull(resources, "resources"));
    if (this.actions.isEmpty()) {
      throw new IllegalArgumentException("actions must not be empty");
    }
    if (this.resources.isEmpty()) {
      throw new IllegalArgumentException("resources must not be empty");
    }
  }

  /**
   * Creates a policy statement.
   *
   * @param effect Allow or Deny
   * @param actions permitted or denied actions
   * @param resources target resources
   * @return statement value object
   */
  public static PolicyStatement of(Effect effect, Set<Action> actions, Set<Resource> resources) {
    return new PolicyStatement(effect, actions, resources);
  }

  /**
   * Returns whether this statement matches the given action and resource.
   *
   * @param action requested action
   * @param resource requested resource
   * @return true when both action and resource match
   */
  public boolean matches(Action action, Resource resource) {
    return matchesAction(action) && matchesResource(resource);
  }

  private boolean matchesAction(Action action) {
    return actions.stream()
        .anyMatch(a -> a.value().equals(action.value()) || a.value().equals("*"));
  }

  private boolean matchesResource(Resource resource) {
    return resources.stream()
        .anyMatch(r -> r.value().equals(resource.value()) || r.value().equals("*"));
  }
}
