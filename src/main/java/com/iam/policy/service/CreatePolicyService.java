package com.iam.policy.service;

import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Arn;
import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.Resource;
import com.iam.policy.domain.model.PolicyAttachment;
import com.iam.policy.domain.model.PolicyDocument;
import com.iam.policy.domain.model.PolicyStatement;
import com.iam.policy.domain.repository.PolicyRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates policy documents and attaches them to principals. */
@Service
public class CreatePolicyService {

  private final PolicyRepository policyRepository;

  /**
   * Creates the create-policy use case.
   *
   * @param policyRepository policy repository
   */
  public CreatePolicyService(PolicyRepository policyRepository) {
    this.policyRepository = policyRepository;
  }

  /**
   * Creates a policy document.
   *
   * @param command creation input
   * @return saved policy
   */
  @Transactional
  public PolicyDocument execute(CreatePolicyCommand command) {
    Objects.requireNonNull(command, "command");
    List<PolicyStatement> statements =
        command.statements().stream()
            .map(
                s ->
                    PolicyStatement.of(
                        Effect.valueOf(s.effect()),
                        s.actions().stream().map(Action::new).collect(Collectors.toSet()),
                        s.resources().stream().map(Resource::new).collect(Collectors.toSet())))
            .toList();
    return policyRepository.save(PolicyDocument.create(command.name(), statements));
  }

  /**
   * Attaches a policy to a principal ARN.
   *
   * @param policyId policy id
   * @param principalArn target principal
   * @return attachment
   */
  @Transactional
  public PolicyAttachment attach(String policyId, Arn principalArn) {
    policyRepository
        .findById(policyId)
        .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
    return policyRepository.saveAttachment(PolicyAttachment.attach(policyId, principalArn));
  }

  /** Input for creating a policy document. */
  public record CreatePolicyCommand(String name, List<StatementInput> statements) {}

  /** Single policy statement input. */
  public record StatementInput(String effect, List<String> actions, List<String> resources) {}
}
