# Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apk add --no-cache maven

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install required packages
RUN apk add --no-cache \
    openssh-client \
    git \
    docker-cli \
    curl \
    wget \
    bash

# Create user
RUN addgroup -g 1001 -S mcp && \
    adduser -u 1001 -S mcp -G mcp

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/mcp-devops-commander.jar app.jar

# Create directories
RUN mkdir -p /app/logs /app/config && \
    chown -R mcp:mcp /app

# Switch to non-root user
USER mcp

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose ports
EXPOSE 8080 8081

# Environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]