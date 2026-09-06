# Anvaya-Prajna-AI — Local Docker Environment

This directory contains the Docker configuration to build and run the complete Anvaya-Prajna backend ecosystem locally with Docker Desktop.

---

## 🚀 Quick Start

### 1. Copy Environment Variables
```bash
cp .env.example .env
```
*(Optional: Provide your `OPENAI_API_KEY` if you want live LLM generation instead of deterministic domain engines)*

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
```

---

## 🌐 Endpoints & Ports

| Service | Port | Healthcheck / Description |
|---|---|---|
| **Explain Service** | `http://localhost:8080` | `http://localhost:8080/actuator/health` |
| **PostgreSQL 16** | `localhost:5432` | `pg_isready -U anvaya -d anvayadb` |
| **Redis 7** | `localhost:6379` | `redis-cli ping` |

---

## 🧪 Sample Verification Request

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

---

## 🛑 Teardown

```bash
# Stop containers
docker compose down

# Stop containers and wipe persistent volumes
docker compose down -v
```
