# Quality

Snapshot of how well each area is protected. Update the row when a PR
changes tests, rules or docs for that area.

Grades: **A** tests + rules + docs current · **B** one gap · **C** two or more gaps.

| Area | Unit tests | Architecture rules | Docs | Grade |
|------|-----------|--------------------|------|-------|
| Audit | JUnit | audit reached only through events | Guideline | A |
| Backend layering | — | ArchUnit: no port/adapter packages, no feature cycles, audit via events | `AGENTS.md`, C4 | B |
| Identity / MFA | JUnit | covered by layer rules | Glossary, Guideline, C4 | A |
| Policy | JUnit (2 classes) | covered by layer rules | Glossary, Guideline, C4 | A |
| STS | — | covered by layer rules | Glossary, Guideline, C4 | B |
| Federation / SCIM | JUnit | covered by layer rules | Guideline, user guide | A |
| Login SPA | — | — | README | C |
| iOS app | XCTest | — | README | C |
