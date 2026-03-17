# =============================================================================
# Wallet Service - Multi-Stage Dockerfile
# =============================================================================
# Java Version: 17
# Build Tool: Maven
# Base Image: Eclipse Temurin (Adoptium)
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build Stage
# -----------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy Maven configuration files first (for better layer caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn/

# Download dependencies (cached if pom.xml unchanged)
RUN ./mvnw dependency:go-offline -B -q

# Copy source code
COPY src ./src

# Build the application
# -DskipTests: Skip tests during Docker build (run tests in CI pipeline)
# -B: Batch mode (less output)
# -q: Quiet mode
RUN ./mvnw clean package -DskipTests -B -q

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage (Production Image)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre AS runtime

# Labels for image metadata
LABEL maintainer="PlastWallet Team"
LABEL version="0.0.1-SNAPSHOT"
LABEL description="PlastWallet - Digital Wallet Service with Clean Architecture"
LABEL organization="PlastWallet"

# Create non-root user for security
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -r appuser -G appgroup

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

# Create directories for logs and temp files
RUN mkdir -p /app/logs /app/temp && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# JVM optimization flags for containerized environments
# -XX:+UseContainerSupport: Enable container-aware memory settings
# -XX:MaxRAMPercentage: Use percentage of available memory (75% default)
# -XX:+UseG1GC: Use G1 garbage collector (better for containers)
# -XX:MaxGCPauseMillis: Target max GC pause time
# -XX:+HeapDumpOnOutOfMemoryError: Create heap dump on OOM
# -XX:HeapDumpPath: Path for heap dumps
# -Djava.security.egd: Use /dev/urandom for faster random generation
# -Dserver.port: Application port
# -Djava.awt.headless=true: Headless mode (no GUI)

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/app/logs \
               -Djava.security.egd=file:/dev/./urandom \
               -Dserver.port=8080 \
               -Djava.awt.headless=true"

# Health check endpoint (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Entry point with JVM options
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# -----------------------------------------------------------------------------
# Stage 3: Debug Image (Optional - for debugging production issues)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre AS debug

LABEL maintainer="PlastWallet Team"
LABEL version="0.0.1-SNAPSHOT"
LABEL description="PlastWallet - Debug version with JDWP enabled"

RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -r appuser -G appgroup

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

RUN mkdir -p /app/logs /app/temp && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080
EXPOSE 5005

# Debug JVM options with JDWP (Java Debug Wire Protocol)
# -agentlib:jdwp: Enable JDWP agent
# -transport=dt_socket: Use socket transport
# -server=y: Listen for debugger connection
# -suspend=n: Don't wait for debugger on startup
# -address=*:5005: Listen on all interfaces, port 5005

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
               -Djava.security.egd=file:/dev/./urandom \
               -Dserver.port=8080"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
