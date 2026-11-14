# Tech debt

Append a row when you take a shortcut; the PR that pays it removes the row.

| Item | Impact | Proposed fix | Added |
|------|--------|--------------|-------|
| `docs/Guideline.md` (623 lines) has no index | Agents read all of it or miss the rule | Link sections from `AGENTS.md` only where a rule is enforced | 2026-11-14 |
| SPA has no lint and no unit tests | UI regressions surface only in manual checks | Add angular-eslint and Vitest to `verify:web` | 2026-11-14 |
