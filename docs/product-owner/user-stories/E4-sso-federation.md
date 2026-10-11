# E4 SSO 与联邦

← [用户故事地图](../User-Story-Map.md)

## 背景

Explore IAM 作为 OIDC Provider，支持业务应用 SSO，并可通过 OAuth2 Client 联邦 Google / GitHub。完整会话要求密码后再完成第二因素（OTT、Passkey 或 TOTP）。

---

## US-08 OIDC 登录业务应用

**As a** 终端用户  
**I want** 从业务应用跳转到 Explore IAM 完成 OIDC 登录后再回到应用  
**So that** 我可以用统一身份使用多个 Explore 产品

### 验收标准

1. **Scenario** 授权码回流
   **GIVEN** 用户访问受保护的 Relying Party 且尚未登录  
   **WHEN** 应用重定向至 Explore IAM `/authorize` 且用户完成认证（含 [US-16](#us-16-mfa-第二因素后获得完整会话) 要求的第二因素）  
   **THEN** 浏览器带回 authorization code  
   **AND** 应用可换取 ID Token / Access Token

2. **Scenario** 已有 IAM 会话免重复登录
   **GIVEN** 用户已在 Explore IAM 持有有效完整会话  
   **WHEN** 另一 Relying Party 发起 OIDC 登录  
   **THEN** 用户无需再次输入上游凭证（同会话策略允许时）  
   **AND** 仍按客户端完成同意（如需要）

### 状态

已实现（本地 OIDC Provider + Explore AI Registered Client；表单登录 + MFA；Jira [EXP-351](https://felixzhu.atlassian.net/browse/EXP-351)）

---

## US-09 联邦 Google/GitHub

**As a** 终端用户  
**I want** 使用 Google 或 GitHub 完成联邦登录  
**So that** 我不必单独维护一套 IAM 密码

### 验收标准

1. **Scenario** Google 联邦成功
   **GIVEN** 管理员已配置 Google 作为外部 IdP  
   **WHEN** 用户选择 Google 并完成上游认证  
   **THEN** Explore IAM 映射或创建 Federated Principal  
   **AND** 继续完成 OIDC 对 Relying Party 的签发

2. **Scenario** 未配置的提供方不可用
   **GIVEN** 某外部 IdP 未启用  
   **WHEN** 用户打开登录页  
   **THEN** 不展示该提供方入口  
   **AND** 其他已启用提供方仍可用

### 状态

规划中

---

## US-16 MFA 第二因素后获得完整会话

**As a** 终端用户  
**I want** 在密码登录后完成可用的第二因素  
**So that** 只有我本人能获得可签发 OIDC 的完整会话

### 验收标准

1. **Scenario** 密码后进入验证引导
   **GIVEN** 用户在 `/login` 提交正确密码且尚无完整 MFA 会话  
   **WHEN** 认证成功处理器处理登录  
   **THEN** 浏览器进入 `/login/verify`  
   **AND** 页面根据 `/api/login/mfa-options` 展示可用因素（OTT、已登记 Passkey、已启用 TOTP）

2. **Scenario** anyOf 第二因素成功
   **GIVEN** 用户已持有 `FACTOR_PASSWORD`  
   **WHEN** 用户完成 OTT、WebAuthn 断言或 TOTP（含备用码）任一验证  
   **THEN** 会话获得对应第二因素 authority  
   **AND** 可继续 OIDC authorize / 受保护资源（与 [US-08](#us-08-oidc-登录业务应用) 衔接）

3. **Scenario** 仅密码不可作为完整会话
   **GIVEN** 用户只完成密码因素  
   **WHEN** 访问需要完整会话的端点  
   **THEN** 被引导或拒绝直至第二因素完成

### 状态

已实现（`FACTOR_PASSWORD` + anyOf `FACTOR_OTT` / `FACTOR_WEBAUTHN` / `FACTOR_TOTP`）

---

## US-17 登记 Passkey 或 TOTP

**As a** 终端用户  
**I want** 为自己登记 Passkey 和/或认证器 App（TOTP）  
**So that** 下次登录可以用钓鱼抵抗或离线第二因素

### 验收标准

1. **Scenario** 登记 TOTP
   **GIVEN** 用户已登录并可访问安全设置  
   **WHEN** 在 `/security/totp` 开始登记并扫描 QR / 输入密钥后提交确认码  
   **THEN** TOTP Credential 启用  
   **AND** `/api/login/mfa-options` 将 TOTP 标为可用

2. **Scenario** 登记 Passkey
   **GIVEN** 用户已登录且浏览器支持 WebAuthn  
   **WHEN** 完成 Passkey 注册仪式  
   **THEN** WebAuthn Credential 持久化  
   **AND** 验证页可选择 Passkey 作为第二因素

### 状态

已实现（`/api/v1/totp` 登记 + WebAuthn JDBC；验证页引导）
