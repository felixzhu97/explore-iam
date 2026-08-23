package com.iam.policy.service;

import com.iam.audit.service.ManagementAuditRecorder;
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
  private final ManagementAuditRecorder managementAuditRecorder;

  /**
   * Creates the create-policy use case.
   *
   * @param policyRepository policy repository
   * @param managementAuditRecorder management audit recorder
   */
  public CreatePolicyService(
      PolicyRepository policyRepository, ManagementAuditRecorder managementAuditRecorder) {
    this.policyRepository = policyRepository;
    this.managementAuditRecorder = managementAuditRecorder;
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
    PolicyDocument policy =
        policyRepository.save(PolicyDocument.create(command.name(), statements));
    managementAuditRecorder.recordSuccess("policy:CreatePolicy", "PolicyDocument", policy.getId());
    return policy;
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
    PolicyAttachment attachment =
        policyRepository.saveAttachment(PolicyAttachment.attach(policyId, principalArn));
    managementAuditRecorder.recordSuccess(
        "policy:AttachPolicy", "PolicyAttachment", attachment.getId());
    return attachment;
  }

  /** Input for creating a policy document. */
  public record CreatePolicyCommand(String name, List<StatementInput> statements) {}

  /** Single policy statement input. */
  public record StatementInput(String effect, List<String> actions, List<String> resources) {}
}
