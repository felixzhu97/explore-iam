# C4 模型文档

使用 PlantUML（C4-PlantUML）描述 **Explore IAM** 软件架构。`.puml` 为源文件；可选导出 `png/` 预览。

> **范围**：文档级架构（类 [AWS IAM](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html)）。本目录不包含可运行控制面源码。

## 布局

```text
docs/developer/c4-model/
├── README.md
├── C1-Context.puml
├── C2-Container.puml
├── C3-Component-ControlPlane.puml
├── C3-Component-Console.puml
├── C4-Sequence-SSOLogin.puml
├── C4-Sequence-PolicyEvaluation.puml
├── C4-Deployment.puml
└── C4-Deployment-Production.puml
```

## 文件

| 文件 | 类型 | 说明 |
|------|------|------|
| `C1-Context.puml` | 系统上下文 | 用户 / 管理员、Explore IAM、外部 IdP、四个业务 Relying Party |
| `C2-Container.puml` | 容器 | Console、Control Plane、Policy Engine、STS、OIDC Provider、DB / 审计 / Redis |
| `C3-Component-ControlPlane.puml` | 组件 | Identity / Policy / STS / AuthZ / Federation / Audit 与 Clean Architecture 边界 |
| `C3-Component-Console.puml` | 组件 | 身份、策略、应用注册、审计 UI 与 API 客户端 |
| `C4-Sequence-SSOLogin.puml` | 序列 | OIDC Authorization Code + 外部 IdP 联邦 |
| `C4-Sequence-PolicyEvaluation.puml` | 序列 | AssumeRole 与 Deny > Allow 策略求值 |
| `C4-Deployment.puml` | 部署 | 本地 Compose / 进程拓扑 |
| `C4-Deployment-Production.puml` | 部署 | 生产目标拓扑（计划，未宣称已上线） |

## 概念对照（AWS IAM ↔ Explore IAM）

| AWS IAM | Explore IAM（文档） |
|---------|---------------------|
| IAM User / Group / Role | 同一套 Principal 模型 |
| Identity / Resource policy | Policy Engine 求值对象 |
| STS AssumeRole | STS 容器 + 序列图 |
| IAM Identity Center / 联邦 | OIDC Provider + 外部 IdP |
| CloudTrail（管理/数据事件） | 审计存储 |

## 渲染

```bash
plantuml docs/developer/c4-model/*.puml
```

- 在线：[PlantUML Online](https://www.plantuml.com/plantuml/uml/)
- 编辑器：VS Code / Cursor PlantUML 扩展

## 技术栈摘要

| 层 | 技术 |
|------|------|
| Console | Angular 22、TypeScript、pnpm |
| Runtime | Java 25、Spring Boot 4.1 |
| OIDC Provider | Spring Authorization Server（`spring-boot-starter-oauth2-authorization-server`） |
| 联邦 | Spring Security OAuth2 Client → Google / GitHub |
| API 安全 | Spring Security + OAuth2 Resource Server |
| REST | Spring Web MVC + Validation |
| 持久化 | Spring Data JPA + Liquibase；PostgreSQL |
| 缓存 | Spring Data Redis（可选） |
| 自研领域 | Policy Engine、STS AssumeRole、鉴权决策 API（无对应 Spring starter） |
| 图 | PlantUML + C4-PlantUML |

参考：[Authorization Server Getting Started](https://docs.spring.io/spring-authorization-server/reference/getting-started.html)、[Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)。

## 源锚点（规划包）

文档设想的控制面分层（实现后应对齐）：

| 区域 | 规划路径 / 职责 |
|------|-----------------|
| Identity | 用户、组、角色、联邦主体 |
| Policy | 自定义 Policy Engine：策略文档、Action / Resource / Condition（无 Spring starter） |
| STS | 自定义 AssumeRole；可复用 OAuth2TokenGenerator / JWT |
| OIDC | Spring Authorization Server：授权码、令牌、JWKS |
| Console | 管理面 Angular SPA |
| Audit | 管理事件与鉴权决策 |

业务接入方见 C1：`explore-ai`、`whatsfeed`、`shopping-system`、`low-code-platform`。

## 查看方式

1. 阅读 C1 → C2 → C3，再看两条 C4 序列理解运行时
2. 对照项目根 [README](../../../README.md) 的 Features / Deployment
3. 以 `.puml` 为准；若存在 `png/`，改图后应重渲
