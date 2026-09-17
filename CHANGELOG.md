# Changelog

All notable changes to the Anvaya-Prajna project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-09-17

### Added
- Multi-agent explanation synthesis pipeline across 7 curriculum domains (Algebra, Arithmetic, Physics, Chemistry, Ratio/Percentage, Logic/Syllogisms, Number Series).
- OpenAPI 3 specification and interactive Swagger UI documentation at `/swagger-ui.html` and `/api-docs`.
- Flyway database migration infrastructure (`V1__initial_schema.sql`, `V2__add_review_columns.sql`) with Hibernate `validate` strategy.
- OWASP ASI-compliant role-based security (`STUDENT`, `EDUCATOR`, `ADMIN`) with `EngineSecurityAuthorizer`.
- Resilience4j rate limiting on explanation generation endpoints.
- End-to-end OpenTelemetry distributed tracing with Jaeger exporter and Micrometer Prometheus metrics at `/actuator/prometheus`.
- Centralized Gradle version catalog `gradle/libs.versions.toml` with Dependabot automated update configuration.
- JaCoCo multi-module code coverage reporting with 70% minimum threshold enforcement.
- `@anvaya-prajna/explanation-player` React UI component library with Vitest test suite and progressive hint scaffolding.
- GitHub Actions CI/CD matrix pipeline (`.github/workflows/ci.yml`) and automated release pipeline (`.github/workflows/release.yml`).
- Community and governance documentation: `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `CODEOWNERS`, and issue/PR templates.
