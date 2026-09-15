# C4 Model

PlantUML ([C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML)) living architecture for **Explore IAM**.  
**Source of truth:** `.puml` files in this directory. Regenerate PNG when PlantUML is available.

Official C4: [c4model.com](https://c4model.com/).  
Style standards: global [c4-model](~/.cursor/skills/scrum-team/developers/developer/references/c4-model.md) skill.

## Visual tracks (do not mix)

| Track | Files | Style |
|-------|-------|-------|
| **Structural C4** | C1–C3, Deployment | `C4_blue_new` theme (wireframe) |
| **Domain model** | `C4-Code-Domain-Model.puml` | 白底黑字黑边框（对齐 explore-chat Code 图） |
| **Dynamics** | `C4-Dynamic-*` | [`style-zinc.puml`](style-zinc.puml) 或内联白底（见各文件） |

## File set

| File | Level | Description |
|------|-------|-------------|
| `C1-Context.puml` | Context | People, Explore IAM boundary, Relying Parties, external IdPs |
| `C2-Container.puml` | Container | IAM Application monolith (:9100), metadata database |
| `C3-Component.puml` | Component | **Single** diagram: Angular Console + backend modules (`controller → service → domain ← infra`) |
| `C4-Code-Domain-Model.puml` | Code | DDD class model with `AbstractEntity` / `AbstractImmutable` kernel |
| `C4-Deployment.puml` | Deployment | **Single** view: local H2 + Render Starter (Docker) |
| `C4-Dynamic-SSOLogin.puml` | Dynamic | OIDC Authorization Code (confidential + public PKCE) |
| `C4-Dynamic-NativePkceLogin.puml` | Dynamic | Native iOS ASWebAuthenticationSession + PKCE → RP Bearer |
| `C4-Dynamic-PolicyEvaluation.puml` | Dynamic | AssumeRole + PolicyEngine (Deny > Allow > implicit Deny) |
| `style-zinc.puml` | Shared | Dynamic 序列图可选样式（Code 领域模型内联白底黑边框） |

## Stack & ports

| Item | Value |
|------|-------|
| Runtime | Java 25, Spring Boot 4.1 |
| Console | Angular 22 (bundled in `src/main/resources/static`) |
| OIDC | Spring Authorization Server (`/oauth2/*`, `/.well-known/*`) |
| Local URL | `http://localhost:9100` |
| Local DB | H2 file `./data/explore-iam` (Liquibase `0.1.xml`) |
| Target DB | PostgreSQL |

## Render

```bash
plantuml -tpng docs/developer/c4-model/*.puml
```

Online: [PlantUML server](https://www.plantuml.com/plantuml/uml/).

## Reading order

1. C1 → C2 → C3 (structure)
2. `C4-Code-Domain-Model.puml` (ubiquitous language)
3. `C4-Dynamic-*` (runtime paths)
4. `C4-Deployment.puml` (where it runs)

## AWS IAM mapping

| AWS IAM | Explore IAM |
|---------|-------------|
| IAM User / Group / Role | `IamUser`, `Group`, `Role` |
| Identity / Resource policy | `PolicyDocument`, `PolicyEngine` |
| STS AssumeRole | `AssumeRoleService`, `AssumedRoleSession` |
| IAM Identity Center / federation | OIDC Provider + `FederatedIdentityLink` |
| CloudTrail | `ManagementEvent`, `AuthorizationDecisionLog` |

## Related docs

- [Glossary](../../Glossary.md) — Preferred Terms (English)
- [README](../../../README.md) — features, getting started
- [User Story Map](../../product-owner/User-Story-Map.md)
