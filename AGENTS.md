# Explore IAM — agent guide

Reusable identity and access module: OpenID Connect provider, IAM User /
Group / Role, identity-based policy, STS AssumeRole sessions, federation,
SCIM provisioning and append-only audit. Java 25 + Spring Boot 4.1 +
Spring Authorization Server backend, Angular 22 login SPA, SwiftUI
hello-world app.

Start here, then follow links. This file is a map; detail lives in `docs/`.

## Map

| Path | What lives there |
|------|------------------|
| `src/main/java/com/iam/<feature>/` | Feature modules: `identity`, `policy`, `sts`, `federation`, `scim`, `audit` |
| `src/main/java/com/iam/common/` | Shared kernel (base entities, domain events, exceptions) |
| `src/main/resources/` | `application*.yml`, Liquibase changelog in `db/changelog/` |
| `src/test/java/com/iam/architecture/` | ArchUnit rules — the mechanical form of the hard rules below |
| `src/main/web/app/` | Angular login SPA and console pages (standalone components) |
| `src/main/ios/` | SwiftUI app |
| `docs/Glossary.md` | Ubiquitous language — name code after Preferred Terms |
| `docs/Guideline.md` | Trust boundaries and integration rules (start at "Best practices") |
| `docs/user-guide/` | Operator and relying-party setup |
| `docs/developer/c4-model/` | C4 views; domain model in `C4-Code-Domain-Model.puml` |
| `docs/developer/zero-trust/` | Zero Trust target architecture |
| `docs/product-owner/` | User Story Map and stories |

## Commands

| Need | Command |
|------|---------|
| Verify everything (run before every commit) | `pnpm verify` |
| Backend only: style, tests, ArchUnit | `pnpm verify:server` |
| Frontend only: types, lint, unit tests | `pnpm verify:web` |
| Docs only: relative Markdown links | `pnpm verify:docs` |
| Start backend + SPA in background, wait until healthy | `scripts/dev-up.sh` (`stop`, `status`) |
| Logs from `dev-up` | `logs/backend.log`, `logs/frontend.log` |
| Start backend (→ :9100) | `./gradlew bootRun` |
| Start SPA with live proxy (→ :4201) | `pnpm start` |

`bootRun` does not read `.env`. Export it first:
`set -a && source .env && set +a`. `APP_TOTP_ENCRYPT_PASSWORD` and
`APP_TOTP_ENCRYPT_SALT` are required. `verify` needs no keys, network or
running services.

## Hard rules (enforced by `verify`)

1. Per feature: `controller → service → domain ← infra`, with `mapper`
   between controller and service. Domain depends on no other layer.
2. Feature modules have no dependency cycles.
3. Only `audit` knows `audit`: other modules publish domain events.
4. No `usecase`, `adapter`, `port` packages; repository interfaces live
   in `domain/repository/`, implementations in `infra/`.
5. Never edit a Liquibase changeset that has shipped; add a new one.
   Changed ids or checksums break every existing database.
6. Never log passwords, tokens, client secrets, TOTP secrets, emails or
   IPs. Client secrets never travel in browser URLs.

When an ArchUnit rule fails, its message ends with `Fix:` — do that.

## Conventions

- Tests: `should <result> when <condition>` (`@DisplayName` with spaces;
  Java method names in camelCase).
- Domain language changes: update `docs/Glossary.md` and
  `C4-Code-Domain-Model.puml` before the code.
- Branch `<slug>` (no prefix); commit subject = PR title; body why +
  References; fine-grained Draft PRs via `gh stack`.
- UI follows Apple HIG; single accent `#0066cc`.

## Where to record work

- Multi-step plan: `docs/developer/exec-plans/` (see its README)
- Known debt: `docs/developer/tech-debt.md`
- Module health: `docs/developer/quality.md`

## Agents

Team agent definitions in `.cursor/agents/` are synced from the
`public` meta-repo — edit them there, not here.
