# User guide

Run an OIDC Authorization Server once. Point every relying party at the same
issuer. Prefer the smallest Spring configuration that works. Diagrams and steps
below are **target best practices**—independent of today’s package layout; align
code to them over time.

For design rules see the [Guideline](../Guideline.md). For terms see the
[Glossary](../Glossary.md).

## Goal

One issuer URL. Authorization Code with PKCE for browsers. `issuer-uri` on every
client and resource server. Client secrets only on the back channel.

```mermaid
flowchart LR
  RP[Relying_party]
  AS[Authorization_Server]
  RP -->|"issuer-uri"| AS
  RP -->|"RegisteredClient"| AS
```

## Get started

1. Stand up the Authorization Server with a single issuer (see
   [Operator setup](operator-setup.md)).
2. Confirm discovery:

```bash
curl -s "$ISSUER/.well-known/openid-configuration" | jq .issuer
```

3. Register a confidential client (YAML bootstrap or admin UI).
4. Wire a relying party with the same `issuer-uri` (see
   [Relying party integration](relying-party.md)).
5. Open a protected page → sign in → receive tokens on the server.

Use placeholders below. Replace `$ISSUER` with your real base URL (for local
dev, often `http://localhost:9000`).

## Useful endpoints

Relative to `$ISSUER`:

- Discovery: `/.well-known/openid-configuration`
- Authorize: `/oauth2/authorize`
- Token: `/oauth2/token`
- JWKS: `/oauth2/jwks`

## Next steps

### Run the provider

**Follow [Operator setup](operator-setup.md).** Set issuer, port, and a bootstrap
user once.

### Connect an app

**Follow [Relying party integration](relying-party.md).** One YAML block for
OAuth2 Client and Resource Server JWT.

## Related

[Operator setup](operator-setup.md)

[Relying party integration](relying-party.md)

[Guideline](../Guideline.md)

[Glossary](../Glossary.md)
