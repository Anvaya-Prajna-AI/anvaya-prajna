# Release Process & Versioning Policy

Anvaya-Prajna adheres strictly to [Semantic Versioning (SemVer 2.0.0)](https://semver.org/):
`MAJOR.MINOR.PATCH`

- **MAJOR**: Incompatible API or Explanation IR schema changes.
- **MINOR**: Backward-compatible functionality additions (new domains, new renderers).
- **PATCH**: Backward-compatible bug fixes and performance improvements.

## Automated Versioning with Axion-Release
Project versions are computed from Git tags prefixed with `v` (e.g. `v1.0.0`).
- To check the current project version:
  ```bash
  ./gradlew currentVersion
  ```
- To bump patch version:
  ```bash
  ./gradlew markNextVersion -Prelease.versionIncrementer=incrementPatch
  ```
- To bump minor version:
  ```bash
  ./gradlew markNextVersion -Prelease.versionIncrementer=incrementMinor
  ```

## Tag-Triggered Automated Release Pipeline
Releases are triggered when a Git tag `v*` is pushed to GitHub:
```bash
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

The GitHub Actions Release workflow (`.github/workflows/release.yml`) will automatically:
1. Run multi-module Gradle test suite & JaCoCo verification.
2. Publish all 5 libraries to GitHub Packages and Maven Central (Sonatype OSSRH).
3. Build and publish `@anvaya-prajna/explanation-player` to GitHub npm Packages.
4. Build and push multi-architecture Docker images (`explain-service` and `explain-player`) to GitHub Container Registry (`ghcr.io`).
5. Generate a GitHub Release with automated changelog release notes.
