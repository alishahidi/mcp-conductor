# 🚀 MCP DevOps Commander

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.1-blue.svg)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![MCP](https://img.shields.io/badge/MCP-Compatible-purple.svg)](https://modelcontextprotocol.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> Enterprise-grade Model Context Protocol (MCP) server for AI-powered DevOps automation built with Spring Boot and Spring AI

## ✨ Features

### 🛠️ DevOps Tools
- **Command Execution**: Secure SSH command execution with validation
- **Docker Management**: Full container lifecycle management
- **Package Management**: Multi-platform package installation (apt, yum, brew)
- **Service Control**: Systemd service management
- **File Operations**: Secure file manipulation with path validation
- **Git Operations**: Repository management and deployment
- **Nginx Configuration**: Web server setup and management
- **System Monitoring**: Real-time resource monitoring

### 🔒 Security Features
- Command validation and sanitization
- Path traversal prevention
- Rate limiting per client
- Comprehensive audit logging
- SSH key authentication
- Role-based access control
- Secure credential management

### 🏗️ Enterprise Features
- Connection pooling for SSH
- Async/reactive operations
- Circuit breaker pattern
- Retry mechanisms
- Health checks and metrics
- Distributed tracing
- Prometheus metrics export
- Docker & Kubernetes ready

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker (optional)
- SSH access to target servers
- Claude Desktop or compatible MCP client

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/mcp-devops-commander.git
cd mcp-devops-commander
```

### 2. Configure Environment
```bash
cp .env.example .env
# Edit .env with your server details
```

### 3. Build the Application
```bash
mvn clean package
```

### 4. Run the Application

#### Using Java:
```bash
java -jar target/mcp-devops-commander.jar
```

#### Using Docker:
```bash
docker-compose up -d
```

#### Using Kubernetes:
```bash
kubectl apply -f k8s/
```

### 5. Configure Claude Desktop

Add to `~/.claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "devops-commander": {
      "url": "http://localhost:8080",
      "transport": "sse"
    }
  }
}
```

## 📖 Configuration

### Application Properties

Key configuration in `application.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: mcp-devops-commander
        type: ASYNC  # or SYNC

ssh:
  servers:
    production:
      host: prod.example.com
      username: deploy
      private-key-path: /path/to/key
    staging:
      host: staging.example.com
      username: deploy

docker:
  host: unix:///var/run/docker.sock

security:
  command:
    strict-mode: true
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `SSH_DEFAULT_HOST` | Default SSH host | `localhost` |
| `SSH_DEFAULT_USERNAME` | Default SSH username | `root` |
| `SSH_PRIVATE_KEY_PATH` | Path to SSH private key | - |
| `DOCKER_HOST` | Docker daemon URL | `unix:///var/run/docker.sock` |
| `SECURITY_USER` | Basic auth username | `admin` |
| `SECURITY_PASSWORD` | Basic auth password | `admin` |

## 🔧 Available MCP Tools

### Command Execution
```
Execute shell commands on remote servers
Parameters: command, serverName, useSudo
```

### Docker Management
```
Manage Docker containers and images
Operations: list, pull, run, stop, remove, logs, exec
```

### Package Management
```
Install and manage system packages
Supports: apt, yum, dnf, pacman, brew
```

### File Operations
```
Read, write, delete, and manage files
Includes: permissions, ownership, directory listing
```

### Git Operations
```
Clone, pull, checkout, commit repositories
Full git workflow support
```

### Service Management
```
Control systemd services
Operations: start, stop, restart, enable, disable
```

### Nginx Configuration
```
Create and manage nginx sites
Supports: reverse proxy, SSL, custom configs
```

### System Monitoring
```
Get system information
Includes: CPU, memory, disk, network stats
```

## 📊 Monitoring & Metrics

### Prometheus Metrics
Available at `/actuator/prometheus`

### Grafana Dashboard
Access at `http://localhost:3000` (when using docker-compose)

### Health Checks
- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

## 🔒 Security Best Practices

1. **Use SSH Keys**: Always prefer key-based authentication
2. **Enable Rate Limiting**: Configure appropriate rate limits
3. **Audit Logging**: Review audit logs regularly
4. **Command Validation**: Keep strict mode enabled in production
5. **Path Restrictions**: Configure allowed/blocked paths appropriately
6. **Network Security**: Use VPN or private networks
7. **Credential Management**: Use secrets management tools

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test with Claude Desktop
1. Start the server
2. Open Claude Desktop
3. Try: "List all Docker containers on my server"

## 📦 Deployment

### Docker Deployment
```bash
docker build -t mcp-devops-commander .
docker run -p 8080:8080 mcp-devops-commander
```

### Kubernetes Deployment
```bash
kubectl create namespace mcp-system
kubectl apply -f k8s/
```

### Production Considerations
- Use external configuration management
- Enable TLS/SSL
- Configure proper resource limits
- Set up monitoring and alerting
- Implement backup strategies

## 🤝 Usage Examples

### With Claude Desktop

**Example 1: Deploy Application**
```
"Deploy my Node.js app from GitHub to production server"
```

**Example 2: Docker Setup**
```
"Set up PostgreSQL and Redis containers with persistent storage"
```

**Example 3: System Maintenance**
```
"Check system resources and update all packages on staging server"
```

**Example 4: Nginx Configuration**
```
"Configure nginx reverse proxy for my app running on port 3000"
```

## 📚 API Documentation

When running, API documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/v3/api-docs`

## 🐛 Troubleshooting

### Connection Issues
- Verify SSH credentials and connectivity
- Check firewall rules
- Ensure proper network configuration

### Permission Denied
- Verify user has sudo privileges
- Check file/directory permissions
- Review security configuration

### Rate Limiting
- Adjust rate limit configuration
- Implement request batching
- Use connection pooling

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## 🔗 Links

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Specification](https://modelcontextprotocol.io)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com)

## 👥 Support

For issues and questions:
- Create an issue on GitHub
- Check existing documentation
- Review closed issues for solutions

## 🏆 Acknowledgments

- Spring AI team for MCP support
- Anthropic for the MCP specification
- Spring Boot community

---

Built with ❤️ for the DevOps community