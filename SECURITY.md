# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.x     | :white_check_mark: |

## Reporting a Vulnerability

The Anvaya-Prajna team takes the security of our software seriously. If you discover a security vulnerability, please report it responsibly.

**Do NOT report security vulnerabilities via public GitHub issues.**

Instead, please report vulnerabilities by emailing:
`security@anvaya.ai`

Include the following details where possible:
- Description of the vulnerability and attack vector
- Steps to reproduce or proof-of-concept payload
- Impact assessment
- Any suggested remediations

You should receive an acknowledgment within 48 hours. We will keep you updated on progress towards resolving the issue.

## OWASP ASI Compliance & Threat Model

Anvaya-Prajna incorporates defenses against the OWASP Top 10 for Agentic Systems & LLMs:
- **ASI01 (Prompt Injection & Jailbreak)**: Handled via `OwaspAgentSecurityGuard` multi-layer regex and heuristic sanitizers.
- **ASI02 (Excessive Agency / Output Sanitization)**: Deterministic domain execution fallback and strict structured JSON schemas.
- **ASI03 (Identity & Privilege Abuse)**: Role-based authorization enforced via `EngineSecurityAuthorizer` (`STUDENT`, `EDUCATOR`, `ADMIN`).
- **ASI05 (Sensitive Data Exposure)**: PII scrubbing and credential neutralization on inputs and outputs.
- **ASI07 (System Prompt Leakage)**: Interception and scrubbing of internal prompt reflection tokens.

## Production Hardening Guide
1. **OAuth2/OIDC**: Configure Spring Security resource server with an identity provider (Keycloak, Auth0, Okta).
2. **Rate Limiting**: Configured with Resilience4j rate limiters per IP / tenant.
3. **Redis & DB Encryption**: Ensure Redis authentication is enabled (`requirepass`) and TLS is active in production.
4. **Actuator Isolation**: Keep `/actuator/metrics` and management endpoints behind internal private networks and require `ROLE_ADMIN`.
