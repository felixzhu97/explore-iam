# Explore IAM

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-green.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-22-red.svg)](https://angular.dev/)

`explore-iam` is a reusable identity and access module. Use it so people and services can authenticate once, receive tokens through standard protocols, and obtain only the access a job needs.

The module issues and validates OpenID Connect tokens for relying parties, models lasting principals as IAM User, Group, and Role, evaluates identity-based policy, issues short-lived STS sessions after AssumeRole, and keeps management and authorization outcomes auditable. An Angular login SPA covers form sign-in and the OAuth authorize path.

`explore-iam` is built with Java 25, Spring Boot, Spring Authorization Server, Spring Security, Spring Data JPA, and Liquibase. A native SwiftUI hello-world app (**IAM**) lives under `src/main/ios`. Trust boundaries and integration rules live in the [Guideline](docs/Guideline.md).

## Get started

Follow the [User guide](docs/user-guide/README.md) for target operator and relying-party steps (best-practice integration; align this repo over time).

### Requirements

You need JDK 25+, Node.js 20+, pnpm 8+, and Git. PlantUML is optional if you want to regenerate C4 diagrams locally.

### Initial install

```bash
git clone https://github.com/felixzhu97/explore-iam.git
cd explore-iam
cp .env.example .env   # optional local overrides
pnpm install
pnpm build             # Angular → src/main/resources/static
./gradlew bootRun      # http://localhost:9100
```

OpenID Provider Configuration is published at:

```text
GET http://localhost:9100/.well-known/openid-configuration
```

### Sign in for the first time

With the default demo user enabled, open:

```text
http://localhost:9100/login
```

Sign in with username `demo` and password `demo-password`.

To work on the login SPA with a live proxy to the backend (port `4201`):

```bash
pnpm start
```

### Register an OIDC client

Relying parties start at `/oauth2/authorize`. Keep `client_secret` on the relying party’s back-channel token exchange—never in browser URLs. Safe query parameters on the authorize and login paths include `client_id`, plus OIDC `state` and PKCE material.

After signing in, register clients in the console or over the API:

| | |
|--|--|
| Console | `http://localhost:9100/clients` · `http://localhost:9100/clients/new` |
| API | `POST /api/v1/clients`, `GET /api/v1/clients`, `GET /api/v1/clients/{clientId}` |

`POST /api/v1/clients` accepts `clientName`, `redirectUris`, and optional `postLogoutRedirectUris`, `clientUri`, `scopes`, `responseTypes` (`code`), `authorizationGrantTypes` (`authorization_code`, optional `refresh_token`), and `clientAuthenticationMethods` (`client_secret_basic` \| `client_secret_post` \| `none`). For confidential clients, the secret is returned **once** on create.

Optional seed clients can be declared under `app.oidc.seed-clients` / matching env vars in `.env.example` for local development.

### Configuration

| Variable | Purpose |
|----------|---------|
| `SERVER_PORT` | HTTP port (default `9100`) |
| `OIDC_ISSUER` | Issuer URL (default `http://localhost:9100`) |
| `APP_DEMO_USER_*` | Local demo IAM User for form login |
| Seed client env vars | Optional bootstrap OIDC client entries |

Do not commit real secrets.

### Testing

```bash
./gradlew test
```

### Deploy

Production is a single Render Web Service (Docker): console SPA and OIDC API share one origin.

1. Connect the GitHub repo [`felixzhu97/explore-iam`](https://github.com/felixzhu97/explore-iam) with the Blueprint in [`render.yaml`](render.yaml) (Dashboard → New → Blueprint, or Render’s Blueprint docs).
2. After the first URL exists, set **`OIDC_ISSUER=https://<service>.onrender.com`** in the Render Dashboard (env `sync: false`), plus `APP_DEMO_USER_PASSWORD` and any seed client secrets you need, then redeploy.
3. Verify `GET /actuator/health` and OpenID discovery at `/.well-known/openid-configuration`, then open the console on the same host.

Plan is Render Starter (Oregon). Disk for H2 under `/app/data` is ephemeral on Starter—same cloud-minimal approach as explore-ai.

## Next steps

- Follow the [User guide](docs/user-guide/README.md), [Operator setup](docs/user-guide/operator-setup.md), and [Relying party integration](docs/user-guide/relying-party.md).
- Read the [Guideline](docs/Guideline.md) for IAM trust boundaries and Spring Security–related resources.
- Align terms with the [Glossary](docs/Glossary.md).
- Browse the [C4 model](docs/developer/c4-model/) for context, containers, and the domain model.
- See the [User Story Map](docs/product-owner/User-Story-Map.md) for product journeys.
- Open the native iOS skeleton: [`src/main/ios`](src/main/ios) (scheme **IAM**).
- Build and run diagrams from `docs/developer/c4-model/*.puml` when PlantUML is available.
- Review [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html) and [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html).

## Contributing

Contributions are welcome. Keep changes small and let CI stay green: [`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs Checkstyle and tests. Weekly GitHub Actions updates come from [Dependabot](.github/dependabot.yml).

## Project Status

Explore IAM is under active development. Behavior may change across minor versions until the module reaches a stable release line. Prefer the Guideline and Glossary as the source of truth for naming and trust boundaries while the surface area evolves.

## License

[MIT](LICENSE) © 2026 Felix
