package com.iam.policy.controller;

import com.iam.common.domain.model.ResourceName;
import com.iam.policy.domain.model.AccessDecision;
import com.iam.policy.domain.model.AllowPolicy;
import com.iam.policy.domain.model.PolicyBinding;
import com.iam.policy.service.PolicyService;
import com.iam.policy.service.PolicyService.CreatePolicyCommand;
import com.iam.policy.service.PolicyService.EvaluateCommand;
import com.iam.policy.service.PolicyService.StatementInput;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Policy document CRUD and evaluation API. */
@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

  private final PolicyService policyService;

  /**
   * Creates the policy API controller.
   *
   * @param policyService policy service
   */
  public PolicyController(PolicyService policyService) {
    this.policyService = policyService;
  }

  /**
   * Creates a new policy document.
   *
   * @param request policy creation payload
   * @return created policy
   */
  @PostMapping
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<PolicyResponse> create(@RequestBody CreatePolicyRequest request) {
    AllowPolicy policy =
        policyService.createAllowPolicy(
            new CreatePolicyCommand(
                request.name(),
                request.statements().stream()
                    .map(s -> new StatementInput(s.effect(), s.actions(), s.resources()))
                    .toList()));
    return ResponseEntity.status(HttpStatus.CREATED).body(PolicyResponse.from(policy));
  }

  /**
   * Lists all policy documents.
   *
   * @return policy summaries
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public List<PolicyResponse> list() {
    return policyService.findAll().stream().map(PolicyResponse::from).toList();
  }

  /**
   * Attaches a policy to a principal ARN.
   *
   * @param policy policy id
   * @param request attachment payload
   * @return created attachment
   */
  @PostMapping("/{policy}:attach")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public PolicyBindingResponse attach(
      @PathVariable String policy, @RequestBody AttachPolicyRequest request) {
    PolicyBinding attachment =
        policyService.createPolicyBinding(policy, new ResourceName(request.principalArn()));
    return new PolicyBindingResponse(attachment.getId(), attachment.getPrincipalArn().value());
  }

  /**
   * Evaluates policies for a principal, action, and resource.
   *
   * @param request evaluation payload
   * @return authorization decision
   */
  @PostMapping(":evaluate")
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public AccessDecisionResponse evaluate(@RequestBody EvaluatePolicyRequest request) {
    AccessDecision decision =
        policyService.evaluate(
            new EvaluateCommand(
                request.principalId(),
                request.principalArn(),
                request.action(),
                request.resource()));
    return new AccessDecisionResponse(
        decision.effect().name(), decision.reasonCode().value());
  }

  /** Request body for creating a policy document. */
  public record CreatePolicyRequest(String name, List<StatementRequest> statements) {}

  /** Single policy statement input. */
  public record StatementRequest(String effect, List<String> actions, List<String> resources) {}

  /** Request body for attaching a policy to a principal. */
  public record AttachPolicyRequest(String principalArn) {}

  /** Request body for policy evaluation. */
  public record EvaluatePolicyRequest(
      String principalId, String principalArn, String action, String resource) {}

  /** Policy document summary. */
  public record PolicyResponse(String id, String name) {
    static PolicyResponse from(AllowPolicy policy) {
      return new PolicyResponse(policy.getId(), policy.getName());
    }
  }

  /** Policy attachment summary. */
  public record PolicyBindingResponse(String id, String principalArn) {}

  /** Authorization decision response. */
  public record AccessDecisionResponse(String effect, String reasonCode) {}
}
