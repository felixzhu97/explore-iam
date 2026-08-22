# Glossary | 领域术语表

> Explore IAM — Ubiquitous Language（统一语言）

---

## 1. Purpose | 文档说明

This document defines the project **Ubiquitous Language**. English terms are the **preferred canonical names** and must align with code, API, and architecture naming. Chinese labels are for localization and stakeholder communication only.

### Maintenance Principles

1. **Glossary first**: Add or update terms here before implementing code
2. **Code sync**: Domain model changes (entity, value object, enum) must update the corresponding glossary entry
3. **Preferred term**: Use the **Preferred Term (English)** column for code, API, Jira keys, commits, and technical docs

### Reference Rules

| Scenario                   | Rule                                                             |
| -------------------------- | ---------------------------------------------------------------- |
| Java class / API / commits | Use Preferred Term (English)                                     |
| Jira / user stories        | English preferred; Chinese may appear in parentheses for clarity |
| Frontend i18n              | Map English preferred terms to localized UI copy                 |
| Cross-team communication   | Lead with English; add Chinese when needed                       |

---

## 2. Business Domains | 业务域总览

| Preferred Term | 中文   | Java Package（planned） | Frontend Route（planned） | API Prefix（planned） | Feature Flag | Notes |
| -------------- | ------ | ----------------------- | ------------------------- | --------------------- | ------------ | ----- |
| Identity       | 身份   | `com.iam.identity`      | `/identity`               | `/api/identity`       | —            | IAM User (local login implemented); Group / Role planned |
| Policy         | 策略   | `com.iam.policy`        | `/policies`               | `/api/policies`       | —            | Policy documents + evaluation (planned) |
| STS            | 临时凭证 | `com.iam.sts`           | —                         | `/api/sts`            | —            | AssumeRole + temporary credentials (planned) |
| Federation     | 联邦   | `com.iam.federation`    | —                         | OIDC endpoints        | —            | OIDC Provider (SAS) implemented; External IdP planned |
| Console        | 控制台 | —                       | `/`                       | —                     | —            | Angular IAM Console shell |
| Audit          | 审计   | `com.iam.audit`         | `/audit`                  | `/api/audit`          | —            | Management + AuthZ decision events |
| Common         | 横切   | `com.iam.common`        | —                         | —                     | —            | Shared filters, errors, config |

**Frontend route map (canonical, planned)**

| Route        | Preferred Term | API prefix      |
| ------------ | -------------- | --------------- |
| `/identity`  | Identity       | `/api/identity` |
| `/policies`  | Policy         | `/api/policies` |
| `/audit`     | Audit          | `/api/audit`    |
| `/`          | Console        | Control Plane REST |

---

## 3. Identity | 身份

| Preferred Term (English) | 中文     | Definition                                              | Type           | Code Mapping（planned）     | Notes                          |
| ------------------------ | -------- | ------------------------------------------------------- | -------------- | --------------------------- | ------------------------------ |
| Principal                | 主体     | Identity that can make requests (user, role, federated) | Aggregate concept | `Principal`               | Aligns with AWS IAM Principal  |
| IAM User                 | IAM 用户 | Long-lived human or service identity within Explore IAM | Entity         | `IamUser`                   | Distinct from app end-user session |
| Group                    | 组       | Collection of users for shared policy attachment        | Entity         | `Group`                     | —                              |
| Role                     | 角色     | Assumable identity with trust + permission policies     | Entity         | `Role`                      | Used with STS AssumeRole       |
| Federated Principal      | 联邦主体 | Principal mapped from an external IdP assertion         | Entity / VO    | `FederatedPrincipal`        | After Google / GitHub login    |

---

## 4. Policy | 策略

| Preferred Term (English) | 中文           | Definition                                                         | Type        | Code Mapping（planned） | Notes |
| ------------------------ | -------------- | ------------------------------------------------------------------ | ----------- | ----------------------- | ----- |
| Policy Document          | 策略文档       | JSON-like document of statements (Effect, Action, Resource, Condition) | Aggregate | `PolicyDocument`        | — |
| Identity-based Policy    | 基于身份的策略 | Policy attached to User / Group / Role                             | Entity      | `IdentityBasedPolicy`   | — |
| Resource-based Policy    | 基于资源的策略 | Policy attached to a resource                                      | Entity      | `ResourceBasedPolicy`   | — |
| Action                   | 操作           | API or resource operation identifier                               | Value Object | `Action`               | e.g. `iam:CreateUser` |
| Resource                 | 资源           | Target of an Action                                                | Value Object | `Resource`             | ARN-style id planned |
| Condition                | 条件           | Context keys that further constrain a statement                    | Value Object | `Condition`            | — |
| Permission Boundary      | 权限边界       | Maximum permissions a principal may be granted                     | Entity      | `PermissionBoundary`   | Optional / later |
| Authorization Decision   | 鉴权决策       | Allow or Deny result for Principal + Action + Resource + Context   | Value Object | `AuthorizationDecision` | Explicit Deny over Allow |
| Policy Engine            | 策略引擎       | Domain service that evaluates policies                             | Domain Service | `PolicyEngine`       | Custom; not a Spring starter |

