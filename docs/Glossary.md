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

| Preferred Term | 中文   | Java Package | Frontend Route | API Prefix | Status | Notes |
| -------------- | ------ | ------------ | -------------- | ---------- | ------ | ----- |
| Identity       | 身份   | `com.iam.identity` | `/identity` | `/api/identity` | partial | `IamUser` + form login; Group / Role expanding |
| Policy         | 策略   | `com.iam.policy` | `/policies` | `/api/policies` | partial | Policy Engine + evaluation API |
| STS            | 临时凭证 | `com.iam.sts` | — | `/api/sts` | partial | AssumeRole + temporary JWT |
| Federation     | 联邦   | `com.iam.federation` | — | OIDC + `/api/clients` | partial | SAS Provider + `FederatedIdentityLink` |
| Console        | 控制台 | — | `/` | — | partial | Login + client registration |
| Audit          | 审计   | `com.iam.audit` | `/audit` | `/api/audit` | partial | Immutable audit aggregates (management + AuthZ) |
| Common         | 横切   | `com.iam.common` | — | — | partial | Shared VOs, security, web errors |

**Frontend route map (canonical)**

| Route        | Preferred Term | API prefix      |
| ------------ | -------------- | --------------- |
| `/identity`  | Identity       | `/api/identity` |
| `/policies`  | Policy         | `/api/policies` |
| `/audit`     | Audit          | `/api/audit`    |
| `/clients`   | App Registration | `/api/clients` |
| `/`          | Console        | Control Plane REST |

---

## 3. Protocols & Standards | 协议与标准

| Preferred Term (English) | 中文 | Definition | Framework Mapping |
| ------------------------ | ---- | ---------- | ----------------- |
| OAuth 2.0 | OAuth 2.0 | Authorization framework; authorization code and refresh token grants | Spring Authorization Server |
| OpenID Connect (OIDC) | 开放身份连接 | Identity layer on OAuth 2.0; `openid` scope | SAS `oidc()` |
| Authorization Code | 授权码 | Browser redirect flow exchanging code for tokens | `/oauth2/authorize` |
| PKCE | PKCE | Proof Key for Code Exchange for public clients | `ClientSettings.requireProofKey` |
| Access Token | 访问令牌 | Token authorizing resource access | JWT via `OAuth2TokenCustomizer` |
| ID Token | 身份令牌 | OIDC token carrying identity claims | `OidcTokenCustomizerConfig` |
| Refresh Token | 刷新令牌 | Long-lived token to obtain new access tokens | SAS `TokenSettings` |
| JWKS | JWKS | JSON Web Key Set for signature verification | `JWKSource`, `/.well-known/jwks.json` |
| JWK | JWK | Single JSON Web Key in a JWKS | `RSAKey` in `AuthorizationServerConfig` |
| JWT | JWT | Self-contained signed token format | `NimbusJwtDecoder` |
| Token Introspection | 令牌自省 | Resource server validates opaque or JWT tokens | SAS introspection endpoint (optional) |
| OAuth2 Client Authentication | OAuth2 客户端认证 | `client_secret_basic`, `client_secret_post`, `none` | Registered client settings |
| SAML 2.0 | SAML 2.0 | Secondary federation protocol (document only) | Not in current scope |

---

## 4. Spring Security & Framework | Spring Security 与框架映射

