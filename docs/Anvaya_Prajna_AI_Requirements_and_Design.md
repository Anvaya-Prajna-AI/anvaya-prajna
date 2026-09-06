# Anvaya-Prajna-AI — Requirements & System Design

**Project:** Anvaya-Prajna-AI\
**Sanskrit roots:** *Anvaya* (अन्वय — logical sequence, inference) + *Prajna* (प्रज्ञा — wisdom, understanding)\
**Module:** Educational Reasoning & Explanation Engine\
**Document Type:** Software Requirements Specification + Architecture & Design\
**Organization:** [Anvaya-Prajna-AI](https://github.com/Anvaya-Prajna-AI)\
**Status:** Proposed\
**Version:** 1.0\
**License:** Apache 2.0\
**Target:** Open-source MVP → production-ready, embeddable explanation engine

> **Adapted from:** NAG Explain Requirements & System Design (NAG platform — internal).\
> This document is the standalone, platform-agnostic version. All NAG-specific coupling has been removed.

------------------------------------------------------------------------

## 1. Executive Summary

**Anvaya-Prajna-AI** is an open-source educational explanation engine that transforms an
assessment question and its verified solution into a structured,
student-friendly explanation rendered through multiple synchronized representations:

- Natural-language explanation
- Mathematical notation
- Symbols and expressions
- Tables
- Logical/reasoning graphs
- Static diagrams
- Interactive diagrams
- Lightweight text-based animations
- Progressive hints
- Common-mistake (misconception) explanations
- Verification steps

The central design principle is:

> **One verified reasoning model → many synchronized educational representations.**

The system must not depend on an LLM directly generating HTML, SVG,
JavaScript animation code, or arbitrary executable code. Instead, the AI
produces a constrained **Reasoning Proposal**, which is validated,
normalized, and converted into a canonical **Explanation IR
(Intermediate Representation)**.

The Explanation IR is the contract between the reasoning engine and the
presentation layer. Any frontend, any platform, any AI provider can be
swapped independently.

------------------------------------------------------------------------

## 2. Goals

### 2.1 Primary Goals

1. Explain answers rather than merely provide them.
2. Make reasoning visible, step-by-step.
3. Support mathematics, logical reasoning, and diagram-based explanations.
4. Generate multiple representations from one underlying reasoning model.
5. Validate mathematical and logical transformations wherever possible.
6. Provide deterministic rendering from a safe, declarative IR.
7. Support interactive and lightweight animated explanations.
8. Allow students to ask "Why?", "How?", and "Give me a hint".
9. Support different explanation depths based on student level.
10. Integrate naturally with any question or assessment platform via a well-defined API and SDK.
11. Keep the core explanation model open, extensible, and vendor-neutral.
12. Reuse mature open-source visualization and mathematics libraries rather than rebuilding them.

### 2.2 Secondary Goals

- Teacher/content-authoring support
- Explanation versioning
- Explanation quality evaluation
- Misconception tracking
- Explanation analytics
- Localization and multi-language support
- Accessibility (WCAG 2.1 AA)
- Future voice narration
- Future adaptive tutoring

------------------------------------------------------------------------

## 3. Non-Goals for MVP

The MVP will not attempt to:

- Build a general-purpose AI tutor.
- Replace a full symbolic mathematics system.
- Build a general-purpose drawing application.
- Build a full video production engine.
- Generate arbitrary executable JavaScript from an LLM.
- Automatically solve every possible question type.
- Guarantee correctness for domains where no deterministic validator exists.
- Provide a built-in multi-tenant SaaS platform (multi-tenancy is a platform concern; Anvaya-Prajna-AI is embeddable).

------------------------------------------------------------------------

## 4. Target Users

### 4.1 Student

Needs:
- Clear answer
- Step-by-step reasoning
- Visual explanation
- Hints
- Verification
- Ability to replay explanation
- Ability to ask "Why?"

### 4.2 Teacher / Content Author

Needs:
- Review generated explanations
- Edit explanation steps
- Add diagrams and hints
- Identify misconceptions
- Publish approved explanations

### 4.3 Question / Content Author

Needs:
- Associate concepts and expected solution
- Provide authoritative answer
- Optionally provide reasoning constraints

### 4.4 Platform Integrator / Administrator

Needs:
- Configure explanation providers and AI models
- Manage safety policies and explanation quality
- Manage versions
- Integrate Anvaya-Prajna-AI as a library or service

### 4.5 Developer / Contributor

Needs:
- Stable IR schema (public contract)
- Renderer APIs and plugin extension points
- Deterministic, testable behavior
- Domain plugin SDK
- Well-documented schemas

------------------------------------------------------------------------

## 5. Core Design Principle

The architecture is based on four distinct layers:

```text
Question
   |
   v
Reasoning
   |
   v
Explanation IR
   |
   +----> Text Renderer
   +----> Math Renderer
   +----> Diagram Renderer
   +----> Animation Renderer
   +----> Accessibility Renderer
```

**The LLM must never be the final authority for correctness.**

Preferred pipeline:

```text
Question
   |
   v
Question Analyzer
   |
   v
AI Reasoning Planner
   |
   v
Reasoning Proposal
   |
   v
Deterministic Validation
   |
   v
Explanation Compiler
   |
   v
Canonical Explanation IR
   |
   +------------------------------+
   |              |               |
   v              v               v
Text/Math      Diagram        Animation
Renderer       Renderer       Renderer
   |              |               |
   +--------------+---------------+
                  |
                  v
            Student Player
```

------------------------------------------------------------------------

## 6. Functional Requirements

### FR-001 Question Input

The system shall accept:

- Plain text questions
- Structured question objects (via the universal Question Input Schema)
- Multiple-choice questions (MCQ)
- Numerical/fill-in-the-blank questions
- Mathematical expression questions
- Image-based questions (where an upstream OCR/vision service provides text)
- Questions with known authoritative answers
- Questions with authoritative solution steps

### FR-002 Question Classification

The system shall classify a question into one or more domains:

- Mathematics, Algebra, Arithmetic, Geometry
- Probability, Statistics, Data interpretation
- Logical reasoning, Quantitative aptitude
- Physics, Chemistry, Biology
- General reasoning

It shall also identify: concepts, entities, quantities, units, relationships,
constraints, unknowns, and expected answer type.

### FR-003 Reasoning Generation

The system shall generate a structured reasoning proposal containing:
interpretation, facts, assumptions, rules, intermediate results, transformations,
calculations, conclusions, and verification strategy.

### FR-004 Reasoning Validation

The system shall validate:
- Mathematical calculations and equation transformations
- Unit consistency and logical consistency
- References to known facts
- Final answer against the authoritative answer (where available)

Invalid reasoning shall not be published automatically.

### FR-005 Explanation Generation

The system shall generate:
1. Short answer
2. Step-by-step explanation
3. Detailed explanation
4. Progressive hints
5. Verification
6. Common mistakes / misconceptions
7. Why alternatives are wrong (for MCQs)

### FR-006 Multiple Representations

A single explanation step shall be capable of producing:
text, equation/math expression, symbol, table, diagram, graph, highlight,
animation event, verification result.

### FR-007 Interactive Diagrams

The system shall support: points, lines, arrows, circles, rectangles, triangles,
coordinate axes, grids, labels, function graphs, trees, relationship diagrams,
and flow diagrams.

### FR-008 Animation

The system shall support declarative animation actions:
`SHOW`, `HIDE`, `HIGHLIGHT`, `EMPHASIZE`, `FOCUS`, `MOVE`, `DRAW`,
`TRANSFORM`, `REVEAL`, `PAUSE`, `RESET`.

Animations shall reference semantic objects rather than arbitrary pixel coordinates.

### FR-009 Student Interaction

Students shall be able to: step forward/backward, replay, pause, request a hint,
reveal the next step, ask "Why?", ask "How?", request simpler or more detailed explanation.

### FR-010 Misconception Support

An explanation may contain misconception records with:
misconception identifier, incorrect reasoning, why it is wrong, corrected reasoning.

### FR-011 Explanation Versioning

Explanations shall be versioned, tracking:
question version, explanation version, renderer version, reasoning engine version.

### FR-012 Human Review Workflow

```text
DRAFT → VALIDATING → NEEDS_REVIEW → APPROVED → PUBLISHED → DEPRECATED | REJECTED
```

### FR-013 Localization

The explanation model shall separate language content from mathematical/visual
semantics. The same reasoning must be renderable in multiple locales without
rebuilding the reasoning model.

### FR-014 Accessibility

The system shall support: screen-reader-friendly math (MathML/aria-label),
text alternatives for diagrams, keyboard navigation, reduced-motion mode,
and accessible step descriptions.

------------------------------------------------------------------------

## 7. Explanation Model

### 7.1 Explanation Structure

```text
Explanation
|
+-- Problem
+-- Concepts
+-- Facts
+-- Assumptions
+-- Reasoning Graph
+-- Steps
+-- Representations
+-- Verification
+-- Hints
+-- Misconceptions
+-- Metadata
```

### 7.2 Reasoning Step

Every step must be atomic.

```text
ReasoningStep
|
+-- id
+-- type
+-- inputs
+-- operation
+-- outputs
+-- justification
+-- representations
+-- verification
+-- misconception (optional)
```

**Supported step types:**

```text
GIVEN       — state a known fact or given datum
DEFINE      — introduce a variable or concept
OBSERVE     — note an observable relationship
IDENTIFY    — identify a concept or pattern
APPLY_RULE  — apply a mathematical or logical rule
SUBSTITUTE  — substitute a value into an expression
CALCULATE   — perform arithmetic
TRANSFORM   — algebraically transform an expression
COMPARE     — compare two quantities or expressions
ELIMINATE   — eliminate an option (MCQ)
INFER       — draw an inference from facts
CONCLUDE    — state the conclusion
VERIFY      — verify the result
```

------------------------------------------------------------------------

## 8. Explanation IR (Intermediate Representation)

The Explanation IR is the most important component of Anvaya-Prajna-AI.
It is a **public, versioned contract** between the reasoning engine and all renderers.

**Example:**

```json
{
  "schemaVersion": "1.0",
  "explanationId": "exp-001",
  "questionId": "q-001",

  "problem": {
    "statement": "If 3x + 5 = 20, find x."
  },

  "concepts": ["linear-equation", "inverse-operation"],

  "facts": [],

  "steps": [
    {
      "id": "s1",
      "type": "TRANSFORM",
      "operation": {
        "name": "SUBTRACT",
        "target": "both-sides",
        "value": 5
      },
      "before": "3x + 5 = 20",
      "after": "3x = 15",
      "justification": {
        "text": "Subtracting the same value from both sides preserves equality."
      },
      "representations": ["TEXT", "MATH", "HIGHLIGHT", "ANIMATION"]
    },
    {
      "id": "s2",
      "type": "TRANSFORM",
      "operation": {
        "name": "DIVIDE",
        "target": "both-sides",
        "value": 3
      },
      "before": "3x = 15",
      "after": "x = 5",
      "representations": ["TEXT", "MATH", "ANIMATION"]
    }
  ],

  "verification": {
    "type": "SUBSTITUTION",
    "expression": "3 * 5 + 5 = 20",
    "expected": true
  }
}
```

------------------------------------------------------------------------

## 9. Reasoning Graph

The canonical reasoning representation shall support graph relationships.

```text
Fact A
  |
  v
Rule
  |
  +----> Intermediate Result A
  |
  +----> Intermediate Result B
                |
                v
            Conclusion
```

**Node schema:**

```text
id
type         — FACT | RULE | RESULT | CONCLUSION | VERIFICATION
content
source       — GIVEN | AI | DETERMINISTIC
confidence
validationStatus
```

**Edge schema:**

```text
from
to
relationship — DEPENDS_ON | DERIVED_FROM | CONTRADICTS | SUPPORTS | REQUIRES | ELIMINATES | VERIFIES
justification
```

------------------------------------------------------------------------

## 10. Mathematical Representation

The system shall distinguish between **display mathematics** and **semantic mathematics**.

**Preferred internal representation:**

```text
Semantic Math AST
       |
       +----> LaTeX        (display)
       +----> MathML       (accessibility)
       +----> MathJSON     (computation / verification)
       +----> Verification engine
```

**Example — semantic representation:**

```json
{
  "type": "divide",
  "numerator":   { "symbol": "d" },
  "denominator": { "symbol": "t" }
}
```

renders as `v = d / t`.

Storing only a display string (LaTeX) is insufficient; the semantic AST enables
automated verification and cross-format rendering.

------------------------------------------------------------------------

## 11. Diagram IR

Diagram IR shall be **semantic** — describing objects and relationships, not pixel coordinates.

**Example:**

```json
{
  "type": "geometry",
  "objects": [
    { "id": "A", "type": "POINT", "position": [0, 3],  "label": "A" },
    { "id": "B", "type": "POINT", "position": [-2, 0], "label": "B" },
    { "id": "C", "type": "POINT", "position": [2, 0],  "label": "C" }
  ],
  "relationships": [
    { "type": "LINE", "from": "A", "to": "B" },
    { "type": "LINE", "from": "A", "to": "C" },
    { "type": "LINE", "from": "B", "to": "C" }
  ]
}
```

------------------------------------------------------------------------

## 12. Animation IR

Animations shall be **declarative and semantic**.

**Example:**

```json
{
  "timeline": [
    { "at": 0,    "action": "SHOW",      "target": "equation"                       },
    { "at": 1000, "action": "HIGHLIGHT", "target": "term-5"                         },
    { "at": 2000, "action": "TRANSFORM", "target": "equation",
      "operation": "SUBTRACT", "value": 5                                            },
    { "at": 3500, "action": "REVEAL",    "target": "result"                         }
  ]
}
```

Semantic animation (`MOVE(point=A, to=B)`) is always preferred over
coordinate-based animation (`MOVE(x=134, y=87)`).

------------------------------------------------------------------------

## 13. Renderer Architecture

### 13.1 Text Renderer

| Input | Output |
|---|---|
| Explanation IR | Accessible HTML / React components |

### 13.2 Math Renderer

Recommended libraries:
- **MathLive** — editable math input + rich rendering
- **KaTeX** — fast display rendering

Responsibilities: render expressions, editable math where needed,
accessibility (MathML), semantic AST ↔ display notation conversion.

### 13.3 Calculation Engine

- **Math.js** — numerical/symbolic in browser and Node.js
- **SymPy** (optional Python service) — advanced symbolic verification

### 13.4 Diagram Renderer

| Library | Use |
|---|---|
| JSXGraph | Mathematical / geometry interaction |
| SVG | Simple static diagrams |
| Mermaid | Logical / flow diagrams |
| React Flow | Reasoning graph visualization |

### 13.5 Animation Renderer

**MVP:** SVG + CSS transitions, React Spring\
**Optional (later):** Manim Community (server-generated math video)

------------------------------------------------------------------------

## 14. Recommended Open-Source Components

| Component | Purpose | Recommendation |
|---|---|---|
| MathLive | Math input/rendering | Adopt |
| KaTeX | Fast math rendering | Adopt |
| Math.js | Numerical/symbolic calculation | Adopt |
| SymPy | Advanced symbolic verification | Optional service |
| JSXGraph | Interactive geometry | Adopt |
| Mermaid | Logical/flow diagrams | Adopt |
| React Flow | Reasoning graph UI | Adopt |
| Excalidraw | Teacher whiteboard authoring | Later |
| React Spring | Browser animation | Adopt |
| Manim Community | High-quality math video | Later |
| Mathigon | Architectural reference | Study; review license |

> [!IMPORTANT]
> Anvaya-Prajna-AI must **not** reimplement these capabilities unless a specific
> requirement demands it.

------------------------------------------------------------------------

## 15. System Architecture

```text
                     Anvaya-Prajna-AI Engine
                             |
            +----------------+----------------+
            |                                 |
     Question Input API                Explanation Repository
            |
            v
     Question Analyzer
            |
            v
    Reasoning Orchestrator
            |
   +---------+----------+
   |                    |
   v                    v
AI Reasoner        Knowledge / Rules
   |                    |
   +---------+----------+
             |
             v
     Reasoning Proposal
             |
             v
    Validation Pipeline
             |
   +---------+----------+
   |         |          |
   v         v          v
 Math     Logic      Domain
Validator Validator  Validator
   |         |          |
   +---------+----------+
             |
             v
     Explanation Compiler
             |
             v
      Canonical IR
             |
    +--------+--------+---------+
    |        |        |         |
    v        v        v         v
  Text     Math    Diagram   Animation
Renderer  Renderer Renderer  Renderer
    |        |        |         |
    +--------+--------+---------+
             |
             v
        Student Player
```

------------------------------------------------------------------------

## 16. Backend Architecture

**Recommended technology:**

```text
Java 21
Spring Boot 4.1.1
Spring AI 2.0.1
PostgreSQL
Redis
OpenTelemetry
Docker
Kubernetes
```

For MVP, keep the explanation engine as a single deployable module
before decomposing into microservices.

**Package structure:**

```text
ai.anvaya.prajna
|
+-- api
|   +-- ExplanationController
|   +-- HintController
|   +-- ValidationController
|
+-- application
|   +-- ExplanationService
|   +-- ReasoningService
|   +-- ValidationService
|   +-- CompilationService
|
+-- domain
|   +-- Explanation
|   +-- ReasoningGraph
|   +-- ReasoningStep
|   +-- MathExpression
|   +-- Diagram
|   +-- Animation
|
+-- ai
|   +-- ReasoningPlanner
|   +-- PromptRepository
|   +-- ModelRouter
|
+-- validation
|   +-- MathValidator
|   +-- LogicValidator
|   +-- AnswerValidator
|
+-- repository
|   +-- ExplanationRepository
|   +-- TemplateRepository
|   +-- FeedbackRepository
|
+-- compiler
|   +-- ExplanationCompiler
|
+-- renderer
|   +-- RendererRegistry
```

------------------------------------------------------------------------

## 17. AI Architecture

The AI layer shall use **structured output with strict JSON schema**.

```text
System Prompt + Question + Known Answer + Domain Rules + Explanation Policy
       |
       v
LLM (any provider)
       |
       v
Strict JSON Reasoning Proposal
       |
       v
JSON Schema Validation
       |
       v
Domain Validation
```

**The LLM shall not be allowed to:**
- Execute arbitrary code
- Produce arbitrary JavaScript
- Directly manipulate the DOM
- Create unrestricted SVG
- Invoke external systems without declared tools
- Bypass validation

------------------------------------------------------------------------

## 18. AI Model Strategy

The system shall support **model abstraction** via `ModelRouter`:

```text
ModelRouter
|
+-- Local Ollama      (offline / private deployment)
+-- AWS Bedrock
+-- Google Vertex AI / Gemini
+-- OpenAI-compatible provider
+-- Any Spring AI–supported provider
```

Model selection criteria: domain, difficulty, latency, cost, context size,
required reasoning quality.

**Smaller models** handle classification, formatting, simple explanation.\
**Stronger models** handle complex reasoning, ambiguous questions, advanced explanations.\
**Deterministic engines** remain responsible for verification regardless of model.

------------------------------------------------------------------------

## 19. Explanation Policies

An explanation policy controls **pedagogical behavior**.

```json
{
  "level": "BEGINNER",
  "showFormula": true,
  "showEveryCalculation": true,
  "showWhy": true,
  "showVerification": true,
  "showMisconceptions": true,
  "animation": "LIGHT"
}
```

**Supported levels:** `BEGINNER` | `INTERMEDIATE` | `ADVANCED` | `EXAM` | `EXPERT`

Policies are passed in at request time by the integrating platform.

------------------------------------------------------------------------

## 20. Hint Engine

Hints shall be **progressive** — each reveals more without exposing the full answer.

**Example (speed problem):**

```text
Hint 1: "What information do we already know?"
Hint 2: "Which formula connects distance and time?"
Hint 3: "Try: speed = distance / time."
Hint 4: "Substitute 120 for distance and 2 for time."
```

------------------------------------------------------------------------

## 21. "Why?" Engine

Every reasoning step shall optionally expose its justification with recursive depth.

**Example:**

```text
Step: Subtract 5 from both sides.

Why?
→ Performing the same operation on both sides preserves equality.

Why does that help?
→ We want to isolate 3x.

Why?
→ The next operation will be division by 3.
```

A configurable **maximum explanation depth** prevents infinite recursion.

------------------------------------------------------------------------

## 22. MCQ Explanation

For multiple-choice questions:

```text
Question
 |
 +-- Option A  →  ELIMINATE  →  reason why wrong
 +-- Option B  →  ELIMINATE  →  reason why wrong
 +-- Option C  →  CORRECT    →  full proof
 +-- Option D  →  ELIMINATE  →  reason why wrong
```

This structure must be explicit in the IR, not collapsed into prose.

------------------------------------------------------------------------

## 23. Examples

### 23.1 Logical Reasoning

**Question:** A is taller than B. B is taller than C. Who is shortest?

```text
Facts:       A > B,  B > C
Inference:   A > B > C
Conclusion:  C is shortest.

Diagram:
  A  ██████████
  B  ███████
  C  ████

Reasoning Graph:
  A > B  ────┐
             ├──▶ A > B > C ──▶ C is shortest
  B > C  ────┘
```

### 23.2 Algebra

**Question:** Solve 3x + 5 = 20.

```text
Step 1 (TRANSFORM / SUBTRACT both sides by 5):
  3x + 5 − 5 = 20 − 5   →   3x = 15

Step 2 (TRANSFORM / DIVIDE both sides by 3):
  3x / 3 = 15 / 3        →   x = 5

Verification:  3(5) + 5 = 20  ✓
```

### 23.3 Word Problem — Average Speed

**Question:** A train travels 120 km in 2 hours. What is its average speed?

```text
Step 1 (GIVEN):      distance = 120 km,  time = 2 h
Step 2 (IDENTIFY):   concept  = average speed
Step 3 (APPLY_RULE): v = d / t
Step 4 (SUBSTITUTE): v = 120 / 2
Step 5 (CALCULATE):  v = 60 km/h
Step 6 (VERIFY):     60 × 2 = 120 km  ✓

Text:    Average speed is distance divided by time.
Math:    v = d/t
Diagram: A ─────────────── B   (120 km, → 2 h)
```

### 23.4 Geometry

**Question:** Why is the sum of angles of a triangle 180°?

```text
Objects:      A, B, C
Lines:        AB, BC, CA
Construction: Line through A parallel to BC
Reasoning:    Alternate interior angle relationship
Conclusion:   A + B + C = 180°
```

------------------------------------------------------------------------

## 24. Rendering Contract

The frontend shall have **no knowledge** of how an explanation was generated.

```http
GET /api/v1/explain/{questionId}
```

```json
{
  "schemaVersion": "1.0",
  "explanationId": "...",
  "problem": {},
  "concepts": [],
  "steps": [],
  "verification": {},
  "hints": [],
  "misconceptions": []
}
```

**React renderer registry:**

```typescript
RendererRegistry.register("TEXT",            TextRenderer);
RendererRegistry.register("MATH",            MathRenderer);
RendererRegistry.register("DIAGRAM",         DiagramRenderer);
RendererRegistry.register("REASONING_GRAPH", ReasoningGraphRenderer);
RendererRegistry.register("ANIMATION",       AnimationRenderer);
```

------------------------------------------------------------------------

## 25. Frontend Components

```text
ExplanationPlayer
|
+-- ExplanationHeader
+-- ProblemView
+-- ConceptView
+-- StepNavigator  (Previous / Next / Replay)
|
+-- StepRenderer
|   +-- TextRenderer
|   +-- MathRenderer
|   +-- DiagramRenderer
|   +-- GraphRenderer
|   +-- AnimationRenderer
|
+-- HintPanel
+-- WhyPanel
+-- VerificationPanel
+-- MisconceptionPanel
```

------------------------------------------------------------------------

## 26. Animation Player

```text
PLAY | PAUSE | RESET
NEXT | PREVIOUS
SPEED:  0.5× / 1× / 1.5× / 2×
REDUCED_MOTION mode
```

> [!IMPORTANT]
> Animation must **never be required** to understand the solution.
> Text and static representations must always remain sufficient on their own.

------------------------------------------------------------------------

## 27. Persistence Model

**Recommended schema (PostgreSQL):**

```sql
-- Core explanation record
explanation (
  id             UUID          PRIMARY KEY,
  question_id    TEXT          NOT NULL,
  version        INT           NOT NULL,
  status         TEXT          NOT NULL,   -- DRAFT | VALIDATING | NEEDS_REVIEW | APPROVED | PUBLISHED | DEPRECATED | REJECTED
  difficulty     TEXT,
  language       TEXT,
  schema_version TEXT          NOT NULL,
  content_json   JSONB         NOT NULL,
  created_by     TEXT,
  created_at     TIMESTAMPTZ   DEFAULT now(),
  updated_at     TIMESTAMPTZ,
  published_at   TIMESTAMPTZ
)

-- Individual reasoning steps
explanation_step (
  id                UUID   PRIMARY KEY,
  explanation_id    UUID   REFERENCES explanation(id),
  sequence          INT    NOT NULL,
  step_type         TEXT   NOT NULL,
  content_json      JSONB  NOT NULL,
  validation_status TEXT
)

-- Validation run results
explanation_validation (
  id             UUID      PRIMARY KEY,
  explanation_id UUID      REFERENCES explanation(id),
  validator      TEXT      NOT NULL,
  status         TEXT      NOT NULL,
  score          NUMERIC,
  details_json   JSONB,
  created_at     TIMESTAMPTZ DEFAULT now()
)

-- Student / teacher feedback
explanation_feedback (
  id             UUID  PRIMARY KEY,
  explanation_id UUID  REFERENCES explanation(id),
  step_id        UUID,
  user_id        TEXT,
  feedback_type  TEXT  NOT NULL,
  comment        TEXT,
  created_at     TIMESTAMPTZ DEFAULT now()
)

-- Domain-specific templates
explanation_template (
  id             UUID  PRIMARY KEY,
  domain         TEXT  NOT NULL,
  template_type  TEXT  NOT NULL,
  content_json   JSONB NOT NULL,
  version        INT   NOT NULL,
  status         TEXT  NOT NULL
)
```

> [!NOTE]
> Anvaya-Prajna-AI does **not** mandate a built-in `tenant_id`.
> Integrating platforms that require multi-tenancy should add a `context_id` column
> or wrap the API in a tenant-aware proxy.

------------------------------------------------------------------------

## 28. REST API

### Generate Explanation

```http
POST /api/v1/explain
Content-Type: application/json

{
  "questionId": "q-001",
  "mode": "STEP_BY_STEP",
  "language": "en",
  "studentLevel": "INTERMEDIATE"
}
```

### Get Explanation

```http
GET /api/v1/explain/{id}
```

### Get Step

```http
GET /api/v1/explain/{id}/steps/{stepId}
```

### Generate Hint

```http
POST /api/v1/explain/{id}/hints
```

### Explain "Why?"

```http
POST /api/v1/explain/{id}/steps/{stepId}/why
```

### Validate

```http
POST /api/v1/explain/{id}/validate
```

### Review

```http
POST /api/v1/explain/{id}/review
```

### Publish

```http
POST /api/v1/explain/{id}/publish
```

------------------------------------------------------------------------

## 29. Security Requirements

The renderer must treat Explanation IR as **untrusted input**.

Controls:
- JSON Schema validation on all IR
- Allow-listed node types only
- Allow-listed animation actions only
- No arbitrary JavaScript in IR
- No arbitrary HTML injection
- SVG sanitization
- URL allow-listing
- Maximum object counts per diagram/animation
- Maximum animation duration and explanation depth
- Prompt-injection defenses in AI layer
- LLM output must **never** be rendered directly as executable code

------------------------------------------------------------------------

## 30. Performance Requirements

| Metric | Target |
|---|---|
| Cached explanation retrieval | < 200 ms server-side |
| Static rendering (client-side) | < 500 ms for typical explanations |
| First explanation generation | < 10 seconds (model-dependent) |
| Animation | Smooth browser playback (60 fps target) |
| Diagrams | Responsive on standard student devices |

Large explanations shall support lazy loading of steps and representations.

------------------------------------------------------------------------

## 31. Observability

Use **OpenTelemetry** for all telemetry.

**Tracked operations:**

```text
explain.request | explain.generation | explain.validation
explain.render  | explain.hint       | explain.why | explain.replay
```

**Key metrics:**

```text
generation_latency       validation_latency        validation_failure_rate
explain_publish_rate     student_completion_rate   hint_usage_count
why_usage_count          replay_count              explain_error_rate
renderer_error_rate
```

------------------------------------------------------------------------

## 32. Quality Metrics

An explanation shall be evaluated on:

```text
Correctness  |  Completeness  |  Clarity  |  Pedagogical quality
Visual usefulness  |  Conciseness  |  Age appropriateness
Accessibility  |  Verification coverage
```

**Explanation Quality Score (configurable weights):**

```text
EQS = correctness            × 0.35
    + reasoning completeness × 0.20
    + pedagogical clarity    × 0.20
    + verification           × 0.15
    + representation quality × 0.10
```

------------------------------------------------------------------------

## 33. Testing Strategy

### Unit Tests
- IR parsing and schema validation
- Mathematical transformation validation
- Graph construction and animation compilation
- Renderer selection logic

### Property Tests

```text
If transformation is valid:
    evaluate(before) == evaluate(after)
```

### Integration Tests

```text
Question → AI Proposal → Validation → Explanation IR → API → React renderer
```

### Golden Tests

Maintain approved JSON explanations; compare generated IR to golden output.

### Visual Regression Tests

For: equations, diagrams, animations, reasoning graphs.

### Security Tests

Test malicious IR: `<script>` tags, `javascript:` URLs, `data:` URIs,
external SVG with scripts, oversized/deeply recursive structures.

------------------------------------------------------------------------

## 34. Content Authoring Workflow

```text
Question
   ↓
Generate explanation (AI-assisted)
   ↓
AI Draft  →  Automated Validation
   ↓
Teacher Review
   ↓
Edit (text, steps, hints, diagrams, animation timing, misconceptions)
   ↓
Preview  →  Approve  →  Publish
```

------------------------------------------------------------------------

## 35. Explanation Templates

Templates for common problem families ensure consistent reasoning structure.

```text
ALGEBRA_LINEAR_EQUATION    PERCENTAGE_CHANGE       RATIO_PROPORTION
AVERAGE                    TIME_SPEED_DISTANCE     NUMBER_SERIES
SYLLOGISM                  BLOOD_RELATION          DIRECTION_SENSE
RANKING                    SEATING_ARRANGEMENT     PROBABILITY
TRIANGLE_GEOMETRY          DATA_INTERPRETATION
```

Each template provides: expected reasoning structure, available operations,
validation rules, common misconceptions, preferred visual representation.

------------------------------------------------------------------------

## 36. Domain Plugin Architecture

New domains are **pluggable**.

```java
public interface ExplanationDomain {

    /** Returns true if this domain handles the given question. */
    boolean supports(Question question);

    /** Generate an AI-assisted reasoning proposal. */
    ReasoningProposal generateReasoning(Question question);

    /** Deterministically validate the reasoning proposal. */
    ValidationResult validate(ReasoningProposal proposal);

    /** Select optimal representations for the reasoning model. */
    RepresentationPlan planRepresentations(ReasoningModel reasoning);

    /** Generate progressive hints. */
    List<Hint> generateHints(ReasoningModel reasoning);

    /** Detect common misconceptions for this domain. */
    List<Misconception> detectMisconceptions(ReasoningProposal proposal);
}
```

------------------------------------------------------------------------

## 37. Renderer Plugin Architecture

```java
public interface ExplanationRenderer {

    /** Unique renderer type identifier. */
    String type();

    /** Render an explanation (or step) to its target representation. */
    RenderedRepresentation render(ExplanationIR explanation, RenderContext context);
}
```

**Built-in renderer types:**

```text
TEXT  |  MATH  |  TABLE  |  SVG  |  GEOMETRY  |  REASONING_GRAPH
MERMAID  |  ANIMATION  |  AUDIO (future)
```

------------------------------------------------------------------------

## 38. Explanation Compiler

```text
Reasoning Proposal
       ↓
Normalization        — clean and standardize AI proposal
       ↓
Validation           — math / logic / domain validators
       ↓
Pedagogical Planning — sequence steps for student level
       ↓
Representation Planning — assign optimal renderers per step
       ↓
Canonical Explanation IR
```

The compiler shall: remove redundant steps, normalize expressions,
assign stable IDs, build dependency graph, detect unsupported operations,
attach validation metadata, select renderers, create animation timeline.

------------------------------------------------------------------------

## 39. Deterministic vs. AI Responsibilities

| Responsibility | AI | Deterministic |
|---|---|---|
| Question classification | Primary | Optional validation |
| Concept identification | Primary | Rules |
| Reasoning proposal | Primary | — |
| Arithmetic | Assist | **Primary** |
| Symbolic verification | Assist | **Primary** |
| Logical graph validation | Assist | **Primary where possible** |
| Text explanation | **Primary** | Policy checks |
| Diagram planning | Primary | Renderer |
| SVG rendering | No | **Primary** |
| Animation rendering | No | **Primary** |
| Safety validation | Assist | **Primary** |

------------------------------------------------------------------------

## 40. MVP Scope

### Phase 1 — Foundation

- Explanation IR + JSON Schema
- Reasoning steps model
- Text renderer + Math renderer (KaTeX)
- Deterministic math validation (Math.js)
- Basic hint engine + basic "Why?" engine
- REST API + PostgreSQL persistence

**Supported domains:**
1. Algebra (linear equations)
2. Arithmetic
3. Number series
4. Basic logical reasoning (syllogism, ordering)
5. Percentage and ratio

### Phase 2 — Visual

- SVG diagram IR
- JSXGraph integration
- Reasoning graph (React Flow)
- Mermaid flow diagrams
- Animation timeline IR + React animation player

### Phase 3 — Authoring

- Teacher explanation editor
- Diagram authoring
- Review workflow UI
- Explanation versioning + publishing workflow

### Phase 4 — Advanced AI

- Misconception detection
- Adaptive explanations by difficulty
- Personalized hints
- Multi-language explanation generation
- Model routing (difficulty-based)
- Feedback-based learning loop

### Phase 5 — Advanced Media

- Manim video generation (server-side)
- Voice narration + audio synchronization
- Physics visualization

------------------------------------------------------------------------

## 41. Repository Structure

```text
anvaya-prajna/
|
+-- services/
|   +-- explain-service/          — Spring Boot explanation engine
|
+-- libraries/
|   +-- explanation-ir/           — IR domain model (Java)
|   +-- reasoning-core/           — Reasoning graph and compiler
|   +-- math-core/                — Math AST, validation, Math.js bridge
|   +-- validation-core/          — Validation pipeline
|
+-- frontend/
|   +-- explanation-player/       — React student explanation player
|   +-- explanation-authoring/    — Teacher/author editor
|
+-- schemas/
|   +-- explanation-ir.schema.json
|   +-- animation-ir.schema.json
|   +-- diagram-ir.schema.json
|   +-- reasoning-step.schema.json
|   +-- math-expression.schema.json
|
+-- docs/
|   +-- requirements.md
|   +-- architecture.md
|   +-- ir-spec.md
|   +-- plugin-guide.md
|
+-- examples/
|   +-- algebra/
|   +-- logical-reasoning/
|   +-- geometry/
```

> [!IMPORTANT]
> The IR schemas must be treated as **first-class public contracts**.

------------------------------------------------------------------------

## 42. IR Versioning Strategy

Every IR schema must carry a `major.minor` version: `1.0 → 1.1 → 1.2 → 2.0`

**Rules:**
- Minor versions remain backward compatible
- Major versions may introduce breaking changes
- Old explanations must remain renderable on their declared schema version
- Renderer compatibility tracked against schema version

------------------------------------------------------------------------

## 43. Caching Strategy

```text
Question
   |
   +-- Reasoning Proposal cache     (Redis, short TTL)
   +-- Explanation IR cache         (Redis + DB, long TTL)
   +-- Rendered representation cache (CDN edge, per locale)
```

**Cache key:** `question_id + question_version + policy_hash + language + model_version + ir_schema_version`

------------------------------------------------------------------------

## 44. Offline and Local AI Support

Because Anvaya-Prajna-AI is open source, the architecture shall support
**fully local/private deployment**:

```text
Spring AI
   |
   +-- Ollama  (local models, fully offline)
   +-- AWS Bedrock
   +-- Google Vertex AI / Gemini
   +-- OpenAI-compatible API
```

The Explanation IR remains **provider-independent**.

------------------------------------------------------------------------

## 45. Open-Source Strategy

**Core Anvaya-Prajna-AI components (owned, published, versioned):**

```text
Explanation IR schema         — public versioned contract
Reasoning Graph Model         — domain-agnostic
Explanation Compiler          — IR generation pipeline
Pedagogical Planner           — step sequencing, level adaptation
Domain Templates              — pluggable by community
Validation Integration        — Math.js + SymPy + custom
Renderer Contracts / SDK      — pluggable
Student Explanation Player    — React component library
```

**Reused from the OSS ecosystem:**

```text
Math rendering  — MathLive, KaTeX      Symbolic math — SymPy
Geometry        — JSXGraph             Graphs        — React Flow, Mermaid
Animation       — React Spring         Whiteboard    — Excalidraw (later)
```

------------------------------------------------------------------------

## 46. Future Research Direction

### Universal Reasoning Representation

```text
Entities + Facts + Rules + Constraints + Transformations + Evidence + Verification
```

Supporting: Mathematics, Physics, Chemistry, Logic, Economics, Data Interpretation, Programming.

**Long-term vision:**

```text
          Universal Reasoning Model
                   |
  +----------------+----------------+
  |                |                |
  v                v                v
Text             Math           Visual
  |                |                |
  +----------------+----------------+
                   |
              Animation
                   |
                   v
           Adaptive Tutoring
```

------------------------------------------------------------------------

## 47. Success Criteria

The MVP is successful when Anvaya-Prajna-AI can take a supported question and produce:

1. Correct answer
2. Correct reasoning steps
3. Human-readable explanation
4. Mathematical notation where applicable
5. At least one useful visual representation
6. Deterministic verification
7. Progressive hints
8. "Why?" explanation for each major step
9. Replayable step-by-step presentation
10. Human-reviewable, structured Explanation IR

**Target flow:**

```text
Question
   ↓ (< 10 sec generation)
Validated Explanation IR
   ↓
Text + Math + Diagram
   ↓
Interactive student explanation
```

------------------------------------------------------------------------

## 48. Architectural Principles

1. **AI proposes; deterministic systems verify.**
2. **IR is the source of truth for presentation.**
3. **Semantic objects are preferred over pixel coordinates.**
4. **Rendering must be deterministic.**
5. **Every reasoning step must be independently explainable.**
6. **Animations are optional representations, not the reasoning itself.**
7. **Accessibility must be built into the IR.**
8. **Human review must always be supported.**
9. **Domain logic must be pluggable.**
10. **LLM providers must remain replaceable.**
11. **Open-source dependencies are preferred over custom implementations.**
12. **The Explanation IR must be versioned as a public contract.**
13. **Never trust raw model output.**
14. **The system must teach the reasoning process, not merely expose the answer.**

------------------------------------------------------------------------

## 49. Recommended Technology Stack

```text
Backend                    AI Providers (via Spring AI)
-------                    ----------------------------
Java 21                    Ollama  (local / offline)
Spring Boot 3.x            AWS Bedrock
Spring AI                  Google Vertex AI / Gemini
PostgreSQL                 OpenAI-compatible providers
Redis                      Structured JSON output (mandatory)
OpenTelemetry

Mathematics                Visualization             Animation
-----------                -------------             ---------
MathLive                   JSXGraph                  SVG + CSS
KaTeX                      SVG                       React Spring
Math.js                    Mermaid
SymPy (optional)           React Flow                Optional Video
                                                     Manim Community
Frontend                   Deployment
--------                   ----------
React + TypeScript         Docker / Kubernetes / GitHub Actions CI/CD
```

------------------------------------------------------------------------

## 50. Recommended Initial Implementation

Build this **vertical slice** first:

```text
Question
   ↓
Spring AI  →  Structured Reasoning Proposal
   ↓
Anvaya-Prajna Explanation Compiler
   ↓
Explanation IR  →  Math.js validation
   ↓
React  ├── Text
       ├── KaTeX / MathLive
       └── Simple SVG
```

Implement ~20–30 representative questions across 5 domains.\
Then add: JSXGraph, React Flow, Animation IR.\
Only after the IR proves stable should additional renderers be introduced.

------------------------------------------------------------------------

## 51. Key Product Decision

> [!TIP]
> **Anvaya-Prajna-AI is not an LLM answer generator.  
> It is a structured reasoning-to-explanation engine.**

The LLM is one possible reasoning assistant. The durable asset is:

```text
          Question
             |
             v
      Reasoning Model
             |
             v
      Explanation IR
             |
  +----------+----------+
  |          |          |
  v          v          v
Text        Math     Visual
  |          |          |
  +----------+----------+
             |
             v
         Animation
             |
             v
      Adaptive Tutoring
```

This architecture allows the AI model, math engine, diagram library, or
renderer to be **replaced independently** without redesigning the educational
content model.

------------------------------------------------------------------------

## 52. Immediate Engineering Deliverables

| # | Deliverable | Contents |
|---|---|---|
| D1 | Explanation IR Specification | `explanation-ir.schema.json`, `reasoning-step.schema.json`, `math-expression.schema.json`, `diagram.schema.json`, `animation.schema.json` |
| D2 | Java Domain Model | `Explanation`, `ReasoningGraph`, `ReasoningStep`, `MathExpression`, `Diagram`, `AnimationTimeline`, `VerificationResult` |
| D3 | Explanation Compiler | `ReasoningProposal → Validation → Normalization → ExplanationIR` |
| D4 | First React Player | Support: `TEXT`, `MATH`, `HIGHLIGHT`, `STEP`, `VERIFY` |
| D5 | First Five Domains | Algebra, Arithmetic, Number Series, Syllogism, Percentage/Ratio |
| D6 | Visual Layer | SVG, JSXGraph, React Flow, Animation IR |
| D7 | Authoring & Review | Generate → Review → Edit → Validate → Approve → Publish |

------------------------------------------------------------------------

## 53. Conclusion

There is no need to build new mathematical rendering, geometry, graph,
or animation frameworks. The strongest architecture combines mature
open-source components while making the
**Anvaya-Prajna Explanation IR + Reasoning Compiler + Pedagogical Planner**
the central innovation.

The resulting system evolves from:

```text
AI-generated solution
       ↓
AI-assisted reasoning
       ↓
verified reasoning graph
       ↓
pedagogically structured explanation
       ↓
text + symbols + mathematics + diagrams
       ↓
interactive animation
       ↓
adaptive student tutoring
```

That gives the ecosystem a **reusable educational infrastructure**
rather than another chatbot — one that any platform can embed,
extend, and build upon.

------------------------------------------------------------------------

*Anvaya-Prajna-AI — Open Source | Apache 2.0 | [github.com/Anvaya-Prajna-AI](https://github.com/Anvaya-Prajna-AI)*
