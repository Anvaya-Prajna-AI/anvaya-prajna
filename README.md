# Anvaya-Prajna-AI (अन्वय-प्रज्ञा)

[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-21-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)]()
[![React](https://img.shields.io/badge/React-18-blue.svg)]()
[![JaCoCo Coverage](https://img.shields.io/badge/coverage-%E2%89%A570%25-brightgreen.svg)]()
[![OWASP Agentic Top 10](https://img.shields.io/badge/OWASP%20Agentic-Compliant-blue.svg)]()
[![OpenAPI 3](https://img.shields.io/badge/OpenAPI-3.0-brightgreen.svg)](http://localhost:8080/swagger-ui.html)
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
3. **Explanation IR (Intermediate Representation):** A canonical contract (`schemas/explanation-ir.schema.json`) that downstream renderers (web, mobile, SVG, animation, audio) consume to render rich, synchronized explanations.
4. **OWASP Agentic Top 10 Hardening:** Built-in safeguards against prompt hijack (ASI01), tool misuse (ASI02), identity & privilege abuse (ASI03), supply-chain tampered models (ASI04), unsafe code execution (ASI05), and cascading model failures (ASI08).

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

The repository is organized into modular libraries, a reusable core engine, an example reference REST microservice, and a React TypeScript UI:

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
│   ├── explanation-ir/       # Java domain models & JSON Schema serialization for IR
│   ├── math-core/            # AST expression evaluator (exp4j, safe against RCE/ASI05)
│   ├── reasoning-core/       # Reasoning graph engine, DAG traversal, step models
│   ├── validation-core/      # JSON Schema validator, math rule checker, safety guards
│   └── explain-core/         # Reusable Core Reasoning Engine (ASI03 decoupled)
│       ├── ai/               # ReasoningPlanner, ModelRouter, OwaspAgentSecurityGuard
│       ├── application/      # ExplanationService, CompilationService, HintService
│       ├── cache/            # ExplanationCacheService (Redis L2 caching)
│       ├── domain/           # 5 Deterministic Domain Plugins (Algebra, Arithmetic, Logic, etc.)
│       ├── repository/       # Spring Data JPA repositories & entity models
│       ├── security/         # EngineSecurityContext, Authorizer & RBAC interfaces
│       └── config/           # @EnableExplanationEngine auto-configuration
│
├── services/
│   └── explain-service/      # Example Reference Spring Boot REST Microservice
│       ├── api/              # Controllers (Explanation, Hint, Validation, Why) with OpenAPI 3
│       ├── security/         # SecurityConfig (JWT/OAuth2 Resource Server + Header mapping)
│       ├── observability/    # LiteLLM Health Indicator, OpenTelemetry OTLP tracing
│       └── resources/        # Flyway migrations, application.yml, logback structured JSON
│
├── packages/
│   └── explanation-player/   # React 18 / TypeScript / Vite / Vitest explanation player
│       ├── src/components/   # Player, navigator, hint panel, verification cards
│       ├── src/renderers/    # KaTeX math, SVG diagrams, reasoning DAG graph
│       ├── Dockerfile        # Multi-stage Alpine container with Nginx reverse proxy
│       └── nginx.conf        # Nginx configuration routing /api/* to explain-service
│
├── docs/                     # Documentation (Migrations, Frontend Pipeline, Releasing, SRS)
├── docker-compose.yml        # Orchestration (UI, App, Postgres, Redis, LiteLLM, Jaeger, Prometheus, Grafana)
└── litellm-config.yaml       # Multi-provider LLM proxy configuration
```

---

## 3. Library vs Service Decoupling (ASI03: Identity & Privilege Abuse)

To ensure strict adherence to **OWASP Agentic Top 10 (ASI03)**, the core engine has been separated into `libraries:explain-core`.
Third-party platforms (such as enterprise LMS, Canvas, Blackboard, or custom assessment engines) can import `libraries:explain-core` directly and plug in their own IAM, OAuth2/OIDC, and RBAC schemes:

```java
@SpringBootApplication
@EnableExplanationEngine
public class CustomLmsApplication {
    // Implement or bean-register custom EngineSecurityAuthorizer
    @Bean
    public EngineSecurityAuthorizer customAuthorizer() {
        return new MyLmsRoleAuthorizer();
    }
}
```

The bundled `services:explain-service` serves as an **example reference microservice**, illustrating how to propagate caller identity (`EngineCallerPrincipal`) and enforce RBAC policies (e.g., student vs educator vs admin permissions).

---

## 4. Getting Started

### Prerequisites
- **JDK 21** or later
- **Node.js 20+** (for frontend development)
- **Docker & Docker Compose** (for containerized setup)

### Building and Testing

```bash
# Build all backend libraries and services
./gradlew build

# Run unit and integration tests and verify JaCoCo >= 70% coverage threshold
./gradlew check

# Generate aggregated JaCoCo multi-module coverage report
./gradlew codeCoverageReport

# Test and lint the Explanation Player UI
cd packages/explanation-player
npm ci
npm test
npm run lint
npm run build
```

### Running Locally with Docker Compose

1. Copy the environment configuration:
   ```bash
   cp .env.example .env
   ```
2. Start all services:
   ```bash
   docker compose up --build -d
   ```
3. Access the endpoints:
   - **Explain Player UI**: [http://localhost:3000](http://localhost:3000)
   - **OpenAPI Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
   - **Actuator Health & Metrics**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
   - **Prometheus Metrics**: [http://localhost:9090](http://localhost:9090)
   - **Grafana Dashboard**: [http://localhost:3001](http://localhost:3001) (admin/admin)
   - **Jaeger Tracing UI**: [http://localhost:16686](http://localhost:16686)
   - **LiteLLM Gateway**: [http://localhost:4000](http://localhost:4000)

---

## 5. API Reference (Example Reference Service)

### Generate Explanation IR
```http
POST /api/v1/explanations/generate
Content-Type: application/json
X-User-Id: student-1
X-User-Roles: STUDENT
```
```json
{
  "question": {
    "questionId": "q-101",
    "statement": "Solve for x: 3x + 5 = 20",
    "domain": "ALGEBRA",
    "authoritativeAnswer": "5"
  },
  "policy": {
    "level": "INTERMEDIATE",
    "showWhy": true,
    "showVerification": true,
    "animation": "LIGHT"
  }
}
```

### Review Explanation (Requires ROLE_EDUCATOR or ROLE_ADMIN)
```http
POST /api/v1/explanations/{id}/review
Content-Type: application/json
X-User-Id: educator-1
X-User-Roles: EDUCATOR

{
  "approved": true,
  "comment": "Verified pedagogical soundness"
}
```

### Publish Explanation (Requires ROLE_ADMIN)
```http
POST /api/v1/explanations/{id}/publish
Content-Type: application/json
X-User-Id: admin-1
X-User-Roles: ADMIN
```

---

## 6. Repository Standards & Contributing

- [Contributing Guidelines](CONTRIBUTING.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Security Policy & OWASP ASI Compliance](SECURITY.md)
- [Database Migrations Guide](docs/MIGRATIONS.md)
- [Frontend Build Pipeline](docs/FRONTEND_PIPELINE.md)
- [Release Process & SemVer](docs/RELEASING.md)
- [Changelog](CHANGELOG.md)

---

## 7. License

This project is licensed under the [Apache License 2.0](LICENSE).
