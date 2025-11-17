# Architecture Overview

This document explains how MCP Conductor is designed and how all the pieces fit together. Whether you're trying to understand the codebase or planning to contribute, this should give you a good mental model of the system.

## The Big Picture

MCP Conductor sits between Claude (or any AI assistant) and your Linux infrastructure. Think of it as a translator that converts natural language requests into actual server commands, with lots of safety checks along the way.

```
Claude Desktop
    ↓
MCP Protocol
    ↓
MCP Conductor (this application)
    ↓
SSH Connection
    ↓
Your Linux Servers
```

## Core Components

### 1. MCP Server Layer

This is the entry point for all communication with Claude. We're using Spring AI's MCP implementation, which handles the protocol details for us.

**What it does:**
- Listens for tool invocation requests from Claude
- Automatically discovers all available tools (using `@Tool` annotations)
- Handles request/response formatting
- Manages the communication protocol

**Location:** `config/McpServerConfig.java`

The cool thing here is that we don't have to manually register tools. Spring AI scans for `@Tool` annotations and makes them available automatically.

### 2. Tool Layer

Tools are the actual operations that Claude can perform. Each tool is a Java method that does one specific thing.

**Available tools:**
- `CommandExecutionTool` - Run shell commands on Linux servers
- `DockerManagementTool` - Manage Docker containers
- `FileOperationsTool` - Read, write, and manage files
- `SystemMonitoringTool` - Check CPU, memory, disk usage
- `ServiceManagementTool` - Start/stop/restart services
- `PackageManagementTool` - Install/update/remove packages
- `GitOperationsTool` - Work with Git repositories
- `NginxConfigurationTool` - Configure Nginx

**Location:** `tools/` package

Each tool follows the same pattern:

```java
@Component
public class ExampleTool {
    @Tool("Description for Claude to understand what this does")
    public String doSomething(
        @Parameter("What this parameter is for") String param) {
        // Validation
        // Security checks
        // Do the actual work
        // Return formatted response
    }
}
```

### 3. Service Layer

This is where the real work happens. Services contain the business logic for actually doing things.

**Key services:**
- `SSHService` - Manages SSH connections and command execution
- `DockerService` - Talks to Docker daemon
- `FileService` - Handles file operations
- `SystemService` - Gathers system information
- `PackageService` - Manages Linux packages

**Location:** `service/` package

Services are kept separate from tools so we can reuse logic and test it independently. For example, multiple tools might use `SSHService` to execute commands.

### 4. Security Layer

Security is not an afterthought here - it's baked into every operation. We have multiple layers of protection.

**Components:**
- `CommandValidator` - Checks if commands are allowed
- `PathValidator` - Prevents directory traversal attacks
- `RateLimiter` - Prevents abuse by limiting request frequency
- `AuditLogger` - Records everything that happens

**Location:** `security/` package

Every command goes through validation before execution. If something looks suspicious, it gets blocked and logged.

### 5. Exception Handling

We have a comprehensive exception hierarchy that makes errors meaningful and actionable.

**Base exception:** `McpConductorException`

**Specific exceptions:**
- `SSHConnectionException` - SSH problems
- `DockerException` - Docker issues
- `FileOperationException` - File operation failures
- `CommandExecutionException` - Command execution errors
- `ValidationException` - Input validation failures

**Handler:** `GlobalExceptionHandler`

When something goes wrong, we catch it, log it, record metrics, and return a helpful error message to Claude. No confusing stack traces or cryptic error codes.

## Data Flow

Let's walk through what happens when Claude asks to check disk space on a server:

1. **Claude sends request** → "Check disk space on production server"

2. **MCP Server receives it** → Identifies this as a `get_disk_usage` tool call

3. **Tool method executes** → `SystemMonitoringTool.getDiskUsage("production")`

4. **Validation happens** →
   - Check if "production" server is configured
   - Check rate limits haven't been exceeded
   - Verify user has permissions

5. **Service executes** → `SSHService.executeCommand("production", "df -h")`

6. **SSH connection** →
   - Get connection from pool (or create new one)
   - Execute command on remote server
   - Capture output

7. **Response formatting** → Parse `df` output into readable format

8. **Return to Claude** → Formatted disk usage information

9. **Logging & metrics** → Record the operation, timing, and outcome

All of this happens in a few hundred milliseconds, with multiple safety checks at each step.

## Security Architecture

Security is multi-layered. Even if one layer fails, others catch problems.

### Layer 1: Input Validation

Before anything executes, we validate all inputs:
- Server names must match configured servers
- Commands must be in the whitelist (if strict mode is enabled)
- File paths can't contain `../` or other traversal attempts
- Package names can't contain shell metacharacters

### Layer 2: Command Sanitization

Even after validation, we sanitize inputs:
- Remove null bytes
- Strip carriage returns
- Check for known attack patterns
- Escape special characters when needed

### Layer 3: Rate Limiting

