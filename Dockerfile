# ==========================================
# Minimal Production Runtime (Java 21 Alpine)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for container health checks
RUN apk add --no-cache curl && \
    addgroup -S anvaya && adduser -S anvaya -G anvaya

# Copy the built jar artifact
COPY services/explain-service/build/libs/explain-service-1.0.0-SNAPSHOT.jar /app/explain-service.jar

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
