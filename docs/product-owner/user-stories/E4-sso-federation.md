# E4 SSO 与联邦

← [用户故事地图](../User-Story-Map.md)

## 背景

Explore IAM 作为 OIDC Provider，支持业务应用 SSO，并可通过 OAuth2 Client 联邦 Google / GitHub。

---

## US-08 OIDC 登录业务应用

**As a** 终端用户  
**I want** 从业务应用跳转到 Explore IAM 完成 OIDC 登录后再回到应用  
**So that** 我可以用统一身份使用多个 Explore 产品

### 验收标准

1. **Scenario** 授权码回流
   **GIVEN** 用户访问受保护的 Relying Party 且尚未登录  
   **WHEN** 应用重定向至 Explore IAM `/authorize` 且用户完成认证  
   **THEN** 浏览器带回 authorization code  
   **AND** 应用可换取 ID Token / Access Token

2. **Scenario** 已有 IAM 会话免重复登录
   **GIVEN** 用户已在 Explore IAM 持有有效会话  
   **WHEN** 另一 Relying Party 发起 OIDC 登录  
   **THEN** 用户无需再次输入上游凭证（同会话策略允许时）  
   **AND** 仍按客户端完成同意（如需要）

### 状态

已实现（本地 OIDC Provider + Explore AI Registered Client；表单登录；Jira [EXP-351](https://felixzhu.atlassian.net/browse/EXP-351)）

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
