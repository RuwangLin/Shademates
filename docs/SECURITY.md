# Security and privacy

## Public-showcase hardening

The private coursework snapshot was not safe to publish as-is: it contained live-looking Mapbox/OpenWeather/backend credentials and a cloud endpoint in tracked files. This public repository was created from a fresh snapshot and a fresh Git history so those values are not recoverable from earlier public commits.

The showcase makes these changes only in the new copy:

- removes Mapbox runtime and Maven tokens from tracked files
- removes hardcoded OpenWeather and backend API keys
- replaces the backend endpoint with build-time configuration
- generates Android resource/BuildConfig values from user-level Gradle properties
- excludes APKs, local settings, IDE state, build outputs, private documents, contact tables, participant recordings, and the internal archive
- documents the remaining prototype risks and configuration expectations

The team's original private repository is unchanged.

## Secret handling

Never commit:

- `local.properties`
- live Gradle property values
- keystores or signing passwords
- API tokens or backend keys
- APKs built with shared/team credentials
- screenshots containing tokens, internal URLs, or stack traces

Use restricted, separately managed credentials for each environment. Rotate any key that has ever been committed to another repository, even if that repository is private.

## Privacy model

The final product design stores age-group and heat-sensitivity preferences on device. Relevant fields may be sent with a request when needed for personalisation. The design does not require persistent user accounts or a persistent server-side health profile.

Location is still sensitive. A production version should:

- request foreground location only when required
- explain why a feature needs it
- avoid background collection unless separately justified and consented
- minimise precision and retention
- document every backend log field
- provide deletion and consent controls

## Threat considerations

| Risk | Public-showcase position | Production expectation |
| --- | --- | --- |
| Credential leakage | Credentials removed from the new history | Secret scanning, rotation, scoped tokens, CI secret store |
| API abuse | Backend details are configurable | Authentication, authorisation, quotas, WAF/rate limits |
| Location/profile disclosure | No collected user data is included | Data minimisation, retention rules, encryption, audit logging |
| Misleading health advice | Prototype disclaimer and uncertainty | Clinical/content governance and source review |
| Untrusted API response | Retrofit mapping and basic failure states | Schema validation, contract tests, response hardening |
| Dependency vulnerabilities | Versioned Gradle dependencies | Dependabot/SCA, patch policy, signed release process |
| Map/data tampering or staleness | Limited prototype indicators | Integrity checks, source timestamps, monitoring |

## Reporting a vulnerability

Do not open a public issue containing a credential, exploit, personal data, or sensitive endpoint. Use GitHub's private vulnerability reporting for this repository if available, or contact the repository owner privately through their GitHub profile.

## Safety boundary

ShadeMates is a coursework prototype, not a medical device or emergency alert service. Environmental and route values can be incomplete, stale, or inaccurate. Users should follow official health/emergency guidance and their own medical advice.
