# Anvaya-Prajna-AI — Local Docker Environment

This directory contains the Docker configuration to build and run the complete Anvaya-Prajna backend ecosystem locally with Docker Desktop.

---

## 🏛️ Architecture Overview

```mermaid
graph LR
    Client["Client / Web / Mobile"] -->|"HTTP / REST (8080)"| ExplainService["Explain Service (Spring Boot 3.4.2)"]
    ExplainService -->|"JPA / Flyway (5432)"| Postgres["PostgreSQL 16"]
    ExplainService -->|"Cache / Redis (6379)"| Redis["Redis 7"]
    ExplainService -->|"Spring AI OpenAI API (4000)"| LiteLLM["LiteLLM Proxy (Router & Fallback)"]
    LiteLLM -->|"OpenAI API"| OpenAI["OpenAI (GPT-4o / GPT-4o-mini)"]
    LiteLLM -->|"Anthropic API"| Anthropic["Anthropic (Claude 3.5 Sonnet)"]
    LiteLLM -->|"Gemini API"| Gemini["Google Gemini (1.5 Flash / Pro)"]
    LiteLLM -->|"Groq API"| Groq["Groq (Llama 3.3 70B)"]
    LiteLLM -->|"Ollama API"| Ollama["Local Ollama"]
```

---

## 🚀 Quick Start

### 1. Copy Environment Variables
```bash
cp .env.example .env
```
*(Configure any model API keys you want LiteLLM to route to, e.g. `OPENAI_API_KEY`, `GEMINI_API_KEY`, `ANTHROPIC_API_KEY`, or `GROQ_API_KEY`)*

### 2. Build and Start All Services
```bash
docker compose up --build -d
```

### 3. Check Service Health & Status
```bash
docker compose ps
```

### 4. View Logs
```bash
# All logs
docker compose logs -f

# Explain service logs only
docker compose logs -f explain-service

# LiteLLM proxy logs only
docker compose logs -f litellm
```

---

## 🌐 Endpoints & Ports

| Service | Port | Healthcheck / Description |
|---|---|---|
| **Explain Service** | `http://localhost:8080` | `http://localhost:8080/actuator/health` |
| **LiteLLM Unified AI Gateway** | `http://localhost:4000` | `http://localhost:4000/health/liveliness` |
| **PostgreSQL 16** | `localhost:5432` | `pg_isready -U anvaya -d anvayadb` |
| **Redis 7** | `localhost:6379` | `redis-cli ping` |

---

## 🤖 LiteLLM Proxy Capabilities & Routing

The LiteLLM proxy container provides:
- **Unified OpenAI-compatible endpoint**: Spring AI communicates using standard OpenAI protocol to `http://litellm:4000`.
- **Intelligent Model Routing**: Routes dynamically based on requested models (`gpt-4o`, `gpt-4o-mini`, `claude-3-5-sonnet`, `gemini-1.5-pro`, `gemini-1.5-flash`, `groq-llama-3.3-70b`, `ollama-llama3`).
- **Automated Fallbacks**: Automatically falls back if rate limits or outages occur (e.g. `gpt-4o` -> `gemini-1.5-pro` -> `claude-3-5-sonnet`).
- **Master Key Security**: Protected by `LITELLM_MASTER_KEY` (`sk-litellm-master-key` by default).

### Direct LiteLLM Verification
```bash
curl http://localhost:4000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer sk-litellm-master-key" \
  -d '{
    "model": "gpt-4o-mini",
    "messages": [
      {"role": "user", "content": "Explain Pythagorean theorem in 1 sentence."}
    ]
  }'
```

---

## 🧪 Sample Explanation Service Requests

### Generate Explanation
```bash
curl -X POST http://localhost:8080/api/v1/explanations/generate \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

### Request Progressive Hint
```bash
curl http://localhost:8080/api/v1/explanations/q-101/hints?level=1
```

### Request Step Justification (Why)
```bash
curl "http://localhost:8080/api/v1/explanations/q-101/why?stepId=s1"
```

---

## 🛑 Teardown

```bash
# Stop containers
docker compose down

# Stop containers and wipe persistent volumes
docker compose down -v
```