Terms mapping Explore IAM security behavior to [Spring Security](https://docs.spring.io/spring-security/reference/index.html) and [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html) APIs. Use these names when discussing filters, beans, and configuration — not as domain aggregate names unless listed in a domain section below.

### 4.1 Core Security

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| Spring Security | Spring Security | Servlet security framework for authentication and authorization | `spring-boot-starter-security` | implemented |
| Security Filter Chain | 安全过滤器链 | Ordered chain of servlet filters applying security rules | `SecurityFilterChain` | implemented |
| Authorization Server Filter Chain | 授权服务器过滤器链 | Filter chain scoped to SAS OAuth2/OIDC endpoints (`@Order(1)`) | `authorizationServerSecurityFilterChain` in `WebSecurityConfig` | implemented |
| Default Security Filter Chain | 默认安全过滤器链 | Filter chain for SPA, form login, and management APIs (`@Order(2)`) | `defaultSecurityFilterChain` in `WebSecurityConfig` | implemented |
| Authentication | 认证 | Verifying who the caller is | `Authentication`, `AuthenticationManager` | implemented |
| Authorization | 授权 | Deciding whether an authenticated caller may access a resource | `AuthorizationManager`, `@PreAuthorize` | implemented |
| Security Context | 安全上下文 | Thread-local holder for the current `Authentication` | `SecurityContextHolder` | implemented |
| Principal | 安全主体 | Identity attached to `Authentication` (username, JWT `sub`, etc.) | `Authentication.getPrincipal()` | implemented |
| Granted Authority | 授权权限 | Role or scope string granted to a principal (e.g. `ROLE_IAM_ADMIN`) | `GrantedAuthority`, `SimpleGrantedAuthority` | implemented |
| Authentication Entry Point | 认证入口点 | Handles unauthenticated access (redirect to login, 401, etc.) | `OAuthAwareLoginEntryPoint` | implemented |
| Authentication Success Handler | 认证成功处理器 | Post-login redirect (e.g. continue OAuth authorize URL) | `ContinueUrlAuthenticationSuccessHandler` | implemented |
| Authentication Failure Handler | 认证失败处理器 | Handles failed form login | `LoginAuthenticationFailureHandler` | implemented |
| Authentication Event | 认证事件 | Success/failure events published by Spring Security | `AuthenticationSuccessEvent`, `AbstractAuthenticationFailureEvent` | implemented |
| Enable Web Security | 启用 Web 安全 | Activates Spring Security filter chains | `@EnableWebSecurity` on `WebSecurityConfig` | implemented |

### 4.2 Session & Form Login

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| Form Login | 表单登录 | Username/password authentication via HTML form | `HttpSecurity.formLogin()` | implemented |
| Login Page | 登录页 | SPA route served at `/login` | `form.loginPage("/login")` | implemented |
| User Details Service | 用户详情服务 | Loads `UserDetails` for form login from IAM store | `IamUserDetailsService` implements `UserDetailsService` | implemented |
| User Details | 用户详情 | Spring Security adapter wrapping IAM user + authorities | `org.springframework.security.core.userdetails.User` | implemented |
| Password Encoder | 密码编码器 | One-way hash for stored passwords (never plaintext) | `BCryptPasswordEncoder` | implemented |
| Logout | 登出 | Invalidates session and clears security context | `HttpSecurity.logout()` → `/logout` | implemented |
| Session | 会话 | Server-side HTTP session binding authenticated principal | `HttpSession` + Spring Security context | implemented |

### 4.3 OAuth 2.0 / OIDC (Authorization Server)

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| OAuth2 Authorization Server | OAuth2 授权服务器 | Spring Authorization Server issuer and token endpoints | `HttpSecurity.oauth2AuthorizationServer()` | implemented |
| Authorization Endpoint | 授权端点 | Browser redirect to obtain authorization code | `/oauth2/authorize` | implemented |
| Token Endpoint | 令牌端点 | Exchange code or refresh token for tokens | `/oauth2/token` | implemented |
| OpenID Provider Configuration | OIDC 发现文档 | Machine-readable issuer metadata | `/.well-known/openid-configuration` via `oidc()` | implemented |
| JWK Set Endpoint | JWK 集端点 | Public signing keys for JWT verification | `/.well-known/jwks.json` | implemented |
| Registered Client Repository | 注册客户端仓库 | Persistence adapter for SAS `RegisteredClient` | `JdbcOidcClientRepository` → `oauth2_registered_client` | implemented |
| Authorization Server Settings | 授权服务器设置 | Issuer URL and server-wide SAS options | `AuthorizationServerSettings` | implemented |
| Token Settings | 令牌设置 | Access token and refresh token TTL | `AuthorizationServerTokenSettingsConfig` | implemented |
| Client Settings | 客户端设置 | Per-client PKCE, consent, and metadata | `ClientSettings` on `RegisteredClient` | implemented |
| OAuth2 Token Customizer | 令牌定制器 | Adds claims to issued access/ID tokens | `OidcTokenCustomizerConfig` | implemented |
| JWT Encoder | JWT 编码器 | Signs JWT access tokens and assumed-role tokens | `JwtEncoder` bean in `JwtEncoderConfig` | implemented |
| JWT Decoder | JWT 解码器 | Validates JWT signatures on resource APIs | `oauth2ResourceServer().jwt()` | implemented |
| OAuth2 Resource Server | OAuth2 资源服务器 | Protects APIs by validating Bearer JWTs | `HttpSecurity.oauth2ResourceServer()` on SAS chain | implemented |
| Authorization Grant Type | 授权类型 | e.g. `authorization_code`, `refresh_token` | `AuthorizationGrantType` | implemented |
| Client Authentication Method | 客户端认证方式 | e.g. `client_secret_basic`, `client_secret_post`, `none` | `ClientAuthenticationMethod` | implemented |
| Redirect URI | 重定向 URI | Allowed OAuth callback URL for a client | `RedirectUri` VO, `RegisteredClient.redirectUris` | implemented |
| Scope | 范围 | OAuth scope string (e.g. `openid`, `profile`) | `RegisteredClient.scopes` | implemented |
| Issuer | 签发者 | OIDC issuer identifier URL | `spring.security.oauth2.authorizationserver.issuer` | implemented |

### 4.4 Federation (OAuth2 Client)

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| OAuth2 Client | OAuth2 客户端 | Spring Security client for upstream IdPs | `spring-boot-starter-oauth2-client` | partial |
| OAuth2 Login | OAuth2 登录 | Browser login via external provider | `HttpSecurity.oauth2Login()` | partial |
| Client Registration | 客户端注册 | External IdP client id/secret and endpoints | `ClientRegistration`, `ClientRegistrationRepository` | planned |
| OAuth2 User Service | OAuth2 用户服务 | Maps external user info to local IAM principal | `FederatedLoginService` | partial |
| Federation Link Service | 联邦链接服务 | Resolves or provisions `FederatedIdentityLink` and local user | `FederationLinkService` | partial |
| User Info Endpoint | 用户信息端点 | External IdP profile URL used after OAuth2 login | `oauth2Login().userInfoEndpoint()` | partial |

### 4.5 Method Security & RBAC

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| Method Security | 方法级安全 | RBAC on controller/service methods | `@EnableMethodSecurity` in `MethodSecurityConfig` | implemented |
| PreAuthorize | 预授权 | SpEL expression evaluated before method invocation | `@PreAuthorize("hasRole('IAM_ADMIN')")` | implemented |
| Has Role | 拥有角色 | SpEL helper matching `ROLE_*` granted authority | `hasRole('IAM_ADMIN')` → `ROLE_IAM_ADMIN` | implemented |
| Has Any Role | 拥有任一角色 | SpEL helper matching any of several roles | `hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')` | implemented |
| Is Authenticated | 已认证 | SpEL helper requiring any authenticated principal | `@PreAuthorize("isAuthenticated()")` on STS | implemented |
| IAM Admin Role | IAM 管理员角色 | Full management API access | `SecurityRoles.IAM_ADMIN` (`ROLE_IAM_ADMIN`) | implemented |
| IAM Auditor Role | IAM 审计员角色 | Read-only audit and list APIs | `SecurityRoles.IAM_AUDITOR` (`ROLE_IAM_AUDITOR`) | implemented |
| User Role | 普通用户角色 | Default role for demo / authenticated users | `SecurityRoles.USER` (`ROLE_USER`) | implemented |

### 4.6 CSRF & HTTP Hardening

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| CSRF Token | CSRF 令牌 | Token validating state-changing requests | `CsrfToken` | implemented |
| Cookie CSRF Token Repository | Cookie CSRF 仓库 | Stores CSRF token in `XSRF-TOKEN` cookie for SPA | `CookieCsrfTokenRepository.withHttpOnlyFalse()` | implemented |
| CSRF Token Request Handler | CSRF 请求处理器 | Resolves token from header (`X-XSRF-TOKEN`) or form field | `SpaCsrfTokenRequestHandler` | implemented |
| CSRF Cookie Filter | CSRF Cookie 过滤器 | Ensures CSRF cookie is written on each response | `CsrfCookieFilter` | implemented |
| Security Headers | 安全响应头 | HSTS, frame options, content-type options, etc. | `HttpSecurity.headers(withDefaults())` | implemented |
| Permit All | 全部放行 | Matcher allowing anonymous access | `authorize.requestMatchers(...).permitAll()` | implemented |

### 4.7 Persistence & Domain Kernel

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| Abstract Immutable | 不可变聚合基类 | Domain/JPA base: `id` + `createdAt` | `com.iam.common.domain.base.AbstractImmutable` | implemented |
| Abstract Entity | 可变聚合基类 | Extends immutable; adds `updatedAt` + `@Version` | `AbstractEntity` | implemented |
| Abstract Named Entity | 具名聚合基类 | Mutable aggregate with unique `name` | `AbstractNamedEntity` | implemented |
| Abstract Audit Event | 不可变审计聚合基类 | Immutable audit aggregate base; `occurred_at` column | `AbstractAuditEvent` | implemented |
| Abstract Embeddable | 可嵌入值对象基类 | Embeddable VO base preventing empty composite `null` | `AbstractEmbeddable` | implemented |
| Domain Strings | 域字符串校验 | Shared non-blank string validation | `DomainStrings.requireNonBlank` | implemented |
| Semantic Accessor | 语义化访问器 | Public domain method expressing read intent without exposing persistence fields | Pattern | e.g. `IamUser.isLoginEnabled`, `PolicyDocument.statements` | implemented |
| Attribute Converter | 属性转换器 | JPA mapping between domain type and column | e.g. `ArnAttributeConverter`, `PolicyStatementsJsonConverter` | implemented |
| Optimistic Locking | 乐观锁 | Concurrent update detection via `@Version` | `AbstractEntity.version` | implemented |
| Liquibase Changelog | Liquibase 变更日志 | Versioned SQL schema migrations | `db/changelog/0.1.xml` | implemented |
| Schema Validation | 模式校验 | Hibernate validates entities against DB schema | `spring.jpa.hibernate.ddl-auto: validate` | implemented |

---

## 5. Security Controls | 安全控制

| Preferred Term (English) | 中文 | Definition | Framework Capability | Status |
| ------------------------ | ---- | ---------- | -------------------- | ------ |
| CSRF Protection | CSRF 防护 | Prevents cross-site request forgery on session APIs | `CookieCsrfTokenRepository` | implemented |
| Session Fixation Protection | 会话固定防护 | New session ID after authentication | Spring Security `changeSessionId` | default |
| Method Security | 方法级安全 | RBAC on management APIs | `@EnableMethodSecurity` + `@PreAuthorize` | implemented |
| Security Headers | 安全响应头 | HSTS, frame options, content type options | `http.headers()` | implemented |
| Password Hashing | 密码哈希 | Stored credentials never plaintext | `PasswordEncoder` (BCrypt) | implemented |
| Disabled User | 停用用户 | Principal cannot authenticate when disabled | `UserDetails.isEnabled()` | implemented |
| Consent | 授权同意 | User approves OAuth client scopes | `OAuth2AuthorizationConsentService` | planned |
| Persistent Signing Key | 持久化签名密钥 | JWKS stable across restarts | `SigningKeyEntity`, `PersistentJwkSourceConfig` | implemented |
| Implicit Deny | 隐式拒绝 | No matching Allow → Deny | `PolicyEngine` default | implemented |
| Reason Code | 理由码 | Machine-readable authz/audit outcome | `ReasonCode` VO | implemented |
| Least Privilege Session | 最小权限会话 | Short TTL temporary credentials | STS + `TokenSettings` | implemented |
| IAM Admin Role | IAM 管理员角色 | Console and management API access | `ROLE_IAM_ADMIN` | implemented |
| IAM Auditor Role | IAM 审计员角色 | Read-only audit API access | `ROLE_IAM_AUDITOR` | implemented |

---

## 5.5 Shared Value Objects | 共享值对象

Cross-bounded-context value objects in `com.iam.common.domain.vo`.

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| ARN | ARN | Explore IAM resource name (`arn:iam::explore-iam:…`) | Value Object | `Arn` | implemented |
| User ARN | 用户 ARN | ARN for an IAM User | Factory | `Arn.user` | implemented |
| Role ARN | 角色 ARN | ARN for an IAM Role | Factory | `Arn.role` | implemented |
| Principal ID | 主体 ID | Identifier of the principal under evaluation | Value Object | `PrincipalId` | implemented |
| Action | 操作 | API or resource operation identifier | Value Object | `Action` | implemented |
| Resource | 资源 | Target of an Action | Value Object | `Resource` | implemented |
| Effect | 效力 | `ALLOW` or `DENY` | Enum | `Effect` | implemented |
| Reason Code | 理由码 | Machine-readable authorization outcome | Value Object | `ReasonCode` | implemented |
| Explicit Deny Reason | 显式拒绝理由 | Matched Deny statement | Constant | `ReasonCode.EXPLICIT_DENY` | implemented |
| Explicit Allow Reason | 显式允许理由 | Matched Allow statement | Constant | `ReasonCode.EXPLICIT_ALLOW` | implemented |
| Implicit Deny Reason | 隐式拒绝理由 | No Allow matched | Constant | `ReasonCode.IMPLICIT_DENY` | implemented |

---

## 6. Identity | 身份

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| Principal | 主体 | Identity that can make requests | Concept | `Principal` | — |
| IAM User | IAM 用户 | Long-lived human or service identity | Aggregate | `IamUser` | implemented |
| User Status | 用户状态 | `ACTIVE` or `DISABLED` | Enum | `UserStatus` | via `enabled` flag |
| Enable User | 启用用户 | Re-enables form login for a user | Behavior | `IamUser.enable` | implemented |
| Disable User | 禁用用户 | Blocks form login for a user | Behavior | `IamUser.disable` | implemented |
| Change Email | 修改邮箱 | Updates the user's contact email | Behavior | `IamUser.changeEmail` | implemented |
| Has Role | 拥有角色 | Whether a role is assigned to the user | Behavior | `IamUser.hasRole` | implemented |
| Assigned Role IDs | 已分配角色 ID | Read-only view of assigned role ids | Behavior | `IamUser.assignedRoleIds` | implemented |
| Encoded Password Hash | 编码密码哈希 | Stored credential for Spring Security only | Behavior | `IamUser.encodedPasswordHash` | implemented |
| Login Enabled | 登录已启用 | Whether form login is permitted | Behavior | `IamUser.isLoginEnabled` | implemented |
| Group | 组 | Collection of users for shared policy attachment | Aggregate | `Group` | implemented |
| Group ARN | 组 ARN | Stable identifier for a Group | Behavior | `Group.arn` | implemented |
| Group Member | 组成员 | Child entity linking a user to a group | Entity | `GroupMember` | implemented |
| Group Membership | 组成员关系 | User belongs to a Group | Behavior | `Group.addMember` | implemented |
| Remove Group Member | 移除组成员 | Removes a user from the group | Behavior | `Group.removeMember` | implemented |
| Has Member | 拥有成员 | Whether a user belongs to the group | Behavior | `Group.hasMember` | implemented |
| Member User IDs | 成员用户 ID | Read-only view of member user ids | Behavior | `Group.memberUserIds` | implemented |
| Role Assignment | 角色分配 | User assigned to a Role | Behavior | `IamUser.assignRole` | implemented |
| User Role Assignment | 用户角色分配 | Child entity linking a role to a user | Entity | `UserRoleAssignment` | implemented |
| Role | 角色 | Assumable identity with trust + permission policies | Aggregate | `Role` | implemented |
| Role ARN Accessor | 角色 ARN 访问 | Returns this role's ARN value object | Behavior | `Role.arn` | implemented |
| Trust Policy Accessor | 信任策略访问 | Returns trust policy governing assume-role | Behavior | `Role.trustPolicy` | implemented |
| Role Authority | 角色权限 | Spring Security `ROLE_*` granted authority | Behavior | `Role.authority` | implemented |
| Trust Policy Document | 信任策略文档 | JSON policy defining who may assume a Role | Value Object | `TrustPolicyDocument` | implemented |
| Federated Principal | 联邦主体 | Principal mapped from external IdP | Concept | `provider:subject` username | implemented |
| Create Federated User | 创建联邦用户 | Factory for external IdP login | Behavior | `IamUser.createForFederatedLogin` | implemented |

---

## 7. Policy | 策略

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| Policy Document | 策略文档 | Document of statements (Effect, Action, Resource) | Aggregate | `PolicyDocument` | implemented |
| Policy Statement | 策略语句 | Single Allow or Deny rule block embedded in JSON | Value Object | `PolicyStatement` | implemented |
| Policy Statements | 策略语句集合 | Unmodifiable view of embedded statements | Behavior | `PolicyDocument.statements` | implemented |
| Effect | 效力 | `ALLOW` or `DENY` | Enum | `Effect` | see §5.5 |
| Identity-based Policy | 基于身份的策略 | Policy attached to User / Group / Role | Entity | `IdentityBasedPolicy` | planned |
| Resource-based Policy | 基于资源的策略 | Policy attached to a resource | Entity | `ResourceBasedPolicy` | planned |
| Policy Attachment | 策略附加 | Links Policy Document to a principal ARN | Aggregate | `PolicyAttachment` | implemented |
| Policy ID Reference | 策略 ID 引用 | Attached policy document id | Behavior | `PolicyAttachment.policyId` | implemented |
| Principal ARN | 主体 ARN | ARN the policy is attached to | Behavior | `PolicyAttachment.principalArn` | implemented |
| Action | 操作 | API or resource operation identifier | Value Object | `Action` | see §5.5 |
| Resource | 资源 | Target of an Action | Value Object | `Resource` | see §5.5 |
| Condition | 条件 | Context keys constraining a statement | Value Object | `Condition` | planned |
| Evaluation Context | 求值上下文 | Principal + Action + Resource | Value Object | `EvaluationContext` | implemented |
| Authorization Decision | 鉴权决策 | Allow or Deny with reason | Value Object | `AuthorizationDecision` | implemented |
| Policy Engine | 策略引擎 | Evaluates policies: Deny > Allow > Implicit Deny | Domain Service | `PolicyEngine` | implemented |
| Permission Boundary | 权限边界 | Maximum permissions cap | Entity | `PermissionBoundary` | later |

---

## 8. STS | 临时凭证

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| AssumeRole | 扮演角色 | Exchange caller identity for Role session | Use Case | `AssumeRoleService` | implemented |
| Temporary Credentials | 临时凭证 | Short-lived token after AssumeRole | Value Object | `TemporaryCredentials` | planned |
| Trust Policy | 信任策略 | Policy for who may assume a Role | Concept | `TrustPolicyDocument` on `Role` | implemented |
| Assumed Role Session | 扮演会话 | Active session bound to Role and expiry | Aggregate | `AssumedRoleSession` | implemented |
| Session Name | 会话名称 | Caller-provided session identifier | Behavior | `AssumedRoleSession.sessionName` | implemented |
| Expiration | 过期时间 | Credential validity end | Behavior | `AssumedRoleSession.expiresAt` | implemented |
| Session Expired | 会话已过期 | Whether credentials are no longer valid | Behavior | `AssumedRoleSession.isExpired` | implemented |
| Caller Principal | 调用方主体 | Principal that requested AssumeRole | Behavior | `AssumedRoleSession.callerPrincipal` | implemented |

---

## 9. Federation | 联邦与 SSO

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| OIDC Provider | OIDC 提供方 | Explore IAM as OIDC issuer | Container | Spring Authorization Server | implemented |
| OIDC Client | OIDC 客户端 | Domain aggregate for Relying Party registration | Aggregate | `OidcClient` | implemented |
| Registered Client | 注册客户端 | OAuth2 / OIDC client for a Relying Party | Concept | `OidcClient` / SAS `RegisteredClient` | implemented |
| Client ID | 客户端 ID | OAuth2 / OIDC `client_id` value object | Value Object | `ClientId` | implemented |
| Public Client | 公共客户端 | Client authenticated with `none` only | Behavior | `OidcClient.isPublicClient` | implemented |
| Relying Party | 依赖方 | Application trusting Explore IAM | Concept | — | — |
| External IdP | 外部身份提供方 | Upstream IdP (Google / GitHub) | System Ext | OAuth2 Client | planned |
| Identity Provider | 身份提供方 | Configured external OAuth2 provider | Entity | `IdentityProvider` | planned |
| Federated Identity Link | 联邦身份链接 | Maps external provider + subject to local user | Aggregate | `FederatedIdentityLink` | implemented |
| Linked User ID | 关联用户 ID | Local IAM User id for a federation link | Behavior | `FederatedIdentityLink.linkedUserId` | implemented |
| External Subject | 外部主体 | Subject identifier from the external IdP | Attribute | `FederatedIdentityLink.externalSubject` | implemented |
| Provider | 联邦提供方 | External IdP provider key (e.g. `google`) | Attribute | `FederatedIdentityLink.provider` | implemented |
| OAuth2 Login | OAuth2 登录 | Browser login via external provider | Capability | `oauth2Login()` | partial |
| SSO Login | 单点登录 | One login for multiple Relying Parties | Use Case | OIDC Authorization Code | implemented |

---

## 10. Audit Aggregates | 审计聚合

Immutable **aggregate roots** for append-only audit rows. Not Spring Security
**Authentication Event** (framework — see §4.1).

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| Management Event | 管理事件 | Management-plane action with actor, target, outcome | Aggregate | `ManagementEvent` | implemented |
| Authorization Decision Log | 鉴权决策日志 | Persisted Allow/Deny with reason code | Aggregate | `AuthorizationDecisionLog` | implemented |
| Audit Actor | 审计主体 | Principal that performed a management action | Value Object | `AuditActor` (`@Embeddable`) | implemented |
| Audit Target | 审计目标 | Resource type and id affected by management action | Value Object | `AuditTarget` (`@Embeddable`) | implemented |
| Audit Outcome | 审计结果 | Success or failure of a management operation | Enum | `AuditOutcome` | implemented |
| Occurred At | 发生时间 | When the audit aggregate was recorded | Attribute | `AbstractAuditEvent.getOccurredAt()` | implemented |
| Log Management Action | 记录管理操作 | Factory on `ManagementEvent` | Behavior | `ManagementEvent.logManagementAction` | implemented |
| Log Authentication | 记录认证 | Factory for `auth:login` management events | Behavior | `ManagementEvent.logAuthentication` | implemented |
| Capture Authorization Decision | 捕获鉴权决策 | Factory on `AuthorizationDecisionLog` | Behavior | `AuthorizationDecisionLog.capture` | implemented |
| From Evaluation | 从求值捕获 | Factory from policy evaluation outcome | Behavior | `AuthorizationDecisionLog.fromEvaluation` | implemented |
| Management Audit Recorder | 管理审计记录器 | Application helper persisting management events | Service | `ManagementAuditRecorder` | implemented |
| Identity Disable User Action | 禁用用户操作 | Audited management action | Action | `identity:DisableUser` | implemented |
| Identity Create Group Action | 创建组操作 | Audited management action | Action | `identity:CreateGroup` | implemented |
| Identity Add Group Member Action | 添加组成员操作 | Audited management action | Action | `identity:AddGroupMember` | implemented |
| Identity Create Role Action | 创建角色操作 | Audited management action | Action | `identity:CreateRole` | implemented |
| Identity Assign Role Action | 分配角色操作 | Audited management action | Action | `identity:AssignRole` | implemented |
| Federation Register Client Action | 注册客户端操作 | Audited management action | Action | `federation:RegisterClient` | implemented |
| Policy Create Action | 创建策略操作 | Audited management action | Action | `policy:CreatePolicy` | implemented |
| Policy Attach Action | 附加策略操作 | Audited management action | Action | `policy:AttachPolicy` | implemented |
| STS Assume Role Action | 扮演角色操作 | Audited management action | Action | `sts:AssumeRole` | implemented |
| Was Successful | 是否成功 | Query on management aggregate | Behavior | `ManagementEvent.wasSuccessful` | implemented |
| Is Allowed | 是否允许 | Query on authorization aggregate | Behavior | `AuthorizationDecisionLog.isAllowed` | implemented |
| Explicit Deny | 显式拒绝 | Query when `EXPLICIT_DENY` reason matched | Behavior | `AuthorizationDecisionLog.wasExplicitDeny` | implemented |
| Implicit Deny | 隐式拒绝 | Query when no allow matched | Behavior | `AuthorizationDecisionLog.wasImplicitDeny` | implemented |

---

## 11. Console | 控制台

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| IAM Console | IAM 控制台 | Angular SPA for administrators | UI | Angular 22 app | partial |
| App Registration | 应用注册 | Create Registered Client | Use Case | Console + `/api/clients` | implemented |

---

## 12. Dev Tooling | 开发工具

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Notes |
| ------------------------ | ---- | ---------- | ---- | ------------ | ----- |
| Orchestrator | 编排器 | Agent delegating to Subagents | Pattern | `.cursor/agents/orchestrator.md` | dev tooling |
| Subagent | 子智能体 | Specialized single-purpose Agent | Pattern | `.cursor/agents/*.md` | dev tooling |

---

## Reference

- [AWS IAM User Guide](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [C4 model](developer/c4-model/)
- [User Story Map](product-owner/User-Story-Map.md)
