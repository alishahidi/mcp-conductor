# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
MCP Conductor is an AI-powered DevOps automation server using Spring AI MCP. It provides secure, production-ready tools for SSH command execution, Docker management, file operations, and system monitoring.

## Development Commands

### Building & Testing
```bash
# Set Java 21 environment
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Compile the project
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean compile -q

# Run tests
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -q

# Run specific tests
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -Dtest=CommandValidatorTest,CommandSanitizerTest,ResponseFormatterTest -q

# Build package
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean package -DskipTests -q
```

### Running the Application
```bash
# Run with development profile
SPRING_PROFILES_ACTIVE=dev java -jar target/mcp-conductor.jar

# Run with production profile
SPRING_PROFILES_ACTIVE=prod java -jar target/mcp-conductor.jar
```

### Docker Operations
```bash
# Build Docker image
docker build -t mcp-conductor .

# Run with docker-compose
docker-compose up -d

# View logs
docker-compose logs -f mcp-conductor

# Stop services
docker-compose down
```

## Architecture Overview

### MCP Tool Integration Architecture
The project is built around the Model Context Protocol (MCP) using Spring AI. The core architecture flows:

1. **MCP Server Layer** (`McpServerConfig`) - Configures Spring AI MCP server endpoints
2. **Tool Registry** - MCP tools are auto-discovered via `@Tool` annotations
3. **Service Layer** - Business logic for SSH, Docker, File, Git operations
4. **Security Layer** - Command validation, sanitization, and rate limiting
5. **Infrastructure Layer** - Connection pools, monitoring, health checks

### Core Packages
- `config/` - Spring configuration classes and beans
- `tools/` - MCP tool implementations annotated with `@Tool`
- `service/` - Business logic services (SSH, Docker, File, Git, etc.)
- `exception/` - Custom exception hierarchy with `McpConductorException` base
- `security/` - Security components (validation, audit, rate limiting)
- `util/` - Utility classes (formatters, sanitizers, connection pools)
- `aspect/` - Cross-cutting concerns (performance monitoring)
- `model/` - Data transfer objects and domain models

### Key Architectural Patterns

#### MCP Tool Implementation
All tools follow this pattern:
```java
@Component
public class ExampleTool {
    @Tool("Tool description for AI")
    public String methodName(@Parameter("param description") String param) {
        // Implementation with proper exception handling
        // Security validation via CommandValidator/PathValidator
        // Performance monitoring via aspects
        return ResponseFormatter.success(result);
    }
}
```

#### Exception Handling Strategy
- Base exception: `McpConductorException`
- Domain-specific exceptions: `SSHConnectionException`, `DockerException`, etc.
- Global handler: `GlobalExceptionHandler` with metrics integration
- All exceptions include proper error codes and user-friendly messages

#### Security Architecture
- **Multi-layered validation**: `CommandValidator` → `CommandSanitizer` → `PathValidator`
- **Rate limiting**: Token bucket implementation via `RateLimiter`
- **Audit logging**: All operations logged via `AuditLogger`
- **Connection pooling**: SSH connections managed via `SSHConnectionPool`

#### Monitoring & Observability
- Micrometer metrics with Prometheus export
- Custom health indicators for SSH/Docker connectivity
- Performance monitoring via AOP (`PerformanceMonitoringAspect`)
- Structured logging with audit trails

## Configuration

### Environment Variables
Key configuration from `.env.example`:
- `SPRING_PROFILES_ACTIVE` - Application profile (dev/prod/test)
- `SSH_DEFAULT_HOST`, `SSH_DEFAULT_USERNAME` - SSH defaults
- `DOCKER_HOST` - Docker daemon connection
- `SECURITY_USER`, `SECURITY_PASSWORD` - Basic auth credentials
- `RATE_LIMIT_CAPACITY` - Rate limiting configuration

### Application Profiles
- `dev` - Development with debug logging and relaxed security
- `prod` - Production optimized with strict security
- `test` - Test environment with mocked dependencies

## Security Model

### Command Execution Security
1. **Whitelist Validation** - `CommandValidator` checks against allowed commands
2. **Sanitization** - `CommandSanitizer` prevents injection attacks
3. **Path Protection** - `PathValidator` prevents traversal attacks
4. **Rate Limiting** - Per-client request limiting
5. **Audit Trail** - All commands logged with context

### Authentication Flow
- Basic HTTP auth for API access
- SSH key-based server authentication
- Role-based command restrictions
- Session-based rate limiting

## Development Workflow

### Adding New MCP Tools
1. Create tool class in `tools/` package extending common patterns
2. Annotate methods with `@Tool` and parameters with `@Parameter`
3. Implement proper exception handling with domain-specific exceptions
4. Add security validation appropriate to the tool's scope
5. Include performance monitoring and audit logging
6. Write comprehensive unit and integration tests
7. Update security whitelist if introducing new commands

### Testing Strategy
- Unit tests for all service methods and utilities
- Integration tests for MCP tool endpoints (`ToolsIntegrationTest`)
- Security tests for validation and sanitization
- Docker/SSH integration tests with Testcontainers
- Performance tests for connection pooling and rate limiting

### Error Handling Best Practices
1. Extend `McpConductorException` for domain-specific errors
2. Add handler methods in `GlobalExceptionHandler`
3. Include metrics collection for error rates
4. Provide actionable error messages for AI consumption
5. Maintain error code consistency across tools

## Monitoring & Debugging

### Health Checks
- Application health: `/actuator/health`
- SSH connectivity: Custom health indicator
- Docker daemon: Custom health indicator
- Prometheus metrics: `/actuator/prometheus`

### Performance Monitoring
- Tool execution timing via AOP
- Connection pool statistics
- Rate limiting metrics
- JVM performance metrics
- Custom business metrics per tool

### Debugging
- Set `logging.level.net.alishahidi.mcpconductor=DEBUG` for detailed logs
- Audit logs in `logs/audit.log` for security investigation
- Performance logs include execution context and timing
- Error logs include full stack traces and correlation IDs