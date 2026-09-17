# Frontend Build & Release Pipeline

`@anvaya-prajna/explanation-player` is a React / TypeScript educational UI component library designed to render Anvaya-Prajna Explanation IR.

## Architecture & Tooling
- **Build Engine**: Vite 5 with Rollup lib mode bundling UMD and ESM modules.
- **Language**: TypeScript 5.5+ with full type definitions (`.d.ts`).
- **Styling**: Scoped CSS with BEM methodology and CSS Variables (`src/styles/player.css`).
- **Math Rendering**: KaTeX with fast client-side rendering.
- **Testing**: Vitest + React Testing Library + JSDOM.
- **Linting**: ESLint 9 with `@eslint/js` and `typescript-eslint`.

## Development Workflows

### 1. Standalone NPM Workflow (Recommended for UI Engineers)
```bash
cd packages/explanation-player
npm install

# Start local interactive dev server with hot reload
npm run dev

# Run unit & component tests
npm test

# Run linter
npm run lint

# Build production app and library distribution
npm run build
npm run build:lib
```

### 2. Gradle-Integrated Workflow
Gradle `node-gradle` plugin integration allows invoking frontend tasks from the project root:
```bash
./gradlew :packages:explanation-player:buildPlayer
./gradlew :packages:explanation-player:testPlayer
```

### 3. Docker Deployment
Production container builds use the multi-stage Dockerfile located in `packages/explanation-player/Dockerfile`:
```bash
docker build -t anvaya-prajna/explain-player:latest ./packages/explanation-player
```

### 4. NPM Packaging & Publishing
Library builds emit artifacts into `dist/`:
- `dist/explanation-player.js` (ES Module)
- `dist/explanation-player.umd.cjs` (UMD bundle)
- `dist/index.d.ts` (TypeScript types)
- `dist/style.css` (Styles)

Publishing to GitHub Packages:
```bash
npm publish --access public
```
Triggered automatically via CI/CD release workflow on version tags `v*`.
