#!/usr/bin/env bash
# ==============================================================================
# Anvaya Prajna AI - Clean Build & Deploy Script
#
# This script performs a clean build of backend artifacts, builds Docker
# images, and deploys the complete ecosystem using Docker Compose.
# ==============================================================================

set -euo pipefail

# ANSI color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Determine repository root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="${SCRIPT_DIR}"
cd "${ROOT_DIR}"

# Default options
CLEAN_BUILD=true
SKIP_TESTS=false
WIPE_VOLUMES=false
NO_CACHE=false
BUILD_ONLY=false
FOLLOW_LOGS=false
HEALTH_TIMEOUT=60

usage() {
    echo -e "${BOLD}Usage:${NC} ./deploy.sh [OPTIONS]"
    echo -e ""
    echo -e "${BOLD}Options:${NC}"
    echo -e "  -c, --clean         Execute clean build (Gradle clean + Docker rebuild) [Default: true]"
    echo -e "  --quick             Fast incremental build (skips Gradle clean)"
    echo -e "  -x, --skip-tests    Skip Gradle test execution during build for faster startup"
    echo -e "  -w, --wipe-data     Tear down containers and wipe persistent database/redis volumes (docker compose down -v)"
    echo -e "  --no-cache          Build Docker images with --no-cache"
    echo -e "  -b, --build-only    Build artifacts and container images only; do not start services"
    echo -e "  -l, --logs          Follow logs after startup (docker compose logs -f)"
    echo -e "  -h, --help          Show this help message and exit"
    echo -e ""
    echo -e "${BOLD}Examples:${NC}"
    echo -e "  ./deploy.sh                          # Clean build, run tests, build images, and deploy"
    echo -e "  ./deploy.sh -x                       # Clean build skipping tests, deploy"
    echo -e "  ./deploy.sh --quick -x               # Incremental build skipping tests, deploy"
    echo -e "  ./deploy.sh -w -x                    # Clean build, wipe volumes (fresh database), and deploy"
    echo -e "  ./deploy.sh --no-cache -l            # Clean build without Docker cache, follow logs"
    exit 0
}

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        -c|--clean)
            CLEAN_BUILD=true
            shift
            ;;
        --quick)
            CLEAN_BUILD=false
            shift
            ;;
        -x|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -w|--wipe-data)
            WIPE_VOLUMES=true
            shift
            ;;
        --no-cache)
            NO_CACHE=true
            shift
            ;;
        -b|--build-only)
            BUILD_ONLY=true
            shift
            ;;
        -l|--logs)
            FOLLOW_LOGS=true
            shift
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo -e "${RED}[ERROR] Unknown option: $1${NC}"
            usage
            ;;
    esac
done

echo -e "${BLUE}${BOLD}================================================================${NC}"
echo -e "${BLUE}${BOLD}        Anvaya Prajna AI — Clean Build & Deployment             ${NC}"
echo -e "${BLUE}${BOLD}================================================================${NC}"

# Check Docker installation and daemon
echo -e "\n${CYAN}[1/5] Checking environment & prerequisites...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[ERROR] 'docker' command not found. Please install Docker or Docker Desktop.${NC}"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo -e "${RED}[ERROR] Docker daemon is not running. Please start Docker Desktop or the docker daemon.${NC}"
    exit 1
fi

# Detect docker compose CLI command
DOCKER_COMPOSE_CMD=""
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker-compose"
else
    echo -e "${RED}[ERROR] Neither 'docker compose' nor 'docker-compose' was found.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker daemon is active and ${DOCKER_COMPOSE_CMD} is available.${NC}"

# Check .env configuration file
if [ ! -f ".env" ]; then
    if [ -f ".env.example" ]; then
        echo -e "${YELLOW}[!] .env not found. Copying from .env.example with defaults...${NC}"
        cp .env.example .env
        echo -e "${GREEN}✓ Created .env file.${NC}"
    else
        echo -e "${YELLOW}[!] Warning: Neither .env nor .env.example found.${NC}"
    fi
else
    echo -e "${GREEN}✓ Environment configuration file .env present.${NC}"
fi

# Step 2: Teardown existing containers if needed
echo -e "\n${CYAN}[2/5] Stopping previous containers...${NC}"
if [ "$WIPE_VOLUMES" = true ]; then
    echo -e "${YELLOW}[!] Wiping persistent volumes (Postgres, Redis)...${NC}"
    $DOCKER_COMPOSE_CMD down -v --remove-orphans || true
else
    $DOCKER_COMPOSE_CMD down --remove-orphans || true
fi
echo -e "${GREEN}✓ Existing containers stopped.${NC}"

# Step 3: Build backend Spring Boot jar via Gradle
echo -e "\n${CYAN}[3/5] Building backend JAR artifact...${NC}"
GRADLE_CMD="./gradlew"
if [[ "${OSTYPE:-}" == "msys" || "${OSTYPE:-}" == "cygwin" || "${OSTYPE:-}" == "win32" ]]; then
    if [ -f "gradlew.bat" ]; then
        GRADLE_CMD="./gradlew.bat"
    fi
