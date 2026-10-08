package com.iam.policy.service;

import com.iam.audit.domain.model.DataAccessLog;
import com.iam.audit.service.AdminActivityRecorder;
import com.iam.audit.service.AuditService;
import com.iam.common.domain.model.Effect;
import com.iam.common.domain.model.Permission;
import com.iam.common.domain.model.PrincipalId;
import com.iam.common.domain.model.Resource;
import com.iam.common.domain.model.ResourceName;
import com.iam.policy.domain.model.AccessDecision;
import com.iam.policy.domain.model.AccessTuple;
import com.iam.policy.domain.model.AllowPolicy;
import com.iam.policy.domain.model.PolicyBinding;
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
  private final AdminActivityRecorder adminActivityRecorder;

  /**
   * Creates the policy service.
   *
   * @param policyRepository policy repository
   * @param policyEngine policy evaluation engine
   * @param auditService audit application service
   * @param adminActivityRecorder management audit recorder
   */
  public PolicyService(
      PolicyRepository policyRepository,
      PolicyEngine policyEngine,
      AuditService auditService,
      AdminActivityRecorder adminActivityRecorder) {
    this.policyRepository = policyRepository;
    this.policyEngine = policyEngine;
    this.auditService = auditService;
    this.adminActivityRecorder = adminActivityRecorder;
  }

  /**
   * Returns all policy documents.
   *
   * @return policy list
   */
  public List<AllowPolicy> findAll() {
    return policyRepository.findAll();
  }

  /**
   * Creates a policy document.
   *
   * @param command creation input
   * @return saved policy
   */
  @Transactional
  public AllowPolicy create(CreatePolicyCommand command) {
    Objects.requireNonNull(command, "command");
    List<PolicyStatement> statements =
        command.statements().stream()
            .map(
                s ->
                    PolicyStatement.of(
                        Effect.valueOf(s.effect()),
                        s.actions().stream().map(Permission::new).collect(Collectors.toSet()),
                        s.resources().stream().map(Resource::new).collect(Collectors.toSet())))
            .toList();
    AllowPolicy policy =
        policyRepository.save(AllowPolicy.create(command.name(), statements));
    adminActivityRecorder.recordSuccess("policy:CreatePolicy", "AllowPolicy", policy.getId());
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
  public PolicyBinding attach(String policyId, ResourceName principalArn) {
    policyRepository
        .findById(policyId)
        .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
    PolicyBinding attachment =
        policyRepository.saveAttachment(PolicyBinding.attach(policyId, principalArn));
    adminActivityRecorder.recordSuccess(
        "policy:AttachPolicy", "PolicyBinding", attachment.getId());
    return attachment;
  }

  /**
   * Evaluates attached policies.
   *
   * @param command evaluation input
   * @return authorization decision
   */
  public AccessDecision evaluate(EvaluateCommand command) {
    Objects.requireNonNull(command, "command");
    ResourceName principalArn = new ResourceName(command.principalArn());
    AccessTuple context =
        new AccessTuple(
            new PrincipalId(command.principalId()),
            new Permission(command.action()),
            new Resource(command.resource()));
    AccessDecision decision =
        policyEngine.evaluate(
            context, policyRepository.findAttachedToPrincipal(principalArn));
    auditService.save(
        DataAccessLog.fromEvaluation(
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