We use a token bucket algorithm to prevent abuse:
- Each client gets a certain number of "tokens"
- Each operation costs tokens
- Tokens refill over time
- If you run out, you have to wait

### Layer 4: Audit Logging

Everything is logged:
- Who did what
- When it happened
- What the result was
- How long it took

These logs are separate from normal application logs and can be used for security audits.

### Layer 5: SSH Security

SSH connections use key-based authentication:
- No passwords in configuration
- Keys are read from secure locations
- Connections are pooled and reused
- Automatic cleanup of stale connections

## Configuration Management

We support different environments with Spring profiles:

- `dev` - Development mode with verbose logging
- `prod` - Production mode with strict security
- `test` - Testing mode with mocked dependencies

Configuration sources (in order of priority):
1. Environment variables
2. `.env` file
3. `application.yml`
4. Profile-specific `application-{profile}.yml`

This means you can override anything without changing code.

## Monitoring & Observability

We expose multiple ways to monitor the application:

### Metrics

Using Micrometer with Prometheus format:
- Request counts per tool
- Execution times
- Error rates
- SSH connection pool stats
- Rate limiting metrics

Access at: `/actuator/prometheus`

### Health Checks

Custom health indicators for:
- SSH connectivity to configured servers
- Docker daemon availability
- Application internal state

Access at: `/actuator/health`

### Performance Monitoring

Using Spring AOP to automatically track:
- Method execution times
- Database query times (if applicable)
- External API calls

## Platform Support

Here's something important to understand about cross-platform support:

### The MCP Server Application

The Spring Boot application itself can run on:
- Linux (any distribution)
- Windows (10/11, Server)
- macOS (Intel or Apple Silicon)

We auto-detect the platform and configure things accordingly (like Docker socket paths).

### Target Servers

The servers you **manage** must be Linux. Why? Because:
- We use SSH for management (standard for Linux)
- All commands are Linux commands (`ps`, `df`, `systemctl`, etc.)
- Package managers are Linux package managers (`apt`, `yum`, `dnf`, etc.)

So you can run MCP Conductor on Windows and manage Linux servers - that works fine. But you can't use it to manage Windows servers (that would need WinRM, which is a different protocol).

## Package Structure

```
net.alishahidi.mcpconductor/
├── config/          # Spring configuration, beans
├── tools/           # MCP tool implementations
├── service/         # Business logic
├── security/        # Security components
├── exception/       # Exception hierarchy
├── model/           # Data models, DTOs
├── util/            # Utility classes
└── aspect/          # AOP aspects (monitoring, etc.)
```

Each package has a clear responsibility. If you're looking for something:
- Adding a new operation? → Start with `tools/`
- Fixing SSH issues? → Look in `service/SSHService`
- Security concerns? → Check `security/`
- Changing configuration? → See `config/`

## Design Patterns

We use several patterns throughout the codebase:

**Dependency Injection** - All components use constructor injection via Lombok's `@RequiredArgsConstructor`

**Builder Pattern** - Complex objects like SSH connections use builders

**Strategy Pattern** - Different package managers use strategy pattern

**Aspect-Oriented Programming** - Cross-cutting concerns like logging and performance monitoring

**Connection Pooling** - SSH connections are pooled and reused

**Token Bucket** - Rate limiting implementation

## Extension Points

Want to add new capabilities? Here's where to hook in:

### Adding a New Tool

1. Create a class in `tools/` package
2. Annotate methods with `@Tool`
3. Add parameters with `@Parameter`
4. Implement logic using existing services
5. Done! Spring AI will discover it automatically

### Adding a New Linux Distribution

1. Add package manager to `PackageService`
2. Add any distro-specific commands
3. Update configuration examples
4. Test thoroughly

### Adding Custom Validation Rules

1. Extend or modify `CommandValidator`
2. Add patterns to check for
3. Update tests
4. Consider adding metrics

## Performance Considerations

We've optimized for typical DevOps use cases:

- **SSH connection pooling** reduces connection overhead
- **Lazy initialization** delays expensive operations
- **Async where appropriate** for non-blocking operations
- **Efficient logging** minimizes performance impact
- **Connection limits** prevent resource exhaustion

Typical operations complete in under 500ms, with most of that being network latency to remote servers.

## Future Enhancements

Some things we're considering (but haven't implemented yet):

- WebSocket support for streaming command output
- Kubernetes management capabilities
- Ansible playbook execution
- More package managers (apk, nix, etc.)
- Windows server management (via WinRM)
- Multi-server operations (run command on N servers in parallel)

If you're interested in contributing any of these, please open an issue first to discuss the approach!

## Summary

MCP Conductor is built as a secure bridge between AI assistants and Linux infrastructure. Every design decision prioritizes safety, usability, and maintainability. The architecture is straightforward - requests come in through MCP, get validated and executed by services, and results go back to Claude.

The code is organized to make it easy to find what you're looking for and add new capabilities. Security is not optional - it's enforced at multiple layers. And everything is logged and monitored so you always know what's happening.
