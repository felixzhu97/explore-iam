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
- [Deployment](#deployment)
- [License](#license)

## Features

| Area | Capability |
|------|------------|
| **Identity** | IAM User / Group / Role / federated principal |
| **Policy** | Identity-based and resource-based policies; Action / Resource / Condition; explicit Deny over Allow |
| **STS** | AssumeRole → temporary credentials for least-privilege sessions |
| **SSO** | OIDC (primary) / SAML (secondary) federation into Relying Parties |
| **Audit** | Management events and authorization decision logs |
| **Multi-app** | Explore AI, WhatsFeed, Shopping System, Low Code Platform as resource accounts / OIDC clients |

Optional product modules (console UX depth, permission boundaries, organizations) are documented in the [User Story Map](docs/product-owner/User-Story-Map.md) and sketched in the [C4 model](docs/developer/c4-model/).

## Tech Stack

| Layer | Choice |
|-------|--------|
| Runtime | Java 25, Spring Boot 4.1 |
| Frontend | Angular 22 Console (planned; not in this slice) |
| OIDC Provider | [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/getting-started.html) (`spring-boot-starter-oauth2-authorization-server`) |
| Federation | Spring Security OAuth2 Client (planned for Google / GitHub into IAM) |
| Persistence | Spring Data JPA + Liquibase; H2 locally (PostgreSQL target) |
| Ops | Spring Boot Actuator |
| Custom domain | Policy Engine, STS, AuthZ API (planned) |
| Diagrams | PlantUML + [C4-PlantUML](https://github.com/plantuml-stdlib/C4-PlantUML) |

Suggested Control Plane starters (BOM-managed; prefer Boot 4.1 `spring-boot-starter-security-oauth2-*` names if the BOM renames them):

```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
implementation("org.springframework.boot:spring-boot-starter-web")
implementation("org.springframework.boot:spring-boot-starter-validation")
implementation("org.springframework.boot:spring-boot-starter-data-jpa")
implementation("org.springframework.boot:spring-boot-starter-liquibase")
implementation("org.springframework.boot:spring-boot-starter-data-redis") // optional
implementation("org.springframework.boot:spring-boot-starter-actuator")
```

Architecture target: `web → application → domain ← infrastructure` for the control plane (see C3). **Not implemented as application source in this catalog.** See also [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html).

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
./gradlew bootRun      # http://localhost:9100
```

OpenID discovery: `GET http://localhost:9100/.well-known/openid-configuration`

Demo form login (local): username `demo` / password `demo-password`.

Registered Relying Party for Explore AI: client id `explore-ai` (see `.env.example`).

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
| SSO sequence | [docs/developer/c4-model/C4-Sequence-SSOLogin.puml](docs/developer/c4-model/C4-Sequence-SSOLogin.puml) |
| Policy evaluation | [docs/developer/c4-model/C4-Sequence-PolicyEvaluation.puml](docs/developer/c4-model/C4-Sequence-PolicyEvaluation.puml) |
| AWS IAM intro (reference) | [AWS IAM User Guide](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html) |

## Deployment

| Target | Role |
|--------|------|
| Local | Console + Control Plane + Postgres (+ optional Redis) — see [C4-Deployment.puml](docs/developer/c4-model/C4-Deployment.puml) |
| Production (planned) | Separated Console / API / OIDC endpoints, managed Postgres, audit retention — see [C4-Deployment-Production.puml](docs/developer/c4-model/C4-Deployment-Production.puml) |

No live deployment is claimed for Explore IAM yet; diagrams describe the intended topology.

## License

[MIT](LICENSE) © 2026 Felix