fi
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
fi

GRADLE_TASKS=""
if [ "$CLEAN_BUILD" = true ]; then
    GRADLE_TASKS="clean :services:explain-service:bootJar"
else
    GRADLE_TASKS=":services:explain-service:bootJar"
fi

if [ "$SKIP_TESTS" = true ]; then
    GRADLE_TASKS="${GRADLE_TASKS} -x test"
    echo -e "${YELLOW}[i] Skipping test execution for faster deployment.${NC}"
else
    echo -e "${BLUE}[i] Running tests and generating bootJar...${NC}"
fi

echo -e "Executing: ${BOLD}${GRADLE_CMD} ${GRADLE_TASKS}${NC}"
$GRADLE_CMD $GRADLE_TASKS

JAR_PATH="services/explain-service/build/libs/explain-service-1.0.0-SNAPSHOT.jar"
if [ ! -f "$JAR_PATH" ]; then
    echo -e "${RED}[ERROR] Expected jar file was not generated: ${JAR_PATH}${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Backend JAR successfully built: ${JAR_PATH}${NC}"

# Step 4: Build Docker Images
echo -e "\n${CYAN}[4/5] Building Docker container images...${NC}"
BUILD_ARGS=""
if [ "$NO_CACHE" = true ]; then
    BUILD_ARGS="--no-cache"
    echo -e "${YELLOW}[i] Building images with --no-cache...${NC}"
fi

$DOCKER_COMPOSE_CMD build $BUILD_ARGS
echo -e "${GREEN}✓ Docker images built successfully.${NC}"

if [ "$BUILD_ONLY" = true ]; then
    echo -e "\n${GREEN}${BOLD}Build completed successfully (--build-only flag active). Exiting.${NC}"
    exit 0
fi

# Step 5: Start Docker Compose stack
echo -e "\n${CYAN}[5/5] Deploying Docker Compose services...${NC}"
$DOCKER_COMPOSE_CMD up -d --remove-orphans

echo -e "\n${BLUE}[i] Waiting for services to initialize (timeout: ${HEALTH_TIMEOUT}s)...${NC}"
START_TIME=$(date +%s)
ALL_HEALTHY=false

while true; do
    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))

    # Check container status
    UNHEALTHY_COUNT=$($DOCKER_COMPOSE_CMD ps --format json 2>/dev/null | grep -E '"Health":\s*"unhealthy"' | wc -l || true)
    STARTING_COUNT=$($DOCKER_COMPOSE_CMD ps --format json 2>/dev/null | grep -E '"Health":\s*"starting"' | wc -l || true)

    if [ "$STARTING_COUNT" -eq 0 ] && [ "$UNHEALTHY_COUNT" -eq 0 ]; then
        ALL_HEALTHY=true
        break
    fi

    if [ "$ELAPSED" -ge "$HEALTH_TIMEOUT" ]; then
        echo -e "\n${YELLOW}[!] Healthcheck wait timed out after ${HEALTH_TIMEOUT}s.${NC}"
        break
    fi

    echo -ne "Waiting for services to become healthy... (${ELAPSED}s / ${HEALTH_TIMEOUT}s)\r"
    sleep 3
done

echo ""
$DOCKER_COMPOSE_CMD ps

echo -e "\n${GREEN}${BOLD}================================================================${NC}"
echo -e "${GREEN}${BOLD}       🚀 Anvaya Prajna AI Successfully Deployed!              ${NC}"
echo -e "${GREEN}${BOLD}================================================================${NC}"
echo -e "  • ${BOLD}Explain Player UI:${NC}        http://localhost:3000"
echo -e "  • ${BOLD}Explain Service REST API:${NC} http://localhost:8080/api/v1/explanations/generate"
echo -e "  • ${BOLD}Spring Boot Actuator:${NC}     http://localhost:8080/actuator/health"
echo -e "  • ${BOLD}LiteLLM AI Gateway:${NC}       http://localhost:4000/health/liveliness"
echo -e "  • ${BOLD}PostgreSQL Database:${NC}      localhost:5432 (db: anvayadb, user: anvaya)"
echo -e "  • ${BOLD}Redis Cache:${NC}              localhost:6379"
echo -e "----------------------------------------------------------------"
echo -e "Useful commands:"
echo -e "  - View live logs:      ${BOLD}${DOCKER_COMPOSE_CMD} logs -f${NC}"
echo -e "  - Stop all services:   ${BOLD}${DOCKER_COMPOSE_CMD} down${NC}"
echo -e "  - Wipe data & restart: ${BOLD}./deploy.sh -w -x${NC}"
echo -e "${GREEN}================================================================${NC}\n"

if [ "$FOLLOW_LOGS" = true ]; then
    echo -e "${BLUE}[i] Following container logs (Ctrl+C to exit)...${NC}"
    $DOCKER_COMPOSE_CMD logs -f
fi
