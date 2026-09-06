# Guideline

Identity and access in `explore-iam` helps sibling Explore products and any
relying party authenticate people and services, obtain tokens through standard
protocols, and grant only the access a job needs—when those outcomes are clear
and auditable. Use it as a shared OIDC provider and IAM control plane without
shipping ad-hoc login stacks inside every application.

## Introduction

This guideline describes how to design identity, policy, STS, federation, and
audit in `explore-iam`. Prefer open protocols, primary research, and Apple
identity guidance when shaping trust boundaries. Vocabulary lives in the
[Glossary](Glossary.md); architecture in the [C4 model](developer/c4-model/).

## Best practices

### Single issuer

**Treat Explore IAM as the single OIDC provider for relying parties.** Trust
tokens from this issuer instead of inventing parallel login protocols. See
[OpenID Connect Core](https://openid.net/specs/openid-connect-core-1_0.html)
and [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html).

### Authorization Code and PKCE

**Prefer Authorization Code with PKCE.** Reject Implicit and password grants;
match redirect URIs exactly; never put Bearer tokens in query strings. See
[OAuth 2.1](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1),
[RFC 9700](https://datatracker.ietf.org/doc/html/rfc9700), and
[RFC 7636](https://datatracker.ietf.org/doc/html/rfc7636).

### Client secrets

**Keep client secrets off browsers.** Secrets belong only in the relying
party’s back-channel token exchange. Publish issuer metadata and JWKS. See
[RFC 6750](https://datatracker.ietf.org/doc/html/rfc6750) and
[RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517).

### Principals

**Model lasting identity with User, Group, and Role.** Prefer short-lived
sessions over permanent elevation. See
[The Protection of Information in Computer Systems](https://web.mit.edu/Saltzer/www/publications/protection/),
[Role-Based Access Control Models](https://profsandhu.com/journals/computer/i94rbac%28org%29.pdf),
and [Managing accounts](https://developer.apple.com/design/human-interface-guidelines/managing-accounts).

### Policy evaluation

**Evaluate Deny before Allow, then implicit Deny.** Grant least privilege and
keep re-checking who can do what. See
[Privacy](https://developer.apple.com/design/human-interface-guidelines/privacy)
and [NIST SP 800-207](https://csrc.nist.gov/pubs/sp/800/207/final).

### Temporary credentials

**Issue short-lived STS credentials only after AssumeRole.** Reject expired
sessions instead of renewing them quietly.

### Federated identity

**Map each external subject to one Federated Identity Link.** Prefer OIDC for
new integrations; retain SAML only when an enterprise IdP requires it. See
[Sign in with Apple](https://developer.apple.com/design/human-interface-guidelines/sign-in-with-apple).

### Append-only audit

**Keep management and authorization outcomes append-only and queryable.**

## Protocols and authorization platform

### OIDC Provider

**Use Explore IAM as the OIDC provider for relying parties.** See
[Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html),
[OpenID Connect Core](https://openid.net/specs/openid-connect-core-1_0.html),
and [Sign in with Apple REST API](https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api).

### OAuth 2.1

**Converge flows on OAuth 2.1 and the security BCP.** Require PKCE; reject
Implicit and Resource Owner Password Credentials. See
[OAuth 2.1](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1) and
[RFC 9700](https://datatracker.ietf.org/doc/html/rfc9700).

### Issuer discovery and JWKS

**Publish a verifiable issuer through discovery and JWKS.** Validate issuer and
signatures instead of hard-coding keys. See
[OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html).

## Identity

### IAM User

**Treat IAM User as the long-lived principal.** Sessions expire; the user
record stays auditable and disableable. See
[Managing accounts](https://developer.apple.com/design/human-interface-guidelines/managing-accounts).

### Account disablement

**Disable the account when access must stop immediately.** After disable, the
principal must not authenticate for management or STS; record the outcome in
audit.

### Groups and roles

**Use Group for shared policy and Role for assumable identity.** Do not bake
temporary tasks into lasting permissions. See
[Role-Based Access Control Models](https://profsandhu.com/journals/computer/i94rbac%28org%29.pdf).

### Authentication assurance

**Match authentication strength to assurance level.** Step up high-value
actions to multi-factor or passkeys. See
[NIST SP 800-63B](https://pages.nist.gov/800-63-4/sp800-63b.html) and
[Passkeys](https://developer.apple.com/passkeys).

### Password storage

**Never store credentials in plaintext or reversible form.** Verify by
comparing hashes. See
[OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

## Policy

### Policy evaluation

**Evaluate Deny before Allow, then deny by default.** See
[The Protection of Information in Computer Systems](https://web.mit.edu/Saltzer/www/publications/protection/).

### Policy attachment

**Attach policy to principals by stable identifiers.** Loose text breaks under
rename and migration. Prefer durable subjects as in
[Sign in with Apple](https://developer.apple.com/documentation/signinwithapple).

### Least privilege

**Grant only the access an observable job needs.** See
[Privacy](https://developer.apple.com/design/human-interface-guidelines/privacy)
and [NIST SP 800-207](https://csrc.nist.gov/pubs/sp/800/207/final).

## STS

### AssumeRole

**Issue temporary credentials only after AssumeRole succeeds.** Do not mint
tokens without the trust-policy check.

### Session expiration

**Make session boundaries enforceable.** When credentials expire, callers must
assume again or fall back to narrower rights.

### Token claims

**Validate JWT `iss`, `aud`, `exp`, and algorithms.** Reject `none` and weak
algorithms. See [RFC 7519](https://datatracker.ietf.org/doc/html/rfc7519),
[RFC 8725](https://datatracker.ietf.org/doc/html/rfc8725), and
[Sign in with Apple REST API](https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api).

## Federation and SSO

### Federated identity link

**Resolve federation to one stable Federated Identity Link.** See
[Sign in with Apple](https://developer.apple.com/design/human-interface-guidelines/sign-in-with-apple).

### Authorization endpoint

**Start OIDC at `/oauth2/authorize`.** Client, redirect, and consent belong on
that trust path. See
[OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html).

### OIDC and SAML

**Prefer OpenID Connect for new integrations.** Retain SAML 2.0 only when an
enterprise IdP requires it. See
[SAML 2.0 Technical Overview](https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html).

### SCIM

**Prefer SCIM for cross-system provisioning.** See
[SCIM 2.0](https://datatracker.ietf.org/doc/html/rfc7644).

## Audit

### Append-only records

**Keep management and authorization history immutable.**

### Audit queries

**Help auditors reconstruct Allow and Deny decisions.** No matches return an
empty list, not an error.

### CSRF protection

**Protect state-changing Console and form-login requests.** See
[Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

## Resources

### Related

[The Protection of Information in Computer Systems](https://web.mit.edu/Saltzer/www/publications/protection/)

[Role-Based Access Control Models](https://profsandhu.com/journals/computer/i94rbac%28org%29.pdf)

[OAuth 2.0 (RFC 6749)](https://datatracker.ietf.org/doc/html/rfc6749)

[OAuth 2.1 (Internet-Draft)](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1)

[Bearer Token Usage (RFC 6750)](https://datatracker.ietf.org/doc/html/rfc6750)

[PKCE (RFC 7636)](https://datatracker.ietf.org/doc/html/rfc7636)

[OAuth 2.0 Security BCP (RFC 9700)](https://datatracker.ietf.org/doc/html/rfc9700)

[OpenID Connect Core](https://openid.net/specs/openid-connect-core-1_0.html)

[OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html)

[JSON Web Token (RFC 7519)](https://datatracker.ietf.org/doc/html/rfc7519)

[JSON Web Key (RFC 7517)](https://datatracker.ietf.org/doc/html/rfc7517)

[JWT Best Current Practices (RFC 8725)](https://datatracker.ietf.org/doc/html/rfc8725)

[SAML 2.0 Technical Overview](https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html)

[SCIM 2.0 Protocol (RFC 7644)](https://datatracker.ietf.org/doc/html/rfc7644)

[NIST SP 800-63B](https://pages.nist.gov/800-63-4/sp800-63b.html)

[NIST SP 800-207 Zero Trust Architecture](https://csrc.nist.gov/pubs/sp/800/207/final)

[W3C Web Authentication (WebAuthn)](https://www.w3.org/TR/webauthn-3/)

[Managing accounts](https://developer.apple.com/design/human-interface-guidelines/managing-accounts)

[Sign in with Apple (HIG)](https://developer.apple.com/design/human-interface-guidelines/sign-in-with-apple)

[Privacy](https://developer.apple.com/design/human-interface-guidelines/privacy)

[Passkeys](https://developer.apple.com/passkeys)

### Developer documentation

[Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/index.html)

[Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)

[Spring Security Authentication](https://docs.spring.io/spring-security/reference/servlet/authentication/index.html)

[Spring Security SAML2](https://docs.spring.io/spring-security/reference/servlet/saml2/index.html)

[Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)

[Authentication Services](https://developer.apple.com/documentation/authenticationservices)

[Sign in with Apple](https://developer.apple.com/documentation/signinwithapple)

[Sign in with Apple REST API](https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api)

[Glossary](Glossary.md)

[C4 model](developer/c4-model/)
