# Guideline

## Introduce

Explore IAM helps you connect the right people to the right apps with clear trust boundaries. Use it so administrators can maintain identity and policy, relying parties can obtain tokens through standard sign-in, and auditors can reconstruct who did what and when.

Identity and access management defines who may authenticate, what they may do, and how temporary access is issued. Prefer standard protocols and durable principal models over ad-hoc login or permission schemes. This guideline describes how to design those experiences so people stay in control, integrations stay interoperable, and outcomes stay auditable.

## Best practices

**Treat Explore IAM as the single OIDC provider for relying parties.** Business apps should trust identity and access tokens issued by this platform instead of inventing their own login protocols. A shared issuer keeps validation, discovery, and revocation consistent across apps.

**Prefer Authorization Code with PKCE.** Require PKCE for clients that use the authorization code flow; reject Implicit and Resource Owner Password Credentials. Match redirect URIs exactly, and never put Bearer tokens in query strings.

**Keep client secrets off browsers.** Secrets belong only in the relying party’s back-channel token exchange. The front end may use `client_id`, `state`, and PKCE material—never the client secret. Publish issuer metadata and JWKS so relying parties can validate signatures without hard-coded keys.

**Model lasting identity with User, Group, and Role.** Use IAM User for stable principals, Group for shared policy, and Role for assumable access. Prefer short-lived sessions over permanent elevation.

**Evaluate Deny before Allow, then implicit Deny.** Grant least privilege and keep verifying who can do what instead of trusting a single login forever. Explicit deny must override allow; with no match, deny by default.

**Issue short-lived STS credentials only after AssumeRole.** Callers exchange an existing identity for a temporary session; the trust policy decides who may assume the Role. Reject expired sessions instead of renewing them quietly in the background.

**Map each external subject to one Federated Identity Link.** Prefer OpenID Connect for new integrations; retain SAML only when an existing enterprise IdP requires it. One external account should not fan out to many local identities.

**Keep management and authorization outcomes append-only and queryable.** Who created a role and who was denied access should leave immutable records. Editing history after the fact breaks accountability.

## Protocols and authorization platform

### OIDC Provider

