package com.iam.policy.service;

import com.iam.audit.domain.model.AuthorizationDecisionLog;
import com.iam.audit.service.AuditService;
import com.iam.audit.service.ManagementAuditRecorder;
import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Arn;
import com.iam.common.domain.vo.Effect;
import com.iam.common.domain.vo.PrincipalId;
import com.iam.common.domain.vo.Resource;
import com.iam.policy.domain.model.AuthorizationDecision;
import com.iam.policy.domain.model.EvaluationContext;
import com.iam.policy.domain.model.PolicyAttachment;
import com.iam.policy.domain.model.PolicyDocument;
import com.iam.policy.domain.model.PolicyStatement;
import com.iam.policy.domain.repository.PolicyRepository;
import com.iam.policy.domain.service.PolicyEngine;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Policy document management and evaluation. */
@Service
@Transactional(readOnly = true)
public class PolicyService {

  private final PolicyRepository policyRepository;
  private final PolicyEngine policyEngine;
  private final AuditService auditService;
  private final ManagementAuditRecorder managementAuditRecorder;

  /**
   * Creates the policy service.
   *
   * @param policyRepository policy repository
   * @param policyEngine policy evaluation engine
   * @param auditService audit application service
   * @param managementAuditRecorder management audit recorder
   */
  public PolicyService(
      PolicyRepository policyRepository,
      PolicyEngine policyEngine,
      AuditService auditService,
      ManagementAuditRecorder managementAuditRecorder) {
    this.policyRepository = policyRepository;
    this.policyEngine = policyEngine;
    this.auditService = auditService;
    this.managementAuditRecorder = managementAuditRecorder;
  }

  /**
   * Returns all policy documents.
   *
   * @return policy list
   */
  public List<PolicyDocument> findAll() {
    return policyRepository.findAll();
  }

  /**
   * Creates a policy document.
   *
   * @param command creation input
   * @return saved policy
   */
  @Transactional
  public PolicyDocument create(CreatePolicyCommand command) {
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

  /**
   * Evaluates attached policies.
   *
   * @param command evaluation input
   * @return authorization decision
   */
  public AuthorizationDecision evaluate(EvaluateCommand command) {
    Objects.requireNonNull(command, "command");
    Arn principalArn = new Arn(command.principalArn());
    EvaluationContext context =
        new EvaluationContext(
            new PrincipalId(command.principalId()),
            new Action(command.action()),
            new Resource(command.resource()));
    AuthorizationDecision decision =
        policyEngine.evaluate(
            context, policyRepository.findAttachedToPrincipal(principalArn));
    auditService.save(
        AuthorizationDecisionLog.fromEvaluation(
            context.principalId(),
            context.action(),
            context.resource(),
            decision.effect(),
            decision.reasonCode()));
    return decision;
  }

  /** Input for creating a policy document. */
  public record CreatePolicyCommand(String name, List<StatementInput> statements) {}

  /** Single policy statement input. */
  public record StatementInput(String effect, List<String> actions, List<String> resources) {}

  /** Input for policy evaluation. */
  public record EvaluateCommand(
      String principalId, String principalArn, String action, String resource) {}
}