---

## 5. STS | 临时凭证

| Preferred Term (English) | 中文       | Definition                                              | Type      | Code Mapping（planned） | Notes |
| ------------------------ | ---------- | ------------------------------------------------------- | --------- | ----------------------- | ----- |
| AssumeRole               | 扮演角色   | Exchange caller identity for a Role session             | Use Case  | `AssumeRoleUseCase`     | Trust policy must Allow |
| Temporary Credentials    | 临时凭证   | Short-lived credentials issued after AssumeRole         | Value Object | `TemporaryCredentials` | May reuse OAuth2TokenGenerator / JWT |
| Trust Policy             | 信任策略   | Policy stating who may assume a Role                    | Entity    | `TrustPolicy`           | Evaluated before session issue |
| Assumed Role Session     | 扮演会话   | Active session bound to a Role and expiry               | Entity    | `AssumedRoleSession`    | — |

---

## 6. Federation | 联邦与 SSO

| Preferred Term (English) | 中文              | Definition                                              | Type       | Code Mapping（planned） | Notes |
| ------------------------ | ----------------- | ------------------------------------------------------- | ---------- | ----------------------- | ----- |
| OIDC Provider            | OIDC 提供方       | Explore IAM as OpenID Connect issuer                    | Container  | Spring Authorization Server | Authorization code, tokens, JWKS |
| Registered Client        | 注册客户端        | OAuth2 / OIDC client registered for a Relying Party     | Entity     | `RegisteredClient` (SAS) | Console app registration |
| Relying Party            | 依赖方            | Application that trusts Explore IAM for login           | Concept    | —                       | Explore AI, WhatsFeed, Shopping, Low Code |
| External IdP             | 外部身份提供方    | Upstream IdP used for federation                        | System Ext | OAuth2 Client           | Google / GitHub |
| SSO Login                | 单点登录          | User authenticates once and accesses multiple apps      | Use Case   | OIDC Authorization Code | See C4-Sequence-SSOLogin |

---

## 7. Audit | 审计

| Preferred Term (English) | 中文         | Definition                                       | Type   | Code Mapping（planned） | Notes |
| ------------------------ | ------------ | ------------------------------------------------ | ------ | ----------------------- | ----- |
| Audit Event              | 审计事件     | Record of a management or AuthZ action           | Entity | `AuditEvent`            | — |
| Management Event         | 管理事件     | CRUD on users, roles, policies, clients          | Entity | `ManagementEvent`       | — |
| Authorization Decision Log | 鉴权决策日志 | Persisted Allow/Deny decision with reason code | Entity | `AuthorizationDecisionLog` | — |

---

## 8. Console | 控制台

| Preferred Term (English) | 中文       | Definition                          | Type | Code Mapping（planned） | Notes |
| ------------------------ | ---------- | ----------------------------------- | ---- | ----------------------- | ----- |
| IAM Console              | IAM 控制台 | Angular SPA for administrators      | UI   | Angular 22 app          | Routes under `/identity`, `/policies`, `/audit` |
| App Registration         | 应用注册   | UI flow to create a Registered Client | Use Case | Console + Control Plane | — |

---

## 9. Dev Tooling | 开发工具

| Preferred Term (English) | 中文     | Definition                                              | Type    | Code Mapping              | Notes |
| ------------------------ | -------- | ------------------------------------------------------- | ------- | ------------------------- | ----- |
| Orchestrator             | 编排器   | Agent that delegates tasks to specialized Subagents     | Pattern | `.cursor/agents/orchestrator.md` | Cursor agent routing (dev tooling) |
| Subagent                 | 子智能体 | Specialized Agent focused on a single responsibility    | Pattern | `.cursor/agents/*.md`     | e.g. developer, product-owner, business-analyst |

---

## Reference

- [AWS IAM User Guide](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html)
- [C4 model](developer/c4-model/)
- [User Story Map](product-owner/User-Story-Map.md)