**Use Explore IAM as the OIDC provider for relying parties.** Business apps should trust identity and access tokens issued by this platform instead of inventing their own login protocols. For developer guidance, see [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html) and [OpenID Connect Core](https://openid.net/specs/openid-connect-core-1_0.html).

### OAuth 2.1

**Converge authorization flows on OAuth 2.1 and the security BCP.** Require PKCE for every client that uses the authorization code flow; match redirect URIs exactly; do not offer Implicit or Resource Owner Password Credentials; do not put Bearer tokens in query strings. For guidance, see [OAuth 2.1](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1), [RFC 9700](https://datatracker.ietf.org/doc/html/rfc9700), and [RFC 7636](https://datatracker.ietf.org/doc/html/rfc7636).

### Authorization Code and PKCE

**Prefer Authorization Code with PKCE for browser clients.** When people sign in from a browser, the code flow keeps secrets on the server; PKCE reduces the risk of stolen authorization codes for public clients. For guidance, see [RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749) and [OAuth for Native Apps](https://datatracker.ietf.org/doc/html/rfc8252).

### Client secrets

**Keep client_secret out of the browser.** Secrets belong only in the relying party’s back-channel token exchange. The front end may use `client_id`, `state`, and PKCE material—never the client secret. For guidance, see [RFC 6750](https://datatracker.ietf.org/doc/html/rfc6750).

### Issuer discovery and JWKS

**Publish a verifiable issuer through discovery and JWKS.** Relying parties should validate issuer and signatures from OpenID Provider Configuration and JWKS instead of hard-coding keys. For guidance, see [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) and [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517).

### High-assurance profiles

**Raise sender-constrained profiles for high-assurance APIs.** For finance-grade or cross-boundary APIs, build on the baseline with PAR, DPoP or mTLS, and the FAPI 2.0 Security Profile when required. For guidance, see [FAPI 2.0 Security Profile](https://openid.net/specs/fapi-2_0-security-profile.html), [RFC 9449](https://datatracker.ietf.org/doc/html/rfc9449), and [RFC 9126](https://datatracker.ietf.org/doc/html/rfc9126).

## Identity

### IAM User

**Treat IAM User as the long-lived principal for form login.** People and service accounts need a manageable, stable identity. Sessions may expire; the user record itself must remain auditable and disableable. For developer guidance, see [AWS IAM User Guide](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html).

### Account disablement

**Disable the account when access must stop immediately.** After disable, the principal must not authenticate for management or STS. Record the disable outcome in audit so investigators can reconstruct what changed and when.

### Groups and roles

**Use Group for shared policy and Role for assumable identity.** Membership fits bulk authorization; roles fit short, revocable duty changes. Do not bake temporary tasks into everyone’s lasting permissions.

### Authentication assurance

**Match authentication strength to assurance level.** Passwords fit only risk-aligned scenarios; high-value actions should step up to multi-factor or phishing-resistant authentication. For guidance, see [NIST SP 800-63B](https://pages.nist.gov/800-63-4/sp800-63b.html) and [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html).

### Passkeys and WebAuthn

**Plan a phishing-resistant sign-in path.** Passkeys / WebAuthn reduce shared-secret and phishing risk. Introduce them alongside form login rather than cutting off existing users overnight. For guidance, see [W3C Web Authentication](https://www.w3.org/TR/webauthn-3/) and [FIDO2](https://fidoalliance.org/fido2/).

### Password storage

**Never store credentials in plaintext or reversible form.** Verify secrets by comparing hashes, and handle credential material with care. For guidance, see [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

## Policy

### Policy evaluation

**Evaluate Deny before Allow, then deny by default.** An explicit deny must override allow; with no match, deny. For developer guidance, see [AWS IAM policy evaluation logic](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_evaluation-logic.html).

### Policy attachment

**Attach policy to identifiable principals by stable ARN.** Attachments should point at User, Group, or Role identities people and systems can recognize over time. Loose text breaks under rename and migration and makes audit hard to replay.

### Least privilege

**Grant only the access an observable job needs.** Console and API roles should map to real duties, not open-by-default access. Keep verifying who can do what instead of trusting a single login forever. For guidance, see [NIST SP 800-207 Zero Trust Architecture](https://csrc.nist.gov/pubs/sp/800/207/final).

## STS

### AssumeRole

**Issue temporary credentials only after AssumeRole succeeds.** Callers exchange an existing identity for a short session; the trust policy decides who may assume the Role. Do not mint tokens without that check. For developer guidance, see [AWS STS AssumeRole](https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html).

### Session expiration

**Make session boundaries clear and enforceable.** Temporary credentials are valuable because the boundary is clear: when they expire, callers must assume again or fall back to narrower rights. Do not renew expired sessions quietly in the background.

### Token claims

**Validate JWT claims and algorithms carefully.** When issuing and validating JWTs, check `iss`, `aud`, `exp`, and the signing algorithm; reject `none` and weak algorithms. For guidance, see [RFC 7519](https://datatracker.ietf.org/doc/html/rfc7519) and [RFC 8725](https://datatracker.ietf.org/doc/html/rfc8725).

## Federation and SSO

### Federated identity link

**Resolve federation to one stable Federated Identity Link.** Federated sign-in should resolve or create a single link so one external account does not fan out to many local identities, and many people do not share one link.

### Authorization endpoint

**Start standard OIDC at the authorization endpoint.** Standard OIDC begins at `/oauth2/authorize`. Client, redirect, and consent belong on that same trust path. For guidance, see [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html).

### OIDC and SAML

**Prefer OpenID Connect for new integrations.** Choose OIDC when you can; retain SAML 2.0 only when an existing enterprise IdP requires it. For guidance, see [SAML 2.0 Technical Overview](https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html).

### SCIM

**Prefer interoperable provisioning over manual reconciliation alone.** When creating, updating, or disabling accounts across systems, use SCIM so lifecycle changes stay consistent. For guidance, see [SCIM 2.0](https://datatracker.ietf.org/doc/html/rfc7644).

## Audit

### Append-only records

**Keep management and authorization history immutable.** Who created a role and who was denied access should leave append-only records. Editing history after the fact breaks accountability and makes investigations unreliable.

### Audit queries

**Help auditors reconstruct Allow and Deny decisions.** Auditors need to reconstruct who changed what, and why a request was Allow or Deny. No matches should return an empty list, not an error.

### CSRF protection

**Protect state-changing Console and form-login requests.** Validate a CSRF token or equivalent before accepting those actions. For guidance, see [OWASP CSRF Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html).

## Resources

### Related

| Resource | URL |
| -------- | --- |
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
| OWASP Authentication Cheat Sheet | https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html |
| OWASP Password Storage Cheat Sheet | https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html |
| OWASP CSRF Prevention | https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html |

### Developer documentation

| Resource | URL |
| -------- | --- |
| AWS IAM User Guide | https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html |
| AWS IAM policy evaluation | https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_evaluation-logic.html |
| AWS STS AssumeRole | https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html |
| Spring Security Reference | https://docs.spring.io/spring-security/reference/index.html |
| Spring Authorization Server | https://docs.spring.io/spring-authorization-server/reference/index.html |
| Spring Security OAuth2 | https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html |
