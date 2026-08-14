# E6 审计

← [用户故事地图](../User-Story-Map.md)

## 背景

合规需要可查询的管理事件与鉴权决策日志，支撑追责与排障。

---

## US-12 管理事件

**As a** 合规审计员  
**I want** 按时间与主体检索用户/角色/策略/客户端变更  
**So that** 我能还原谁在何时改了什么

### 验收标准

1. **Scenario** 列表与过滤
   **GIVEN** 系统中已有管理事件  
   **WHEN** 审计员按时间范围与主体过滤  
   **THEN** 返回匹配的 Management Event 列表  
   **AND** 每条含操作类型与目标资源标识

2. **Scenario** 无匹配时空结果
   **GIVEN** 过滤条件无命中记录  
   **WHEN** 执行查询  
   **THEN** 返回空列表而非错误  
   **AND** UI 提示无结果

### 状态

规划中

---

## US-13 鉴权决策查询

**As a** 合规审计员  
**I want** 查询 Authorization Decision 日志（Allow/Deny 与理由）  
**So that** 我能解释某次访问为何被拒绝或允许

### 验收标准

1. **Scenario** 按请求上下文检索
   **GIVEN** 已有鉴权决策日志  
   **WHEN** 按 Principal、Action 或时间查询  
   **THEN** 返回对应决策记录  
   **AND** 含 Allow/Deny 与理由码

2. **Scenario** 敏感字段受控
   **GIVEN** 审计员角色仅有只读审计权限  
   **WHEN** 查看决策详情  
   **THEN** 可读取决策结果与必要上下文  
   **AND** 不能修改或删除审计记录

### 状态

规划中
