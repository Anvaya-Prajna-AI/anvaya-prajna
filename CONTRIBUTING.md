# Contributing to Anvaya-Prajna

Thank you for your interest in contributing to **Anvaya-Prajna**! We welcome contributions from developers, educators, researchers, and designers.

## Code of Conduct
All contributors and maintainers are expected to adhere to our [Code of Conduct](CODE_OF_CONDUCT.md).

## Getting Started
1. Fork the repository and clone it locally:
   ```bash
   git clone https://github.com/<your-username>/anvaya-prajna.git
   cd anvaya-prajna
   ```
2. Ensure you have **Java 21** and **Node 20+** installed.
3. Build the backend and run the test suite:
   ```bash
   ./gradlew check
   ```

## Development Standards & Guidelines

### 1. Branch Naming & Git Workflow
- Create a feature branch from `main`:
  - `feat/add-geometry-domain`
  - `fix/math-ast-fraction-eval`
  - `docs/update-migrations-guide`

### 2. Conventional Commits
All commits must follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:
- `feat(math-core)`: add support for quadratic equation solving
- `fix(explain-service)`: handle null policy in explanation generation
- `docs(api)`: document OpenAPI query parameters
- `test(validation-core)`: add AST check regression test
- `refactor(reasoning-core)`: streamline step animation compilation

### 3. Code Coverage & Testing
- Every bug fix or new feature must include unit or integration tests.
- Code coverage is enforced at **70% minimum line coverage** via JaCoCo. Run `./gradlew jacocoTestReport` to inspect coverage reports.

### 4. Code Formatting & Style
- Java: Follow standard Google Java Style Conventions.
- TypeScript / React: Adhere to ESLint rules (`npm run lint` in `packages/explanation-player`).

## Pull Request Process
1. Ensure all Gradle tests and linter checks pass locally:
   ```bash
   ./gradlew check
   cd packages/explanation-player && npm run lint && npm test
   ```
2. Submit your Pull Request against the `main` branch using the provided [PR Template](.github/PULL_REQUEST_TEMPLATE.md).
3. Ensure the PR links to an existing open issue.
4. Maintainers will review your PR and provide constructive feedback.
