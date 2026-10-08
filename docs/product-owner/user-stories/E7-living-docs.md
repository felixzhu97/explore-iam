# E7 活文档与架构

← [用户故事地图](../User-Story-Map.md)

## 背景

跨仓协作时，领域术语、代码与 C4 领域图若不一致，会增加评审成本与实现偏差。需要可核对的活文档，使 Glossary、领域模型图与 public domain API 保持同一套统一语言。

---

## US-14 领域模型图与代码一致

**As a** 跨仓开发者  
**I want** C4 领域模型图与 Glossary、Java domain 公共 API 一一对应  
**So that** 评审与实现时能快速核对聚合、实体、值对象与领域行为

### 验收标准

1. **Scenario** 类型与 stereotype 一致
   **GIVEN** 代码中存在 Identity / Policy / STS / Federation / Audit 等领域类型  
   **WHEN** 打开 `C4-Code-Domain-Model.puml`  
   **THEN** 图中列出对应 Aggregate / Entity / ValueObject / Enum / DomainService  
   **AND** `PolicyStatement` 标注为 ValueObject，`Effect` 与 `AuditOutcome` 标注为 Enum

2. **Scenario** 公共领域方法可核对
   **GIVEN** 聚合根上存在语义化访问器与领域行为（如 `IamUser.disable`、`PolicyEngine.evaluate`）  
   **WHEN** 对照领域图与 Java 源码  
   **THEN** 图中以 UML `+methodName(params)` 列出 public 领域 API  
   **AND** 持久化专用 `reconstitute` 工厂不出现在图中

3. **Scenario** 关联关系反映实现
   **GIVEN** `PolicyAttachment` 通过 `policyId` 引用 `PolicyDocument`  
   **WHEN** 查看 Policy 包关联  
   **THEN** 图为引用关系而非聚合内导航  
   **AND** `PolicyDocument` 与 `PolicyStatement` 标注为 `document_json` 嵌入组合

### 状态

已实现

---

## US-15 C4 Dynamic 图统一黑白

**As a** 架构评审参与者  
**I want** 全部 `C4-Dynamic-*` 序列图使用同一套 plain 黑白样式  
**So that** 运行时路径图不与彩色 zinc 混用，阅读负担更低

### 验收标准

1. **Scenario** Dynamic 轨纯黑白
   **GIVEN** `C4-Dynamic-*.puml` 使用 `!theme plain` 与白底  
   **WHEN** 渲染任一 Dynamic 图  
   **THEN** 无彩色 stereotype 边框或彩色 role legend  
   **AND** README Dynamics 轨说明为 plain 黑白

2. **Scenario** 不再依赖 style-zinc
   **GIVEN** Dynamic 图已内联 plain 样式  
   **WHEN** 检查 c4-model 目录  
   **THEN** 无 `style-zinc.puml`  
   **AND** 无 Dynamic 图 `!include style-zinc.puml`

### 状态

已实现
