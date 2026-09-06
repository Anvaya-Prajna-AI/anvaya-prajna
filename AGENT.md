# Agent Guidelines & Configuration — Anvaya-Prajna-AI

## Code Modification Protocol

### 1. Pre-Modification Inspection
Before modifying any existing file in this project:
- Always read/view the complete file or the relevant section first.
- Understand the existing responsibilities, dependencies, and helper methods.

### 2. Guard Against Overwriting
- **Never overwrite or discard existing implementations unintentionally.**
- When enhancing or refactoring a class or function:
  - Preserve all other methods, fields, and imports in the file.
  - Maintain backward compatibility of public APIs and data models.
  - Never leave placeholder stubs (such as `// previous implementation here`) when writing files.

### 3. Verification & Safety
- Run the build/test suite after any modifications (`./gradlew test` or `./gradlew build`) to ensure zero regressions across all modules.
- Ensure strict adherence to project JSON schemas and IR specifications.
