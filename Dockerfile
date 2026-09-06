# ==========================================
# Stage 1: Build & Package
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Install bash and dos2unix for gradle wrapper compatibility
RUN apk add --no-cache bash dos2unix

# Copy Gradle wrapper and build configuration first (for layer caching)
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY libraries/explanation-ir/build.gradle ./libraries/explanation-ir/
COPY libraries/math-core/build.gradle ./libraries/math-core/
COPY libraries/reasoning-core/build.gradle ./libraries/reasoning-core/
COPY libraries/validation-core/build.gradle ./libraries/validation-core/
COPY services/explain-service/build.gradle ./services/explain-service/

RUN dos2unix gradlew && chmod +x gradlew

# Download dependencies (offline cache layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy all source files and schemas
COPY schemas ./schemas
COPY libraries ./libraries
COPY services ./services

# Build the Spring Boot executable jar
RUN ./gradlew :services:explain-service:bootJar --no-daemon -x test

# ==========================================
# Stage 2: Minimal Production Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create a non-privileged system user for security
RUN addgroup -S anvaya && adduser -S anvaya -G anvaya && \
    apk add --no-cache curl

# Copy the built jar artifact from builder stage
COPY --from=builder /workspace/services/explain-service/build/libs/explain-service-1.0.0-SNAPSHOT.jar /app/explain-service.jar

USER anvaya:anvaya

# Expose HTTP service port
EXPOSE 8080

# Health check configuration
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Launch JVM with container-aware ergonomics
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/explain-service.jar"]
