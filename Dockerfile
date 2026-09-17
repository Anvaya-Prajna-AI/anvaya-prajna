# ============================================================
# Stage 1: Build Spring Boot JAR with Gradle & OpenJDK 21
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Install bash and dos2unix for cross-platform gradle wrapper compatibility
RUN apk add --no-cache bash dos2unix

# Copy Gradle wrapper and project definitions
COPY gradlew gradlew.bat settings.gradle build.gradle gradle.properties* ./
COPY gradle ./gradle

# Normalize line endings and execute permissions
RUN dos2unix gradlew && chmod +x gradlew

# Copy source modules and schema definitions
COPY schemas ./schemas
COPY libraries ./libraries
COPY services ./services

# Build production Boot JAR (tests can be run separately)
RUN ./gradlew :services:explain-service:bootJar --no-daemon -x test

# ============================================================
# Stage 2: Minimal Production Runtime (Java 21 JRE Alpine)
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for container health checks and create unprivileged user
RUN apk add --no-cache curl && \
    addgroup -S anvaya && adduser -S anvaya -G anvaya

# Copy the built jar artifact from builder stage
COPY --from=builder /workspace/services/explain-service/build/libs/explain-service-1.0.0-SNAPSHOT.jar /app/explain-service.jar

USER anvaya:anvaya

# Expose HTTP service port
EXPOSE 8080

# Health check configuration
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Launch JVM with container-aware ergonomics
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/explain-service.jar"]
