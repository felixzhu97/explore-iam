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
| **Dynamics** | `C4-Dynamic-*` | Plain 黑白（`!theme plain` + 白底，无彩色 legend） |

## File set

| File | Level | Description |
|------|-------|-------------|
| `C1-Context.puml` | Context | People, Explore IAM boundary, Relying Parties, external IdPs |
| `C2-Container.puml` | Container | IAM Application monolith (:9100), metadata database |
| `C3-Component.puml` | Component | **Single** diagram: Angular Console + backend modules (`controller → service → domain ← infra`) |
| `C4-Code-Domain-Model.puml` | Code | DDD class model grouped by business domain (Domain Kernel, Common, Identity, Policy, Short-lived credentials, Federation, Audit) |
| `C4-Deployment.puml` | Deployment | **Single** view: local H2 + Render Starter (Docker) |
| `C4-Dynamic-SSOLogin.puml` | Dynamic | OIDC Authorization Code (confidential + public PKCE) |
| `C4-Dynamic-NativePkceLogin.puml` | Dynamic | Native iOS ASWebAuthenticationSession + PKCE → RP Bearer |
| `C4-Dynamic-PolicyEvaluation.puml` | Dynamic | Short-lived credentials + PolicyEngine (Deny > Allow > implicit Deny) |

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
cd docs/developer/c4-model
PLANTUML_LIMIT_SIZE=16384 plantuml -tpng -o png C4-Code-Domain-Model.puml
```

Online: [PlantUML server](https://www.plantuml.com/plantuml/uml/).

## Reading order

1. C1 → C2 → C3 (structure)
2. `C4-Code-Domain-Model.puml` (ubiquitous language)
3. `C4-Dynamic-*` (runtime paths)
4. `C4-Deployment.puml` (where it runs)

## Google Cloud IAM mapping

Vocabulary follows [Google Cloud IAM](https://cloud.google.com/iam/docs/overview).

| Google Cloud IAM | Explore IAM |
|------------------|-------------|
| Principal (user, group) | `User`, `Group` |
| Role / role binding | `Role`, `RoleBinding` |
| Allow policy | `AllowPolicy`, `PolicyStatement`, `PolicyEngine` |
| Policy binding | `PolicyBinding` |
| Permission | `Permission` (common value object and policy catalog entry) |
| Resource name | `ResourceName` |
| Short-lived credentials | `ShortLivedCredential` |
| Workforce / workload identity federation | OIDC Provider + `FederatedIdentity` |
| OAuth client | `OAuthClient` |
| Cloud Audit Logs (Admin Activity, Data Access) | `AdminActivity`, `DataAccessLog` |

## Related docs

- [Glossary](../../Glossary.md) — Preferred Terms (English)
- [README](../../../README.md) — features, getting started
- [User Story Map](../../product-owner/User-Story-Map.md)
