# Guideline

Explore IAM connects the right people to the right apps. Administrators maintain identity and policy; relying parties obtain tokens through standard sign-in; auditors can reconstruct who did what and when.

## Protocols and authorization platform

**Use Explore IAM as the OIDC provider for relying parties.**

Business apps should trust identity and access tokens issued by this platform instead of inventing their own login protocols. See [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html) and [OpenID Connect Core](https://openid.net/specs/openid-connect-core-1_0.html).

**Converge authorization flows on OAuth 2.1 and the security BCP.**

Require PKCE for every client that uses the authorization code flow; match redirect URIs exactly; do not offer Implicit or Resource Owner Password Credentials; do not put Bearer tokens in query strings. See [OAuth 2.1](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1), [RFC 9700](https://datatracker.ietf.org/doc/html/rfc9700), and [RFC 7636](https://datatracker.ietf.org/doc/html/rfc7636).

**Prefer Authorization Code with PKCE for browser clients.**

When people sign in from a browser, the code flow keeps secrets on the server; PKCE reduces the risk of stolen authorization codes for public clients. See [RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749) and [OAuth for Native Apps](https://datatracker.ietf.org/doc/html/rfc8252).

**Keep client_secret out of the browser.**

Secrets belong only in the relying party’s back-channel token exchange. The front end may use `client_id`, `state`, and PKCE material—never the client secret. See [RFC 6750](https://datatracker.ietf.org/doc/html/rfc6750).

**Publish a verifiable issuer through discovery and JWKS.**

Relying parties should validate issuer and signatures from OpenID Provider Configuration and JWKS instead of hard-coding keys. See [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) and [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517).

**Raise sender-constrained profiles for high-assurance APIs.**

For finance-grade or cross-boundary APIs, build on the baseline with PAR, DPoP or mTLS, and the FAPI 2.0 Security Profile when required. See [FAPI 2.0 Security Profile](https://openid.net/specs/fapi-2_0-security-profile.html), [RFC 9449](https://datatracker.ietf.org/doc/html/rfc9449), and [RFC 9126](https://datatracker.ietf.org/doc/html/rfc9126).

## Identity

**Treat IAM User as the long-lived principal for form login.**

People and service accounts need a manageable, stable identity. Sessions may expire; the user record itself must remain auditable and disableable. See [AWS IAM User Guide](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html).

**Disable the account when access must stop immediately.**

After disable, the principal must not authenticate for management or STS. Record the disable outcome in audit.

**Use Group for shared policy and Role for assumable identity.**

Membership fits bulk authorization; roles fit short, revocable duty changes. Do not bake temporary tasks into everyone’s lasting permissions.

**Match authentication strength to assurance level.**

Passwords fit only risk-aligned scenarios; high-value actions should step up to multi-factor or phishing-resistant authentication. See [NIST SP 800-63B](https://pages.nist.gov/800-63-4/sp800-63b.html) and [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html).

**Plan a phishing-resistant sign-in path.**

Passkeys / WebAuthn reduce shared-secret and phishing risk. Introduce them alongside form login rather than cutting off existing users overnight. See [W3C Web Authentication](https://www.w3.org/TR/webauthn-3/) and [FIDO2](https://fidoalliance.org/fido2/).

**Store passwords only as one-way hashes.**

Never keep credentials in plaintext or reversible form; verify by comparing hashes. See [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

## Policy

**Evaluate Deny before Allow, then implicit Deny.**

An explicit deny must override allow; with no match, deny by default. See [AWS IAM policy evaluation logic](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_evaluation-logic.html).

**Attach policies to principals by stable ARN, not ad-hoc strings.**

Attachments should point at identifiable User, Group, or Role identities. Loose text breaks under rename and migration and makes audit hard to replay.

**Grant only the permissions a task needs.**

Console and API roles should map to observable jobs, not open-by-default access. Keep verifying who can do what instead of trusting a single login forever. See [NIST SP 800-207 Zero Trust Architecture](https://csrc.nist.gov/pubs/sp/800/207/final).

## STS

**Issue temporary credentials only after a successful AssumeRole.**

Callers exchange an existing identity for a short session; the trust policy decides who may assume the Role. Do not mint tokens without that check. See [AWS STS AssumeRole](https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html).

**Reject expired sessions; do not renew them quietly in the background.**

Temporary credentials are valuable because the boundary is clear: when they expire, callers must assume again or fall back to narrower rights.

**Make audience and purpose of temporary tokens verifiable.**

When issuing and validating JWTs, check `iss`, `aud`, `exp`, and the signing algorithm; reject `none` and weak algorithms. See [RFC 7519](https://datatracker.ietf.org/doc/html/rfc7519) and [RFC 8725](https://datatracker.ietf.org/doc/html/rfc8725).

## Federation and SSO

**Map each external subject to exactly one local user link.**

Federated sign-in should resolve or create a stable Federated Identity Link so one external account does not fan out to many local identities, and many people do not share one link.

**Start relying-party login at the authorization endpoint.**

Standard OIDC begins at `/oauth2/authorize`. Client, redirect, and consent belong on that same trust path. See [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html).

**Prefer OIDC; keep SAML secondary.**

Choose OpenID Connect for new integrations; retain SAML 2.0 only when an existing enterprise IdP requires it. See [SAML 2.0 Technical Overview](https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html).

**Synchronize identity lifecycle with standard protocols.**

When creating, updating, or disabling accounts across systems, prefer interoperable provisioning instead of manual reconciliation alone. See [SCIM 2.0](https://datatracker.ietf.org/doc/html/rfc7644).

## Audit

**Record management actions and authorization decisions as append-only facts.**

Who created a role and who was denied access should leave immutable records. Editing history after the fact breaks accountability.

**Make outcomes queryable by time and principal.**

Auditors need to reconstruct who changed what, and why a request was Allow or Deny. No matches should return an empty list, not an error.

**Protect session-based management surfaces from CSRF.**

State-changing Console and form-login requests should validate a CSRF token or equivalent. See [OWASP CSRF Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html).

## Related resources

| Resource | URL |
| -------- | --- |
| AWS IAM User Guide | https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html |
| AWS IAM policy evaluation | https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_evaluation-logic.html |
| AWS STS AssumeRole | https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html |
| OAuth 2.0 (RFC 6749) | https://datatracker.ietf.org/doc/html/rfc6749 |
| OAuth 2.1 (Internet-Draft) | https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1 |
| Bearer Token (RFC 6750) | https://datatracker.ietf.org/doc/html/rfc6750 |
| PKCE (RFC 7636) | https://datatracker.ietf.org/doc/html/rfc7636 |
| OAuth for Native Apps (RFC 8252) | https://datatracker.ietf.org/doc/html/rfc8252 |
| OAuth 2.0 Security BCP (RFC 9700) | https://datatracker.ietf.org/doc/html/rfc9700 |
| Pushed Authorization Requests (RFC 9126) | https://datatracker.ietf.org/doc/html/rfc9126 |
| DPoP (RFC 9449) | https://datatracker.ietf.org/doc/html/rfc9449 |
| JWT (RFC 7519) | https://datatracker.ietf.org/doc/html/rfc7519 |
| JWK (RFC 7517) | https://datatracker.ietf.org/doc/html/rfc7517 |
| JWT Best Current Practices (RFC 8725) | https://datatracker.ietf.org/doc/html/rfc8725 |
| OpenID Connect Core | https://openid.net/specs/openid-connect-core-1_0.html |
| OpenID Connect Discovery | https://openid.net/specs/openid-connect-discovery-1_0.html |
| FAPI 2.0 Security Profile | https://openid.net/specs/fapi-2_0-security-profile.html |
| SAML 2.0 Technical Overview | https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html |
| SCIM 2.0 Protocol (RFC 7644) | https://datatracker.ietf.org/doc/html/rfc7644 |
| NIST SP 800-63B | https://pages.nist.gov/800-63-4/sp800-63b.html |
| NIST SP 800-207 Zero Trust | https://csrc.nist.gov/pubs/sp/800/207/final |
| W3C Web Authentication (WebAuthn) | https://www.w3.org/TR/webauthn-3/ |
| FIDO2 | https://fidoalliance.org/fido2/ |
| Spring Security Reference | https://docs.spring.io/spring-security/reference/index.html |
| Spring Authorization Server | https://docs.spring.io/spring-authorization-server/reference/index.html |
| Spring Security OAuth2 | https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html |
| OWASP Authentication Cheat Sheet | https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html |
| OWASP Password Storage Cheat Sheet | https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html |
| OWASP CSRF Prevention | https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html |
