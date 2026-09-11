<#
.SYNOPSIS
    Anvaya Prajna AI - Clean Build & Deploy Script for Windows PowerShell
.DESCRIPTION
    Performs a clean build of backend artifacts, builds Docker images,
    and deploys the complete ecosystem using Docker Compose.
.PARAMETER Clean
    Execute clean build (Gradle clean + Docker rebuild) [Default: $true]
.PARAMETER SkipTests
    Skip Gradle test execution during build for faster startup
.PARAMETER WipeData
    Tear down containers and wipe persistent database/redis volumes (docker compose down -v)
.PARAMETER NoCache
    Build Docker images with --no-cache
.PARAMETER BuildOnly
    Build artifacts and container images only; do not start services
.PARAMETER Logs
    Follow container logs after startup (docker compose logs -f)
.EXAMPLE
    .\deploy.ps1
    .\deploy.ps1 -SkipTests
    .\deploy.ps1 -WipeData -SkipTests
    .\deploy.ps1 -NoCache -Logs
#>
[CmdletBinding()]
param(
    [Parameter()][switch]$Clean = $true,
    [Parameter()][switch]$Quick,
    [Parameter()][alias("x")][switch]$SkipTests,
    [Parameter()][alias("w")][switch]$WipeData,
    [Parameter()][switch]$NoCache,
    [Parameter()][alias("b")][switch]$BuildOnly,
    [Parameter()][alias("l")][switch]$Logs
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

if ($Quick) {
    $Clean = $false
}

Write-Host "================================================================" -ForegroundColor Blue
Write-Host "        Anvaya Prajna AI — Clean Build & Deployment             " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Blue

# 1. Environment and Prerequisite Checks
Write-Host "`n[1/5] Checking environment & prerequisites..." -ForegroundColor Cyan

try {
    $dockerVer = docker --version
    Write-Host "✓ Docker CLI found: $dockerVer" -ForegroundColor Green
} catch {
    Write-Error "'docker' command not found. Please ensure Docker Desktop is installed and in your PATH."
    exit 1
}

try {
    $null = docker info 2>&1
    Write-Host "✓ Docker daemon is active." -ForegroundColor Green
} catch {
    Write-Error "Docker daemon is not running. Please launch Docker Desktop."
    exit 1
}

# Check docker compose
$composeCmd = "docker compose"
try {
    $null = docker compose version 2>&1
} catch {
    Write-Error "'docker compose' is required."
    exit 1
}

# Check .env file
if (-not (Test-Path ".env")) {
    if (Test-Path ".env.example") {
        Write-Host "[!] .env not found. Copying from .env.example..." -ForegroundColor Yellow
        Copy-Item ".env.example" ".env"
        Write-Host "✓ Created .env configuration file." -ForegroundColor Green
    } else {
        Write-Host "[!] Warning: Neither .env nor .env.example found." -ForegroundColor Yellow
    }
} else {
    Write-Host "✓ Environment configuration file .env present." -ForegroundColor Green
}

# 2. Teardown existing containers
Write-Host "`n[2/5] Stopping previous containers..." -ForegroundColor Cyan
if ($WipeData) {
    Write-Host "[!] Wiping persistent volumes (Postgres, Redis)..." -ForegroundColor Yellow
    docker compose down -v --remove-orphans
} else {
    docker compose down --remove-orphans
}
Write-Host "✓ Existing containers stopped." -ForegroundColor Green

# 3. Build backend Spring Boot JAR via Gradle
Write-Host "`n[3/5] Building backend JAR artifact..." -ForegroundColor Cyan
$gradleCmd = ".\gradlew.bat"
if (-not (Test-Path $gradleCmd)) {
    $gradleCmd = "gradle"
}

$gradleArgs = @()
if ($Clean) {
    $gradleArgs += "clean"
}
$gradleArgs += ":services:explain-service:bootJar"

if ($SkipTests) {
    $gradleArgs += "-x"
    $gradleArgs += "test"
    Write-Host "[i] Skipping tests for faster deployment." -ForegroundColor Yellow
} else {
    Write-Host "[i] Running tests and building bootJar..." -ForegroundColor Blue
}

Write-Host "Executing: $gradleCmd $($gradleArgs -join ' ')"
& $gradleCmd $gradleArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle build failed with exit code $LASTEXITCODE."
    exit $LASTEXITCODE
}

$jarPath = "services\explain-service\build\libs\explain-service-1.0.0-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    Write-Error "Expected jar file not found: $jarPath"
    exit 1
}
Write-Host "✓ Backend JAR successfully built: $jarPath" -ForegroundColor Green

# 4. Build Docker Images
Write-Host "`n[4/5] Building Docker container images..." -ForegroundColor Cyan
$buildArgs = @("compose", "build")
if ($NoCache) {
    $buildArgs += "--no-cache"
    Write-Host "[i] Building Docker images with --no-cache..." -ForegroundColor Yellow
}

& docker $buildArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker compose build failed."
    exit $LASTEXITCODE
}
Write-Host "✓ Docker images built successfully." -ForegroundColor Green

if ($BuildOnly) {
    Write-Host "`nBuild completed successfully (-BuildOnly active). Exiting." -ForegroundColor Green
    exit 0
}

# 5. Start Docker Compose Stack
Write-Host "`n[5/5] Deploying Docker Compose services..." -ForegroundColor Cyan
docker compose up -d --remove-orphans

Write-Host "`n[i] Waiting for services to initialize..." -ForegroundColor Blue
Start-Sleep -Seconds 5
docker compose ps

Write-Host "`n================================================================" -ForegroundColor Green
Write-Host "       🚀 Anvaya Prajna AI Successfully Deployed!              " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host "  • Explain Player UI:        http://localhost:3000"
Write-Host "  • Explain Service REST API: http://localhost:8080/api/v1/explanations/generate"
Write-Host "  • Spring Boot Actuator:     http://localhost:8080/actuator/health"
Write-Host "  • LiteLLM AI Gateway:       http://localhost:4000/health/liveliness"
Write-Host "  • PostgreSQL Database:      localhost:5432 (db: anvayadb, user: anvaya)"
Write-Host "  • Redis Cache:              localhost:6379"
Write-Host "----------------------------------------------------------------"
Write-Host "Useful commands:"
Write-Host "  - View live logs:      docker compose logs -f"
Write-Host "  - Stop all services:   docker compose down"
Write-Host "  - Wipe data & restart: .\deploy.ps1 -WipeData -SkipTests"
Write-Host "================================================================`n" -ForegroundColor Green

if ($Logs) {
    Write-Host "[i] Following container logs (Ctrl+C to exit)..." -ForegroundColor Blue
    docker compose logs -f
}
