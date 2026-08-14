# E1 身份

← [用户故事地图](../User-Story-Map.md)

## 背景

IAM 管理员需要维护 Principal 体系：IAM User、Group、Role，作为策略附加与 STS 扮演的基础。

---

## US-01 管理 IAM 用户

**As a** IAM 管理员  
**I want** 创建、查看、更新与停用 IAM User  
**So that** 人员与服务账号有可管理的长期身份

### 验收标准

1. **Scenario** 创建用户
   **GIVEN** 管理员已登录 IAM Console 且拥有用户管理权限  
   **WHEN** 管理员提交有效的用户创建表单  
   **THEN** 系统持久化新的 IAM User  
   **AND** 用户出现在身份列表中

2. **Scenario** 停用用户
   **GIVEN** 存在处于启用状态的 IAM User  
   **WHEN** 管理员执行停用  
   **THEN** 该用户无法再通过身份认证发起管理或 STS 操作  
   **AND** 停用结果可在审计中查到

### 状态

规划中

---

## US-02 管理用户组

**As a** IAM 管理员  
**I want** 创建 Group 并将用户加入或移出组  
**So that** 可以按组批量附加策略

### 验收标准

1. **Scenario** 创建组并加人
   **GIVEN** 至少存在一个 IAM User  
   **WHEN** 管理员创建 Group 并将该用户加入  
   **THEN** 组与成员关系被保存  
   **AND** 随后附加到组的策略适用于该成员

2. **Scenario** 移出组
   **GIVEN** 用户已在某 Group 中  
   **WHEN** 管理员将该用户移出  
   **THEN** 用户不再继承该组策略  
   **AND** 列表反映最新成员

### 状态

规划中

---

## US-03 管理角色

**As a** IAM 管理员  
**I want** 创建 Role 并配置信任策略  
**So that** 调用方可通过 AssumeRole 获得临时身份

### 验收标准

1. **Scenario** 创建角色
   **GIVEN** 管理员拥有角色管理权限  
   **WHEN** 管理员创建 Role 并设置信任策略主体  
   **THEN** Role 可被查询  
   **AND** 信任策略可供 STS 求值

2. **Scenario** 更新信任策略
   **GIVEN** Role 已存在  
   **WHEN** 管理员修改信任策略并保存  
   **THEN** 后续 AssumeRole 使用新信任策略  
   **AND** 变更写入管理审计

### 状态

规划中
