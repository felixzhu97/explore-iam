# E2 策略

← [用户故事地图](../User-Story-Map.md)

## 背景

策略文档定义 Action / Resource / Condition；求值遵循显式 Deny 优先于 Allow，与 AWS IAM 对齐。

---

## US-04 编写并附加策略

**As a** IAM 管理员  
**I want** 创建 Policy Document 并将其附加到 User、Group 或 Role  
**So that** 主体获得可声明的权限集

### 验收标准

1. **Scenario** 创建策略文档
   **GIVEN** 管理员在策略编辑页  
   **WHEN** 提交包含 Effect、Action、Resource 的有效策略  
   **THEN** Policy Document 被保存  
   **AND** 可在策略列表中打开

2. **Scenario** 附加到角色
   **GIVEN** 已有 Policy Document 与 Role  
   **WHEN** 管理员将策略附加到该 Role  
   **THEN** 身份策略关联建立  
   **AND** 后续鉴权会加载该策略

### 状态

规划中

---

## US-05 策略求值 Deny 优先于 Allow

**As a** 应用开发者  
**I want** 鉴权决策对同一请求先处理显式 Deny  
**So that** 收紧策略时不会被宽泛 Allow 覆盖

### 验收标准

1. **Scenario** 显式 Deny 生效
   **GIVEN** 主体同时拥有允许与拒绝同一 Action 的策略语句  
   **WHEN** 调用鉴权决策 API  
   **THEN** 结果为 Deny  
   **AND** 理由码标明显式拒绝

2. **Scenario** 仅 Allow 时通过
   **GIVEN** 主体仅有匹配的 Allow 且无 Deny  
   **WHEN** 调用鉴权决策 API  
   **THEN** 结果为 Allow

### 状态

规划中

---

## US-05a 权限点目录（GitHub 风格 Scope）

**As a** IAM 管理员  
**I want** 维护 Permission Point 目录，每个权限点对应一个 GitHub 风格 OAuth scope  
**So that** Explore AI / Chat 等依赖方可用统一的 `write:ai_chat` / `admin:chat` 做 JWT 鉴权

### 验收标准

1. **Scenario** 列出权限点
   **GIVEN** 管理员或审计员已登录  
   **WHEN** 调用 `GET /api/v1/permissionPoints`  
   **THEN** 返回含 `write:ai_chat`、`admin:chat` 等目录项  
   **AND** 每项含 Action、Resource、module

2. **Scenario** 拒绝点分产品 Scope
   **GIVEN** 管理员尝试创建 `oauthScope` 为 `ai.chat` 的权限点  
   **WHEN** 提交创建请求  
   **THEN** 请求被拒绝  
   **AND** 提示须使用 `{access}:{resource}` 格式

### 状态

规划中
