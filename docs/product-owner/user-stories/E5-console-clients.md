# E5 控制台与客户端

← [用户故事地图](../User-Story-Map.md)

## 背景

应用开发者在 Console 注册 Relying Party；IAM 管理员通过 Angular 管理面运维身份与策略。

---

## US-10 注册 Relying Party

**As a** 应用开发者  
**I want** 注册 OIDC 客户端（client_id、redirect_uri、scopes）  
**So that** 我的应用可以作为 Relying Party 接入 Explore IAM

### 验收标准

1. **Scenario** 创建 Registered Client
   **GIVEN** 开发者已登录 Console 且拥有客户端管理权限  
   **WHEN** 提交合法的回调 URI 与 scopes  
   **THEN** 系统创建 Registered Client 并返回 client 凭证（按安全策略展示一次）  
   **AND** 客户端可出现在列表中

2. **Scenario** 非法回调被拒绝
   **GIVEN** 开发者提交不安全或格式错误的 redirect_uri  
   **WHEN** 保存客户端  
   **THEN** 请求失败并提示校验错误  
   **AND** 不创建客户端记录

### 状态

规划中

---

## US-11 Console 管理面

**As a** IAM 管理员  
**I want** 在统一 Console 中导航身份、策略、应用与审计  
**So that** 我可以完成日常运维而无需多个工具

### 验收标准

1. **Scenario** 主导航可达
   **GIVEN** 管理员已登录 Console  
   **WHEN** 打开身份、策略、应用注册、审计入口  
   **THEN** 各页面可加载  
   **AND** 未授权入口被隐藏或拒绝

2. **Scenario** 未登录被重定向
   **GIVEN** 管理员未认证  
   **WHEN** 访问受保护的 Console 路由  
   **THEN** 重定向至 Explore IAM 登录  
   **AND** 登录成功后回到原目标（若支持）

### 状态

规划中
