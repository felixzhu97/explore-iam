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

## US-15 C4 图视觉风格跨仓一致

**As a** 架构评审参与者  
**I want** Explore IAM 与 Explore AI 的 C4 Code / Dynamic 图使用同一套 zinc 视觉规范  
**So that** 跨项目阅读领域图与序列图时认知负担更低

### 验收标准

1. **Scenario** 白底彩色边框样式
   **GIVEN** `style-zinc.puml` 被 Code 与 Dynamic 图引用  
   **WHEN** 渲染 `C4-Code-Domain-Model.puml` 或 `C4-Dynamic-*.puml`  
   **THEN** 类/参与者为白底  
   **AND** stereotype 通过彩色标题与边框区分（AggregateRoot / Entity / ValueObject 等）

2. **Scenario** 图例与 explore-ai 对齐
   **GIVEN** 领域模型图包含 IAM 彩色类型图例  
   **WHEN** 与 explore-ai 同目录 `style-zinc.puml` 对比  
   **THEN** skinparam 令牌与 stereotype 色板一致  
   **AND** README 说明「白底 + 彩色边框/标题」视觉轨道

### 状态

已实现
