# Contributing

Thanks for your interest in Explore IAM.

## Development

Requires Node.js ≥ 20 and [pnpm](https://pnpm.io/).

```bash
pnpm install
pnpm start
```

Useful checks:

```bash
pnpm typecheck
pnpm build
```

CI also runs Checkstyle and tests via [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

## Pull requests

1. Branch from `main` (or the active stack tip for dependent work).
2. Keep changes focused; prefer the Guideline and Glossary as the source of
   truth for naming and trust boundaries.
3. Ensure `pnpm typecheck` and `pnpm build` pass before opening a PR.
4. Open a PR against `main` with a clear description of why the change is
   needed.

## Code of Conduct

Participation is governed by our [Code of Conduct](CODE_OF_CONDUCT.md).
