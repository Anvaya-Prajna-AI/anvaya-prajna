# Safe File Modification Rule

## Purpose
Enforces strict safeguards when an AI assistant or agent modifies existing source files across the codebase, ensuring that existing implementations, methods, imports, comments, and contracts are not accidentally truncated, deleted, or overwritten.

## Mandatory Workflow for Modifying Existing Files

### 1. Mandatory Pre-Read Inspection
- **Rule:** NEVER overwrite or edit a file without first viewing and thoroughly analyzing its existing contents.
- Before applying any change, inspect:
  - Existing imports and package declarations.
  - All existing classes, methods, constructors, interfaces, and helper functions.
  - Edge-case handling, error checking, and comments.

### 2. Preservation of Existing Capabilities
- When adding new features or fixing a bug, all other existing methods and behaviors must be explicitly preserved unless the user explicitly requested their removal.
- Avoid truncating files or replacing existing functions with placeholder comments like `// ... rest of code unchanged ...`.

### 3. Non-Destructive Edits
- Verify that every existing public/protected method signature remains intact to prevent breaking downstream callers.
- Ensure all annotations (e.g. `@Override`, `@NotNull`, `@Entity`, `@Component`, `@Builder`) and type parameters are retained.

### 4. Post-Modification Verification
- Immediately after editing any file, compile and run the test suite to verify:
  1. No compilation errors exist.
  2. No existing unit or integration tests break.
  3. The modified functionality satisfies the requirement without regressions.
