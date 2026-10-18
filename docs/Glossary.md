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

Preferred vocabulary follows [Google Cloud IAM](https://cloud.google.com/iam/docs/overview)
and [OAuth clients](https://support.google.com/cloud/answer/15549257). Value
objects live in `domain.model` (no `domain.vo`).

---

## 2. Business Domains | 业务域总览

| Preferred Term | 中文   | Java Package | Frontend Route | API Prefix | Status | Notes |
| -------------- | ------ | ------------ | -------------- | ---------- | ------ | ----- |
| Identity       | 身份   | `com.iam.identity` | `/identity` | `/api/v1/users`, `/api/v1/groups`, `/api/v1/roles` | partial | AIP-121; `User` / Group / Role / RoleBinding |
| Policy         | 策略   | `com.iam.policy` | `/allow-policies` | `/api/v1/allowPolicies`, `/api/v1/permissions` | partial | Policy Engine + Permission catalog |
| Short-lived credentials | 临时凭证 | `com.iam.sts` | — | `/api/v1/shortLivedCredentials` | partial | ShortLivedCredential JWT |
| Federation     | 联邦   | `com.iam.federation` | `/oauth-clients` | OIDC + `/api/v1/oauthClients` | partial | SAS Provider + `OAuthClient` |
| Console        | 控制台 | — | `/` | — | partial | Login + OAuth client registration |
| Audit          | 审计   | `com.iam.audit` | `/audit` | `/api/v1/adminActivities`, `/api/v1/dataAccessLogs` | partial | AdminActivity + DataAccessLog |
| Provisioning   | 开通   | `com.iam.scim` | — | `/scim/v2` | implemented | SCIM 2.0 Service Provider (IdP → IAM) + SCIM Client (IAM → Explore AI) |
| Common         | 横切   | `com.iam.common` | — | — | partial | Shared model VOs, security, AIP helpers |

**Frontend route map (canonical)**

| Route        | Preferred Term | API prefix (AIP) |
| ------------ | -------------- | ---------------- |
| `/identity`  | Identity       | `/api/v1/users`, `/api/v1/groups`, `/api/v1/roles` |
| `/allow-policies` | Policy    | `/api/v1/allowPolicies`, `/api/v1/permissions` |
| `/audit`     | Audit          | `/api/v1/adminActivities`, `/api/v1/dataAccessLogs` |
| `/oauth-clients` | OAuth Client | `/api/v1/oauthClients` |
| `/`          | Console        | Control Plane REST |

**AIP REST (management APIs)** — Google [API Improvement Proposals](https://google.aip.dev/): resource paths under `/api/v1`, standard Get/List/Create/Update/Delete, AIP-136 custom methods (`POST …/{resource}:disable`), AIP-158 `page_size` / `page_token` / `next_page_token`, AIP-193 `RpcStatus` errors.

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
| SCIM 2.0 | SCIM 2.0 | System for Cross-domain Identity Management; REST provisioning of Users and Groups ([RFC 7643](https://datatracker.ietf.org/doc/html/rfc7643), [RFC 7644](https://datatracker.ietf.org/doc/html/rfc7644)) | UnboundID `scim2-sdk-common` models + Spring MVC |
| SCIM Service Provider | SCIM 服务端 | Server receiving provisioning calls at `/scim/v2` | `com.iam.scim.controller` (IAM); `com.ai.scim` (Explore AI, Users only) |
| SCIM Client | SCIM 客户端 | Caller pushing provisioning changes to a Service Provider | Okta / Entra ID → IAM; `ScimPushService` (IAM → Explore AI) |
| SCIM Media Type | SCIM 媒体类型 | `application/scim+json` request and response body | `ScimJsonHttpMessageConverter` |
| SCIM Discovery | SCIM 发现端点 | `/ServiceProviderConfig`, `/ResourceTypes`, `/Schemas` | `ScimDiscoveryController` |
| SCIM Filter | SCIM 过滤 | `filter` query (`eq`, `and`) on `userName`, `externalId`, `displayName`, `id` | UnboundID `Filter` + `ScimFilterTranslator` |
| SCIM PATCH | SCIM 局部更新 | `PatchOp` with `add` / `replace` / `remove` operations | UnboundID `PatchRequest` |
| List Response | 列表响应 | Paged SCIM list (`startIndex`, `count`, `totalResults`) | UnboundID `ListResponse` |
| SCIM Error | SCIM 错误 | Error body with `status` and `scimType` (RFC 7644 §3.12) | UnboundID `ErrorResponse` |
| Weak ETag | 弱 ETag | `W/"<version>"` from `@Version`; `If-Match` mismatch → 412 | `AbstractEntity.version` |

---

## 4. Spring Security & Framework | Spring Security 与框架映射

Terms mapping Explore IAM security behavior to [Spring Security](https://docs.spring.io/spring-security/reference/index.html) and [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html) APIs. Use these names when discussing filters, beans, and configuration — not as domain aggregate names unless listed in a domain section below.

### 4.1 Core Security

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| Spring Security | Spring Security | Servlet security framework for authentication and authorization | `spring-boot-starter-security` | implemented |
| Security Filter Chain | 安全过滤器链 | Ordered chain of servlet filters applying security rules | `SecurityFilterChain` | implemented |
| Authorization Server Filter Chain | 授权服务器过滤器链 | Filter chain scoped to SAS OAuth2/OIDC endpoints (`@Order(1)`) | `authorizationServerSecurityFilterChain` in `WebSecurityConfig` | implemented |
| SCIM Security Filter Chain | SCIM 安全过滤器链 | Stateless Bearer chain for `/scim/v2/**`; requires `SCOPE_write:iam_scim` (`@Order(2)`) | `scimSecurityFilterChain` in `ScimSecurityConfig` | implemented |
| Default Security Filter Chain | 默认安全过滤器链 | Filter chain for SPA, form login, and management APIs (`@Order(3)`) | `defaultSecurityFilterChain` in `WebSecurityConfig` | implemented |
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

### 4.2.1 Multi-Factor Authentication

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| MFA | 多因素认证 | Require more than one authentication factor before a full session | Spring Security `FactorGrantedAuthority` + factor authorization rules | implemented |
| Factor Authority | 因素权限 | Authority issued when a factor succeeds (e.g. password, OTT) | `FactorGrantedAuthority` (`FACTOR_PASSWORD`, `FACTOR_OTT`, …) | implemented |
| One-Time Token | 一次性令牌 | Server-generated login token delivered out-of-band (magic link); **not** TOTP | `HttpSecurity.oneTimeTokenLogin()` / `OneTimeTokenService` | implemented |
| Passkey | 通行密钥 | WebAuthn public-key credential for phishing-resistant sign-in | `HttpSecurity.webAuthn()` + JDBC WebAuthn repositories | implemented |
| WebAuthn Credential | WebAuthn 凭证 | Stored public key / credential id for a Passkey | Spring Security WebAuthn JDBC tables | implemented |
| TOTP Credential | TOTP 凭证 | Shared secret for authenticator-app codes (RFC 6238) | Domain `TotpCredential` + custom `FACTOR_TOTP` | implemented |

### 4.3 OAuth 2.0 / OIDC (Authorization Server)

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| OAuth2 Authorization Server | OAuth2 授权服务器 | Spring Authorization Server issuer and token endpoints | `HttpSecurity.oauth2AuthorizationServer()` | implemented |
| Authorization Endpoint | 授权端点 | Browser redirect to obtain authorization code | `/oauth2/authorize` | implemented |
| Token Endpoint | 令牌端点 | Exchange code or refresh token for tokens | `/oauth2/token` | implemented |
| OpenID Provider Configuration | OIDC 发现文档 | Machine-readable issuer metadata | `/.well-known/openid-configuration` via `oidc()` | implemented |
| JWK Set Endpoint | JWK 集端点 | Public signing keys for JWT verification | `/.well-known/jwks.json` | implemented |
| Registered Client Repository | 注册客户端仓库 | Persistence adapter for SAS `RegisteredClient` | `JdbcOAuthClientRepository` → `oauth2_registered_client` | implemented |
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
| Scope | 范围 | OAuth scope string; OIDC std (`openid`, `profile`, `email`) plus GitHub-style product scopes (`write:ai_chat`, `admin:chat`) | `RegisteredClient.scopes` / Permission `oauthScope` | implemented |
| Scope Catalog | 范围目录 | Curated set of OAuth scopes backed by Permissions | Permission seeds | implemented |
| GitHub-Style Scope | GitHub 风格范围 | `{access}:{resource}` as in GitHub OAuth (`read:user`, `write:packages`, `admin:org`); access ∈ `read` \| `write` \| `admin` | Permission `oauthScope` | implemented |
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
| Semantic Accessor | 语义化访问器 | Prefer Lombok getters when the field name is the Preferred Term; behavior methods use verb + noun | Pattern | e.g. `User.isLoginEnabled`, `AllowPolicy.getStatements` | implemented |
| Attribute Converter | 属性转换器 | JPA mapping between domain type and column | e.g. `ArnAttributeConverter`, `PolicyStatementsJsonConverter` | implemented |
| Optimistic Locking | 乐观锁 | Concurrent update detection via `@Version` | `AbstractEntity.version` | implemented |
| Liquibase Changelog | Liquibase 变更日志 | Versioned SQL schema migrations | `db/changelog/0.1.xml` | implemented |
| Schema Validation | 模式校验 | Hibernate validates entities against DB schema | `spring.jpa.hibernate.ddl-auto: validate` | implemented |
| Domain Event | 领域事件 | Fact published by an application service after a state change, inside its transaction; a `record` named `*Event` in `<module>/domain/event`, carrying ids only | `ApplicationEventPublisher.publishEvent` | implemented |
| Event Handler | 事件处理方法 | Synchronous `handleXxxEvent` method on the consuming module's service; runs in the publisher's transaction | `@EventListener` | implemented |

### 4.8 Provisioning Delivery

| Preferred Term (English) | 中文 | Definition | Framework Mapping | Status |
| ------------------------ | ---- | ---------- | ----------------- | ------ |
| Event Publication Registry | 事件发布登记表 | Stores each event publication in the business transaction (transactional outbox) | `spring-modulith-starter-jpa` → `event_publication` | implemented |
| Application Module Listener | 应用模块监听器 | Async, after-commit listener in its own transaction | `@ApplicationModuleListener` on `ScimPushListener` | implemented |
| Incomplete Event Publication | 未完成事件发布 | Publication whose listener failed; re-delivered later | `IncompleteEventPublications` | implemented |
| OAuth2 Client HTTP Request Interceptor | OAuth2 客户端请求拦截器 | Adds a `client_credentials` Bearer token to `RestClient` calls | `OAuth2ClientHttpRequestInterceptor` (`iam-scim-outbound`) | implemented |
| Retryable | 可重试 | Retry 5xx / 429 with backoff and jitter; honors `Retry-After` | Spring Framework 7 `@Retryable` | implemented |

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

Cross-bounded-context value objects in `com.iam.common.domain.model`
(Google IAM resource / permission vocabulary).

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| Resource Name | 资源名 | Full resource name (value may still use `arn:iam::…` shape) | Value Object | `ResourceName` | implemented |
| User Resource Name | 用户资源名 | Resource name for a User | Factory | `ResourceName.createUserResourceName` | implemented |
| Role Resource Name | 角色资源名 | Resource name for a Role | Factory | `ResourceName.createRoleResourceName` | implemented |
| Principal ID | 主体 ID | Identifier of the principal under evaluation | Value Object | `PrincipalId` | implemented |
| Permission | 权限 | IAM permission string (also used in allow-policy statements) | Value Object | `com.iam.common.domain.model.Permission` | implemented |
| Resource | 资源 | Target of a Permission | Value Object | `Resource` | implemented |
| Effect | 效力 | `ALLOW` or `DENY` | Enum | `Effect` | implemented |
| Reason Code | 理由码 | Machine-readable authorization outcome | Value Object | `ReasonCode` | implemented |
| Explicit Deny Reason | 显式拒绝理由 | Matched Deny statement | Constant | `ReasonCode.EXPLICIT_DENY` | implemented |
| Explicit Allow Reason | 显式允许理由 | Matched Allow statement | Constant | `ReasonCode.EXPLICIT_ALLOW` | implemented |
| Implicit Deny Reason | 隐式拒绝理由 | No Allow matched | Constant | `ReasonCode.IMPLICIT_DENY` | implemented |

---

## 6. Identity | 身份

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| Principal | 主体 | Identity that can make requests (Google IAM principal) | Concept | `Principal` | — |
| User | 用户 | Long-lived human or service identity | Aggregate | `User` (`directory_user`) | implemented |
| User Status | 用户状态 | `ACTIVE` or `DISABLED` | Enum | via `enabled` flag | implemented |
| Enable User | 启用用户 | Re-enables form login for a user | Behavior | `User.enableUser` | implemented |
| Disable User | 禁用用户 | Blocks form login for a user | Behavior | `User.disableUser` | implemented |
| Change Email | 修改邮箱 | Updates the user's contact email | Behavior | `User.changeEmail` | implemented |
| Create User | 创建用户 | Factory for a local user | Behavior | `User.createUser` | implemented |
| Create Federated User | 创建联邦用户 | Factory for external IdP login | Behavior | `User.createFederatedUser` | implemented |
| External ID | 外部 ID | Identifier assigned by the provisioning client (SCIM `externalId`) | Value Object | `ExternalId` | implemented |
| Display Name | 显示名 | Human-readable name (SCIM `displayName`) | Attribute | `User.displayName` | implemented |
| Provision User | 开通用户 | Factory for a password-less user created by a SCIM Client | Behavior | `User.provisionUser` | implemented |
| Update Profile | 更新资料 | Replaces username, email, and display name | Behavior | `User.updateProfile` | implemented |
| Change External ID | 修改外部 ID | Sets or clears the External ID | Behavior | `User.changeExternalId` / `Group.changeExternalId` | implemented |
| Group | 组 | Collection of users for shared policy binding | Aggregate | `Group` | implemented |
| Group Member | 组成员 | Child entity linking a user to a group | Entity | `GroupMember` | implemented |
| Provision Group | 开通组 | Factory for a group created by a SCIM Client | Behavior | `Group.provisionGroup` | implemented |
| Rename Group | 重命名组 | Changes the group display name | Behavior | `Group.renameGroup` | implemented |
| Replace Members | 替换成员 | Sets the full member list; returns the added / removed ids | Behavior | `Group.replaceMembers` | implemented |
| User Provisioned Event | 用户已开通事件 | User created (console, SCIM, federated sign-in, or seed) | Domain Event | `UserProvisionedEvent` | implemented |
| User Profile Changed Event | 用户资料已变更事件 | Username / email / display name / External ID changed | Domain Event | `UserProfileChangedEvent` | implemented |
| User Deactivated Event | 用户已停用事件 | User disabled | Domain Event | `UserDeactivatedEvent` | implemented |
| User Reactivated Event | 用户已恢复事件 | User re-enabled | Domain Event | `UserReactivatedEvent` | implemented |
| User Password Reset Event | 用户密码已重置事件 | Admin reset a user's password | Domain Event | `UserPasswordResetEvent` | implemented |
| User Role Assigned Event | 用户已分配角色事件 | Role bound to a user | Domain Event | `UserRoleAssignedEvent` | implemented |
| User Role Unassigned Event | 用户已解除角色事件 | Role unbound from a user | Domain Event | `UserRoleUnassignedEvent` | implemented |
| User Deletion Requested Event | 用户删除请求事件 | Published before the user row is deleted so other modules clean up their links in the same transaction | Domain Event | `UserDeletionRequestedEvent` | implemented |
| User Deleted Event | 用户已删除事件 | User removed | Domain Event | `UserDeletedEvent` | implemented |
| Group Created Event | 组已创建事件 | Group created | Domain Event | `GroupCreatedEvent` | implemented |
| Group Profile Changed Event | 组资料已变更事件 | Group name / External ID changed | Domain Event | `GroupProfileChangedEvent` | implemented |
| Group Membership Changed Event | 组成员已变更事件 | User ids added to / removed from a group | Domain Event | `GroupMembershipChangedEvent` | implemented |
| Group Deleted Event | 组已删除事件 | Group removed | Domain Event | `GroupDeletedEvent` | implemented |
| Role Created Event | 角色已创建事件 | Role created | Domain Event | `RoleCreatedEvent` | implemented |
| Role | 角色 | Named collection of permissions (Google IAM role) | Aggregate | `Role` | implemented |
| Role Binding | 角色绑定 | Links a principal to a Role | Entity | `RoleBinding` | implemented |
| Assign Role | 分配角色 | Bind a role to a user | Behavior | `User.assignRole` | implemented |
| Impersonation Policy | 模拟策略 | Who may obtain short-lived credentials for a Role | Value Object | `ImpersonationPolicy` | implemented |
| Federated Identity | 联邦身份 | Maps external provider + subject to local user | Aggregate | `FederatedIdentity` | implemented |
| TOTP Credential | TOTP 凭证 | Authenticator-app second factor (RFC 6238); secret encrypted at rest | Aggregate | `TotpCredential` | implemented |
| OAuth Client | OAuth 客户端 | Relying product registered as an OAuth / OIDC client | Aggregate | `OAuthClient` | implemented |

---

## 7. Policy | 策略

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| Allow Policy | 允许策略 | Document of statements (Effect, Permission, Resource) | Aggregate | `AllowPolicy` | implemented |
| Policy Statement | 策略语句 | Single Allow or Deny rule block | Value Object | `PolicyStatement` | implemented |
| Effect | 效力 | `ALLOW` or `DENY` | Enum | `Effect` | see §5.5 |
| Policy Binding | 策略绑定 | Links an Allow Policy to a principal resource name | Aggregate | `PolicyBinding` | implemented |
| Permission | 权限 | Catalog entry: code, Resource, and GitHub-style `oauthScope` | Aggregate | `com.iam.policy.domain.model.Permission` | implemented |
| Access Tuple | 访问元组 | Principal + Permission + Resource (Policy Troubleshooter shape) | Value Object | `AccessTuple` | implemented |
| Access Decision | 访问决策 | Allow or Deny with reason | Value Object | `AccessDecision` | implemented |
| Policy Engine | 策略引擎 | Evaluates policies: Deny > Allow > Implicit Deny | Domain Service | `PolicyEngine` | implemented |
| Allow Policy Created Event | 允许策略已创建事件 | Allow Policy created | Domain Event | `AllowPolicyCreatedEvent` | implemented |
| Policy Attached Event | 策略已绑定事件 | Policy Binding created | Domain Event | `PolicyAttachedEvent` | implemented |
| Permission Created Event | 权限已创建事件 | Permission catalog entry created | Domain Event | `PermissionCreatedEvent` | implemented |
| Access Evaluated Event | 访问已评估事件 | Access Tuple evaluated to an Access Decision | Domain Event | `AccessEvaluatedEvent` | implemented |

---

## 8. Short-lived credentials | 临时凭证

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| Short-Lived Credential | 短期凭证 | Temporary JWT after role impersonation | Aggregate | `ShortLivedCredential` | implemented |
| Impersonation Policy | 模拟策略 | Who may create short-lived credentials for a Role | Concept | `ImpersonationPolicy` on `Role` | implemented |
| Create Short-Lived Credential | 创建短期凭证 | Exchange caller identity for role session credentials | Use Case | `ShortLivedCredentialService` | implemented |
| Session Expired | 会话已过期 | Whether credentials are no longer valid | Behavior | `ShortLivedCredential.isExpired` | implemented |
| Role Assumed Event | 角色已扮演事件 | Caller obtained short-lived credentials for a Role | Domain Event | `RoleAssumedEvent` | implemented |

---

## 9. Federation | 联邦与 SSO

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| OIDC Provider | OIDC 提供方 | Explore IAM as OIDC issuer | Container | Spring Authorization Server | implemented |
| OAuth Client | OAuth 客户端 | Domain aggregate for Relying Party registration | Aggregate | `OAuthClient` | implemented |
| Client ID | 客户端 ID | OAuth2 `client_id` value object | Value Object | `ClientId` | implemented |
| Public Client | 公共客户端 | Client authenticated with `none` only | Behavior | `OAuthClient.isPublicClient` | implemented |
| Relying Party | 依赖方 | Application trusting Explore IAM | Concept | — | — |
| Federated Identity | 联邦身份 | Maps external provider + subject to local user | Aggregate | `FederatedIdentity` | implemented |
| Create OAuth Client | 创建 OAuth 客户端 | Factory for confidential / public clients | Behavior | `OAuthClient.createOAuthClient` | implemented |
| Restore OAuth Client | 恢复 OAuth 客户端 | Rehydrate from persistence | Behavior | `OAuthClient.restoreOAuthClient` | implemented |
| SSO Login | 单点登录 | One login for multiple Relying Parties | Use Case | OIDC Authorization Code | implemented |
| OAuth Client Registered Event | OAuth 客户端已注册事件 | OAuth Client created | Domain Event | `OAuthClientRegisteredEvent` | implemented |
| OAuth Client Scopes Changed Event | OAuth 客户端范围已变更事件 | Allowed scopes replaced | Domain Event | `OAuthClientScopesChangedEvent` | implemented |

---

## 10. Audit Aggregates | 审计聚合

Immutable **aggregate roots** for append-only audit rows (Google Cloud Audit Logs style).

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| Admin Activity | 管理活动 | Management-plane action with actor, target, outcome | Aggregate | `AdminActivity` | implemented |
| Data Access Log | 数据访问日志 | Persisted Allow/Deny with reason code | Aggregate | `DataAccessLog` | implemented |
| Audit Actor | 审计主体 | Principal that performed a management action | Value Object | `AuditActor` | implemented |
| Audit Target | 审计目标 | Resource type and id affected | Value Object | `AuditTarget` | implemented |
| Audit Outcome | 审计结果 | Success or failure | Enum | `AuditOutcome` | implemented |
| Record Admin Activity | 记录管理活动 | Factory on `AdminActivity` | Behavior | `AdminActivity.recordAdminActivity` | implemented |
| Record Sign-In Activity | 记录登录活动 | Factory for sign-in admin activity | Behavior | `AdminActivity.recordSignInActivity` | implemented |
| Record Data Access Log | 记录数据访问 | Factory on `DataAccessLog` | Behavior | `DataAccessLog.recordDataAccessLog` | implemented |
| Admin Activity Recorder | 管理活动记录器 | Handles domain events from other modules and persists Admin Activity / Data Access Log rows; other modules never call it | Service | `AdminActivityRecorder.handleXxxEvent` | implemented |

---

## 10.1 Provisioning | 开通（SCIM）

IAM is the identity hub: external IdPs push to IAM; IAM pushes users to
downstream products. Protocol types stay in `com.iam.scim`; the
identity domain never imports SCIM classes.

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| Provisioning | 开通 | Creating, updating, deactivating, and deleting identities across systems | Concept | `com.iam.scim` | implemented |
| Provisioning Target | 开通目标 | Downstream SCIM Service Provider IAM pushes to | Aggregate | `ProvisioningTarget` | implemented |
| Assignment Group | 分配组 | Optional group limiting which users a Target receives | Attribute | `ProvisioningTarget.assignmentGroupId` | implemented |
| Provisioned Account | 已开通账号 | Mapping of IAM user id to the Target's remote id and last synced version | Entity | `ProvisionedAccount` | implemented |
| SCIM Provisioning Service | SCIM 开通服务 | Orchestrates inbound SCIM calls via `UserService` / `GroupService` | Service | `ScimProvisioningService` | implemented |
| SCIM Push Service | SCIM 推送服务 | Sends idempotent SCIM calls to a Provisioning Target | Service | `ScimPushService` | implemented |
| SCIM Push Listener | SCIM 推送监听器 | Turns identity domain events into pushes | Listener | `ScimPushListener` | implemented |
| SCIM Provisioner Client | SCIM 开通客户端 | OAuth client for external IdPs (`client_credentials`, `write:iam_scim`) | OAuth Client | `scim-provisioner` | implemented |
| SCIM Outbound Registration | SCIM 出站注册 | IAM's own OAuth client for pushes (`client_credentials`, `write:ai_scim`) | Client Registration | `iam-scim-outbound` | implemented |

---

## 11. Console | 控制台

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Status |
| ------------------------ | ---- | ---------- | ---- | ------------ | ------ |
| IAM Console | IAM 控制台 | Angular SPA for administrators | UI | Angular 22 app | partial |
| OAuth Client Registration | OAuth 客户端注册 | Create OAuth Client | Use Case | Console + `/api/v1/oauthClients` | implemented |

---

## 11.1 Module Scope Vocabulary | 模块范围词汇（GitHub-style）

Permission `oauthScope` / `code` uses GitHub OAuth scope shape
[`{access}:{resource}`](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/scopes-for-oauth-apps)
(`read:user`, `write:packages`, `admin:org`). Reject dotted product scopes
(`ai.chat`, `chat.admin`).

| Module | oauthScope | Relying Party use |
|--------|------------|-------------------|
| oidc | `openid`, `profile`, `email` | OIDC standard |
| ai | `write:ai_chat` | Explore AI chat / text / privacy |
| ai | `write:ai_audio` | ASR / TTS |
| ai | `write:ai_rag` | RAG |
| ai | `write:ai_media` | image / vision |
| ai | `write:ai_agent` | pipeline / workflow / automation / skill |
| ai | `write:ai_tools` | mcp / tools / eval |
| ai | `write:ai_scim` | IAM → Explore AI SCIM `/scim/v2/Users` |
| iam | `write:iam_scim` | External IdP → IAM SCIM `/scim/v2` |
| chat | `write:chat_messaging` | chats / messages / calls / media / notifications |
| chat | `write:chat_social` | posts / follow / groups / status / search |
| chat | `admin:chat` | Chat admin APIs |

---

## 12. Dev Tooling | 开发工具

| Preferred Term (English) | 中文 | Definition | Type | Code Mapping | Notes |
| ------------------------ | ---- | ---------- | ---- | ------------ | ----- |
| Orchestrator | 编排器 | Agent delegating to Subagents | Pattern | `.cursor/agents/orchestrator.md` | dev tooling |
| Subagent | 子智能体 | Specialized single-purpose Agent | Pattern | `.cursor/agents/*.md` | dev tooling |

---

## Reference

- [Guideline](Guideline.md) — IAM domain principles and related standards
- [C4 model](developer/c4-model/)
- [User Story Map](product-owner/User-Story-Map.md)
