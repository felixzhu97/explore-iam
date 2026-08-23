# Explore IAM

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-green.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-22-red.svg)](https://angular.dev/)

Explore IAM frees everyone to safely use any technology. Our mission is to connect the right people to the right apps at the right time.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Testing](#testing)
- [Documentation](#documentation)
- [AI-assisted development](#ai-assisted-development)
- [Deployment](#deployment)
- [License](#license)

## Features

| Area | Capability |
|------|------------|
| **Identity** | IAM User / Group / Role / federated principal |
| **Policy** | Identity-based and resource-based policies; Action / Resource / Condition; explicit Deny over Allow |
| **STS** | AssumeRole → temporary credentials for least-privilege sessions |
| **SSO** | OIDC (primary) / SAML (secondary) federation into Relying Parties; Angular form login SPA |
| **Audit** | Management events and authorization decision logs |
| **Multi-app** | Explore AI, WhatsFeed, Shopping System, Low Code Platform as resource accounts / OIDC clients |

Optional product modules (console UX depth, permission boundaries, organizations) are documented in the [User Story Map](docs/product-owner/User-Story-Map.md) and sketched in the [C4 model](docs/developer/c4-model/).

## Tech Stack

| Layer | Choice |
|-------|--------|
| Runtime | Java 25, Spring Boot 4.1 |
| Frontend | Angular 22 login SPA (`src/main/web`) |
| OIDC Provider | [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/getting-started.html) (`spring-boot-starter-oauth2-authorization-server`) |
| Federation | Google / GitHub into IAM (planned; US-09) |
| Persistence | Spring Data JPA + Liquibase; H2 locally (PostgreSQL target) |
| Ops | Spring Boot Actuator |
| Custom domain | Policy Engine, STS, AuthZ API (planned) |
| Diagrams | PlantUML + [C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML) |

Suggested Control Plane starters (BOM-managed; prefer Boot 4.1 `spring-boot-starter-security-oauth2-*` names if the BOM renames them):

```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
implementation("org.springframework.boot:spring-boot-starter-web")
implementation("org.springframework.boot:spring-boot-starter-validation")
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
implementation("org.springframework.boot:spring-boot-starter-liquibase")
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

Architecture: `controller → service → domain ← infra` per feature module (`com.iam.*`) — see [C4 model](docs/developer/c4-model/) and global [`architecture.mdc`](~/.cursor/rules/architecture.mdc).

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 25+ |
| Node.js | 20+ |
| pnpm | 8+ |
| PlantUML | latest (CLI or editor extension) |
| Git | latest |

Online alternative for diagrams: [PlantUML Online](https://www.plantuml.com/plantuml/uml/) — paste any `.puml` without local install.

## Getting Started

```bash
git clone https://github.com/felixzhu97/explore-iam.git
cd explore-iam
cp .env.example .env   # optional
pnpm install
pnpm build             # Angular → src/main/resources/static
./gradlew bootRun      # http://localhost:9100
```

Local Angular (proxies login POST / OAuth to `:9100`; avoids Explore AI on `:4200`):

```bash
pnpm start             # http://127.0.0.1:4201/login
```

OpenID discovery: `GET http://localhost:9100/.well-known/openid-configuration`

Demo form login (local): username `demo` / password `demo-password`.

Shared login SPA (same page for direct IAM login and OAuth authorize):

| Mode | URL | Notes |
|------|-----|--------|
| Direct | `http://localhost:9100/login` | IAM console-style sign-in |
| OAuth | `http://localhost:9100/login?client_id=explore-ai` | Shown after `/oauth2/authorize?...&client_id=explore-ai` when unauthenticated |

Relying Parties must start at `/oauth2/authorize` (standard OIDC). **Do not put `client_secret` in browser URLs** — the secret is used only on the token endpoint by the RP backend. Safe query params: `client_id` (and OIDC `state` / PKCE on the authorize URL).

Context API: `GET /api/login/context?client_id=explore-ai` → `{ clientId, clientName, oauth }`.

### App registration (US-10)

| | |
|--|--|
| UI | `http://localhost:9100/clients` list · `http://localhost:9100/clients/new` create wizard (login as `demo` first) |
| API | `POST /api/clients`, `GET /api/clients`, `GET /api/clients/{clientId}` (session auth; secret returned **once** on create for confidential clients) |

`POST` body accepts `clientName`, `redirectUris`, optional `postLogoutRedirectUris` / `clientUri` / `scopes` / `responseTypes` (`code`) / `authorizationGrantTypes` (`authorization_code` required, optional `refresh_token`) / `clientAuthenticationMethods` (`client_secret_basic` \| `client_secret_post` \| `none`).

OIDC clients are stored in `oauth2_registered_client` (not hardcoded). Local Explore AI is seeded from `app.oidc.seed-clients` in `application.yml` (env: `IAM_CLIENT_EXPLORE_AI_*`).

### Diagrams

```bash
ls docs/developer/c4-model/*.puml
# macOS: brew install plantuml && plantuml docs/developer/c4-model/*.puml
```

More detail: [docs/developer/c4-model/README.md](docs/developer/c4-model/README.md).

## Configuration

| Variable | Required | Purpose |
|----------|----------|---------|
| `OIDC_ISSUER` | No (default `http://localhost:9100`) | Issuer URL |
| `IAM_CLIENT_EXPLORE_AI_ID` | No (default `explore-ai`) | Explore AI client id |
| `IAM_CLIENT_EXPLORE_AI_SECRET` | No (dev default) | Explore AI client secret |
| `APP_DEMO_USER_*` | No | Seed local IAM User for form login |

Do not commit real secrets.

## Testing

```bash
./gradlew test
```

## Documentation

| Doc | Link |
|-----|------|
| C4 model | [docs/developer/c4-model/](docs/developer/c4-model/) |
| Glossary | [docs/Glossary.md](docs/Glossary.md) |
| User story map | [docs/product-owner/User-Story-Map.md](docs/product-owner/User-Story-Map.md) |
| System context (C1) | [docs/developer/c4-model/C1-Context.puml](docs/developer/c4-model/C1-Context.puml) |
| SSO dynamic diagram | [docs/developer/c4-model/C4-Dynamic-SSOLogin.puml](docs/developer/c4-model/C4-Dynamic-SSOLogin.puml) |
| Policy evaluation dynamic | [docs/developer/c4-model/C4-Dynamic-PolicyEvaluation.puml](docs/developer/c4-model/C4-Dynamic-PolicyEvaluation.puml) |
| Domain model (Code) | [docs/developer/c4-model/C4-Code-Domain-Model.puml](docs/developer/c4-model/C4-Code-Domain-Model.puml) |
| AWS IAM intro (reference) | [AWS IAM User Guide](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html) |

## AI-assisted development

Cursor / Claude Code conventions align with [explore-ai](https://github.com/felixzhu97/explore-ai): **no repo-local skill or rule copies** — use global paths below.

| Resource | Location |
|----------|----------|
| Rules | `~/.cursor/rules/` |
| Skills | `~/.cursor/skills/scrum-team/developers/EXPLORE_SKILLS.md` |
| Agents | [`.cursor/agents/`](.cursor/agents/) |
| Claude Code | Regenerate [`CLAUDE.md`](CLAUDE.md) with `./.claude/generate-rules.sh` after global rule changes |
| Delivery gates | Husky pre-commit (`pnpm typecheck`, `./gradlew checkstyleMain checkstyleTest`); [`.github/workflows/ci.yml`](.github/workflows/ci.yml) |

**Architecture note:** Source follows global `controller → service → domain ← infra` (`com.iam`) — see [C4 model](docs/developer/c4-model/) and [`architecture.mdc`](~/.cursor/rules/architecture.mdc).

## Deployment

| Target | Role |
|--------|------|
| Local | Single IAM Application on `:9100`, H2 file DB — see [C4-Deployment.puml](docs/developer/c4-model/C4-Deployment.puml) |
| Production (planned) | CDN static + replicated app tier, managed PostgreSQL, audit archive — same deployment diagram |

No live deployment is claimed for Explore IAM yet; diagrams describe the intended topology.

## License

[MIT](LICENSE) © 2026 Felix
