package com.iam.policy.service;

import com.iam.audit.service.AuditService;
import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Arn;
import com.iam.common.domain.vo.PrincipalId;
import com.iam.common.domain.vo.Resource;
import com.iam.policy.domain.model.AuthorizationDecision;
import com.iam.policy.domain.model.EvaluationContext;
import com.iam.policy.domain.repository.PolicyRepository;
import com.iam.policy.domain.service.PolicyEngine;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Evaluates policies for a principal, action, and resource. */
@Service
public class EvaluatePolicyService {

  private final PolicyRepository policyRepository;
  private final PolicyEngine policyEngine;
  private final AuditService auditService;

  /**
   * Creates the evaluate-policy use case.
   *
   * @param policyRepository policy repository
   * @param policyEngine policy evaluation engine
   * @param auditService audit application service
   */
  public EvaluatePolicyService(
      PolicyRepository policyRepository, PolicyEngine policyEngine, AuditService auditService) {
    this.policyRepository = policyRepository;
    this.policyEngine = policyEngine;
    this.auditService = auditService;
  }

  /**
   * Evaluates attached policies.
   *
   * @param command evaluation input
   * @return authorization decision
   */
  @Transactional(readOnly = true)
  public AuthorizationDecision execute(EvaluateCommand command) {
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
    auditService.recordAuthorizationDecision(
        command.principalId(),
        command.action(),
        command.resource(),
        decision.effect(),
        decision.reasonCode());
    return decision;
  }

  /** Input for policy evaluation. */
  public record EvaluateCommand(
      String principalId, String principalArn, String action, String resource) {}
}
