# Anvaya-Prajna-AI (अन्वय-प्रज्ञा)

[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-21-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)]()
[![React](https://img.shields.io/badge/React-18-blue.svg)]()
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**An educational reasoning & explanation engine that transforms assessment questions and verified solutions into structured, student-friendly, multi-modal explanations.**

> **Sanskrit roots:**
> - **Anvaya** (*अन्वय*) — *logical sequence, structured connection, inference*
> - **Prajna** (*प्रज्ञा*) — *wisdom, deep understanding, discernment*

---

## 1. Core Philosophy

> **One verified reasoning model → many synchronized educational representations.**

Traditional AI-generated answers suffer from hallucinations, inconsistent formatting, or directly generating untrusted presentation code. **Anvaya-Prajna** decouples reasoning from presentation:

1. **Constrained AI Proposal:** The AI model produces a structured reasoning proposal adhering to strict JSON schemas.
2. **Deterministic Validation & Normalization:** Mathematical steps, logical dependencies, and schema constraints are rigorously verified.
3. **Explanation IR (Intermediate Representation):** A canonical contract that downstream renderers (web, mobile, SVG, animation, audio) consume to render rich, synchronized explanations.

```
+---------------------+     +--------------------------+     +--------------------------+
| Assessment Question | --> |   AI Reasoning Planner   | --> | Deterministic Validation |
| & Verified Solution |     | (LiteLLM / Ollama / API) |     |  (Math & Schema Engine)  |
+---------------------+     +--------------------------+     +--------------------------+
                                                                          |
                                                                          v
+----------------------------------------------------------------------------------------+
|                               Explanation IR (Canonical)                               |
|   - Step-by-Step Reasoning Graph          - Common Misconceptions & Pitfalls           |
|   - Mathematical Expressions (LaTeX/Sym)  - Interactive / Static Diagram IR            |
|   - Multi-Level Progressive Hints         - Synchronized Animation Sequences           |
+----------------------------------------------------------------------------------------+
                                           |
                                           v
+----------------------------------------------------------------------------------------+
|                       Explain Player UI (React / Vite / Nginx)                         |
|   - Dynamic Step Navigator & Audio Sync   - KaTeX Formula & Expression Display         |
|   - SVG Geometry / Number Line Renderers  - Live API Backend Explorer & IR Inspector   |
+----------------------------------------------------------------------------------------+
```

---

## 2. Project Architecture & Modules

The repository is organized as a multi-module Java 21 / Spring Boot 3 backend and React TypeScript UI project:

```
anvaya-prajna/
├── schemas/                  # Canonical JSON Schemas
│   ├── explanation-ir.schema.json
│   ├── reasoning-step.schema.json
│   ├── math-expression.schema.json
│   ├── diagram-ir.schema.json
│   └── animation-ir.schema.json
│
├── libraries/
│   ├── explanation-ir/       # Java domain models & JSON Schema serialisation for IR
│   ├── math-core/            # Deterministic math engine (parsers, symbol verifiers)
│   ├── reasoning-core/       # Reasoning graph engine, misconception models, scaffolders
│   └── validation-core/      # JSON Schema validator, math rule checker, semantic audits
│
├── services/
│   └── explain-service/      # Spring Boot 3 REST microservice
│       ├── ai/               # AI reasoning planner & prompt engineering
│       ├── application/      # Orchestration, caching, and domain plugins
│       ├── config/           # Redis, PostgreSQL, Jackson configuration
│       ├── controller/       # REST API endpoints & Health indicators
│       ├── domain/           # 5 Domain Plugins (Math, Physics, Chemistry, Biology, Code)
│       └── persistence/      # PostgreSQL entities, Flyway migrations, Redis cache
│
├── packages/
│   └── explanation-player/   # React 18 / TypeScript / Vite student explanation player
│       ├── src/components/   # Player, navigator, hint panel, verification & misconception cards
│       ├── src/renderers/    # KaTeX math, SVG diagrams, reasoning DAG graph, animations
│       ├── Dockerfile        # Multi-stage Alpine container with Nginx reverse proxy
│       └── nginx.conf        # Nginx configuration routing /api/* to explain-service
│
├── docs/                     # Detailed architecture and SRS documentation
├── docker-compose.yml        # Multi-container orchestration (Player UI, App, Postgres, Redis, LiteLLM)
└── litellm-config.yaml       # Multi-provider LLM proxy configuration
```

---

## 3. Features & Domain Plugins

- **Multi-Modal Explanation IR:** Generates text steps, mathematical expressions, diagram models (coordinates, nodes, edges), and progressive hint tiers.
- **Interactive Explanation Player UI:** Complete React/TypeScript frontend supporting step playback, KaTeX math typesetting, SVG diagrams, reasoning graphs, and live backend exploration.
- **5 Built-In Domain Plugins:**
  - `MathematicsPlugin`: Equation solvers, step validation, geometry diagrams.
  - `PhysicsPlugin`: Free-body diagrams, unit conversions, kinematic verification.
  - `ChemistryPlugin`: Chemical reaction balancing, stoichiometric steps, molecular structures.
  - `BiologyPlugin`: Biological processes, taxonomy graphs, pathway diagrams.
  - `ComputerSciencePlugin`: Algorithm traces, complexity analysis, control-flow diagrams.
- **AI Gateway Integration:** Seamless connectivity to OpenAI, Anthropic, Gemini, Mistral, and local Ollama instances via an optional LiteLLM gateway.
- **Resilient Persistence & Caching:** PostgreSQL for persistent storage and Flyway schema migrations, with Redis for sub-millisecond IR response caching.

---

## 4. Getting Started

### Prerequisites
- **JDK 21** or later
- **Node.js 20+** (for frontend development)
- **Docker & Docker Compose** (for containerized setup)

### Building the Project

```bash
# Build all backend libraries and services
./gradlew build

# Run unit and integration tests across all modules
./gradlew test

# Build the Explanation Player UI
cd packages/explanation-player
npm ci
npm run build
```

### Running Locally with Docker Compose

1. Copy the environment configuration:
   ```bash
   cp .env.example .env
   ```
2. Start all services (Explain Player UI, Explain Service, Postgres, Redis, and LiteLLM):
   ```bash
   docker compose up --build -d
   ```
3. Access the services:
   - **Explain Player UI**: [http://localhost:3000](http://localhost:3000)
   - **Explain Service API**: [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health)
   - **LiteLLM Gateway**: [http://localhost:4000](http://localhost:4000)

For detailed container management and configuration options, see [DOCKER.md](DOCKER.md).

---

## 5. API Reference

### Health Check
```http
GET /api/v1/health
```
```json
{
  "status": "UP",
  "service": "explain-service",
  "version": "1.0.0"
}
```

### Generate Explanation IR
```http
POST /api/v1/explanations/generate
Content-Type: application/json
```
```json
{
  "question": {
    "id": "q-101",
    "prompt": "Solve for x: 2x + 6 = 14",
    "domain": "MATHEMATICS",
    "gradeLevel": "8",
    "targetAudience": "middle_school"
  },
  "verifiedSolution": {
    "finalAnswer": "x = 4",
    "steps": [
      "Subtract 6 from both sides: 2x = 8",
      "Divide both sides by 2: x = 4"
    ]
  },
  "options": {
    "includeHints": true,
    "includeMisconceptions": true,
    "includeDiagrams": true,
    "maxDepth": 3
  }
}
```

---

## 6. License

This project is licensed under the [Apache License 2.0](LICENSE).
