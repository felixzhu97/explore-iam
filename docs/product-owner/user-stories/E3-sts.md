# E3 STS

← [用户故事地图](../User-Story-Map.md)

## 背景

STS 通过 AssumeRole 发放 Temporary Credentials，支持最小权限短时会话。

---

## US-06 AssumeRole

**As a** 应用开发者  
**I want** 调用 AssumeRole 并在信任策略允许时获得临时凭证  
**So that** 我可以以 Role 身份访问受保护资源

### 验收标准

1. **Scenario** 信任允许时签发
   **GIVEN** 调用方被 Role 的信任策略允许  
   **WHEN** 调用 `AssumeRole` 并提供合法 roleArn 与 sessionName  
   **THEN** 返回 Temporary Credentials 与过期时间  
   **AND** 审计记录一次成功扮演

2. **Scenario** 信任拒绝时失败
   **GIVEN** 调用方不在信任策略允许范围内  
   **WHEN** 调用 `AssumeRole`  
   **THEN** 请求被拒绝  
   **AND** 不签发任何临时凭证

### 状态

规划中

---

## US-07 临时凭证过期

**As a** 应用开发者  
**I want** 过期临时凭证被拒绝  
**So that** 会话不会无限期保持权限

### 验收标准

1. **Scenario** 过期后调用失败
   **GIVEN** 临时凭证已超过过期时间  
   **WHEN** 使用该凭证调用受保护 API  
   **THEN** 认证失败  
   **AND** 提示凭证无效或已过期

2. **Scenario** 未过期仍可用
   **GIVEN** 临时凭证仍在有效期内且策略允许  
   **WHEN** 使用该凭证调用受保护 API  
   **THEN** 请求可进入鉴权求值  
   **AND** 不因凭证生命周期被拒

### 状态

规划中
