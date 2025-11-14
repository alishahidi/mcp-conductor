# 🚀 MCP Conductor

<div align="center">

![MCP Conductor](https://img.shields.io/badge/MCP-Conductor-blue?style=for-the-badge&logo=spring&logoColor=white)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg?style=flat-square)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.0--RC1-blue.svg?style=flat-square)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square)](https://www.oracle.com/java/)
[![MCP](https://img.shields.io/badge/MCP-Compatible-purple.svg?style=flat-square)](https://modelcontextprotocol.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)
[![Production Ready](https://img.shields.io/badge/Production-Ready-green.svg?style=flat-square)]()

**AI-Powered DevOps Automation Server Using Spring AI MCP**  
*Enterprise-grade infrastructure management through natural language*

</div>

---

## 🎯 What is MCP Conductor?

MCP Conductor is a production-ready DevOps automation server that enables **AI assistants to manage your infrastructure through natural language**. Built on Spring Boot and the Model Context Protocol (MCP), it provides secure, validated operations for SSH command execution, Docker management, file operations, and system monitoring.

### 💡 Real-World Impact

Instead of writing complex scripts or memorizing commands, simply tell Claude:
- *"Deploy my Node.js app to production with zero downtime"*
- *"Check disk space across all servers and alert if any are above 80%"*
- *"Scale up my Docker containers and verify health checks"*
- *"Update security patches on staging and restart affected services"*

## ✨ Features

### 🔒 **Enterprise Security**
- **Multi-layer validation**: Command whitelist → Sanitization → Path validation → Execution
- **Comprehensive audit logging**: Every operation tracked with full context
- **Rate limiting**: Token bucket algorithm prevents abuse
- **SSH key authentication**: Secure server access without password exposure

### 🛠️ **Complete DevOps Toolkit** 
- **SSH Command Execution**: Run validated commands on remote servers
- **Docker Management**: Container lifecycle, image management, logs, health checks
- **File Operations**: Read, write, manage files with path traversal protection
- **System Monitoring**: CPU, memory, disk usage, process management
- **Service Management**: Start, stop, restart systemd services
- **Package Management**: Install, update, search packages (apt/yum/dnf)
- **Git Operations**: Clone, pull, checkout, branch management
- **Nginx Configuration**: Virtual host setup, SSL configuration, reverse proxies

### 📊 **Production-Ready Monitoring**
- **Prometheus metrics**: Performance, error rates, usage statistics
- **Health indicators**: SSH connectivity, Docker daemon, system resources
- **Spring Boot Actuator**: Management endpoints, application insights
- **Custom dashboards**: Grafana integration with pre-built dashboards

### ⚡ **High Performance**
- **Connection pooling**: Efficient SSH connection reuse
- **Async operations**: Non-blocking command execution
- **Circuit breakers**: Resilience against service failures
- **Resource optimization**: Minimal memory footprint, fast startup

## 🚀 Quick Start

### Automated Setup (Recommended)

```bash
# Clone the repository
git clone https://github.com/alishahidi/mcp-conductor.git
cd mcp-conductor

# Run the interactive setup script
./setup.sh

# Follow the prompts to configure SSH and generate Claude Desktop config
```

The setup script will:
- ✅ Check Java and Maven installation
- ✅ Build the MCP server
- ✅ Configure SSH connection
- ✅ Generate Claude Desktop configuration
- ✅ Test the server

**📖 For detailed instructions, see [USAGE_GUIDE.md](USAGE_GUIDE.md)**

### Manual Setup

#### Prerequisites
- **Java 21+** (required for Spring Boot 3.4.1)
- **Maven 3.8+** for building
- **SSH access** to target servers
- **Docker** (optional, for container management)

#### 1. Clone and Build
```bash
git clone https://github.com/alishahidi/mcp-conductor.git
cd mcp-conductor

# Set Java environment
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Build the project
mvn clean package -DskipTests
```

#### 2. Configure for Claude Desktop

Copy the example config and update with your paths:

```bash
# Copy the example configuration
cp claude_desktop_config.example.json ~/your-config.json

# Edit with your actual paths and SSH credentials
nano ~/your-config.json
```

Example configuration:
```json
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/mcp-conductor/target/mcp-conductor.jar"],
      "env": {
        "JAVA_HOME": "/usr/lib/jvm/java-21-openjdk-amd64",
        "SSH_DEFAULT_HOST": "your-server.com",
        "SSH_DEFAULT_USERNAME": "your-username",
        "SSH_PRIVATE_KEY_PATH": "/home/user/.ssh/id_rsa"
      }
    }
  }
}
```

#### 3. Add to Claude Desktop

**On macOS:**
```bash
cp ~/your-config.json ~/Library/Application\ Support/Claude/claude_desktop_config.json
```

**On Linux:**
```bash
mkdir -p ~/.config/Claude
cp ~/your-config.json ~/.config/Claude/claude_desktop_config.json
```

**On Windows:**
```powershell
copy your-config.json %APPDATA%\Claude\claude_desktop_config.json
```

#### 4. Restart Claude Desktop

Close and reopen Claude Desktop. You should see the 🔌 icon indicating MCP tools are available.

#### 5. Test with Claude

In Claude Desktop, try asking:
- "List all Docker containers on the server"
- "Check disk space on the server"
- "Show me system stats"

### 4. Test Basic Operations
```bash
# Check application info
curl http://localhost:8080/actuator/info

# View available endpoints
curl http://localhost:8080/actuator

# Test with authentication
curl -u admin:your-password http://localhost:8080/actuator/metrics
```

## 🔧 Available Operations

### 🖥️ **Server Management**
- Execute commands with security validation
- Run complex bash scripts safely
- Monitor system resources (CPU, memory, disk)
- Track running processes
- Manage system services

### 🐳 **Container Operations**
- List and manage Docker containers
- Pull and deploy images
- View container logs and status
- Execute commands inside containers
- Health check monitoring

### 📁 **File Management**
- Read and write configuration files
- List directory contents with filtering
- Manage file permissions securely
- Path traversal protection
- Bulk file operations

### 📦 **Package Management**
- Install software packages (apt/yum/dnf)
- Update system packages
- Search package repositories
- Dependency management

### 🌐 **Web Server Configuration**
- Create Nginx virtual hosts
- Configure SSL certificates
- Set up reverse proxies
- Enable/disable sites
- Configuration validation

### 🔄 **Version Control**
- Clone Git repositories
- Pull latest changes
- Switch branches and tags
- Deploy from repositories

## 📊 Monitoring & Observability

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health with components
curl http://localhost:8080/actuator/health?show-details=always
```

### Metrics
```bash
# Prometheus format metrics
curl http://localhost:8080/actuator/prometheus

# JSON format metrics
curl http://localhost:8080/actuator/metrics
```

### Built-in Dashboards
When using `docker-compose up`:
- **Prometheus** (`:9090`) - Metrics collection
- **Grafana** (`:3000`) - Visualization (admin/admin)
- **MCP Conductor** (`:8080`) - Main application

## 🔒 Security Model

### Defense in Depth
1. **Authentication**: Basic Auth + SSH key validation
2. **Command Validation**: Whitelist-based command checking
3. **Input Sanitization**: Injection attack prevention
4. **Path Validation**: Directory traversal protection
5. **Rate Limiting**: Request throttling per client
6. **Audit Logging**: Complete operation trail

### Security Configuration
```env
# Strict mode (production)
SECURITY_COMMAND_STRICT_MODE=true

# Allowed commands (whitelist)
SECURITY_COMMAND_ALLOWED=ls,ps,df,docker,git,systemctl

# Blocked paths
SECURITY_PATH_BLOCKED=/etc/passwd,/etc/shadow,/root

# Rate limiting
RATE_LIMIT_CAPACITY=100
RATE_LIMIT_REFILL_TOKENS=10
```

## 🐳 Docker Deployment

### Using Docker Compose (Recommended)
```bash
# Start full stack (includes Grafana + Prometheus)
docker-compose up -d

# View logs
docker-compose logs -f mcp-conductor

# Stop services
docker-compose down
```

### Standalone Docker
```bash
# Build image
docker build -t mcp-conductor .

# Run container
docker run -d \
  -p 8080:8080 \
  -v ~/.ssh:/app/.ssh:ro \
  -e SSH_DEFAULT_HOST=your-server \
  -e SSH_DEFAULT_USERNAME=your-user \
  --name mcp-conductor \
  mcp-conductor
```

## 🧪 Testing

### Run Tests
```bash
# Unit tests
mvn test

# Integration tests with coverage
mvn verify jacoco:report

# Specific test classes
mvn test -Dtest=CommandValidatorTest,SecurityTest
```

### Manual Testing
```bash
# Test HTTP mode
curl http://localhost:8080/actuator/health
curl -u admin:password http://localhost:8080/actuator/info

# Test STDIO mode with Claude Code
claude mcp list
# Should show: mcp-conductor: ✓ Connected
```

## 🚀 Production Deployment

### Environment Profiles
- **dev**: Development with debug logging
- **prod**: Production optimized with strict security
- **test**: Testing with mocked dependencies

### Production Checklist
- [ ] Use strong authentication credentials
- [ ] Enable strict command validation
- [ ] Configure rate limiting appropriately
- [ ] Set up monitoring and alerting
- [ ] Enable audit logging
- [ ] Use HTTPS with proper certificates
- [ ] Configure firewall rules
- [ ] Set up log rotation
- [ ] Monitor disk space and performance

### High Availability Setup
```bash
# Load balancer configuration
# Multiple MCP Conductor instances
# Shared configuration storage
# Centralized logging
```

## 🤖 Claude Code Integration

### Quick Setup
```bash
# Register with Claude Code
claude mcp add-json mcp-conductor --scope user '{
  "command": "/path/to/mcp-conductor/scripts/run-stdio-mode.sh",
  "args": [],
  "env": {},
  "description": "AI-powered DevOps automation server"
}'

# Verify connection
claude mcp list
# Should show: mcp-conductor: ✓ Connected
```

### Available Tools in Claude
When connected to Claude Code, you can use natural language to:
- **execute_command** - Run SSH commands on remote servers
- **get_system_info** - Get comprehensive system information
- **docker_list_containers** - List and manage Docker containers
- **file_read** - Read file contents with security validation
- **check_server_health** - Monitor server health and metrics

### Example Usage
In Claude Code, try asking:
- "What's the current health status of the server?"
- "List all running Docker containers"
- "Execute 'df -h' to check disk space"
- "Get system information from the MCP server"

## 📁 Project Structure

```
mcp-conductor/
├── bin/                    # Executable scripts
│   └── mcp-server.py      # MCP STDIO server
├── config/                 # Configuration files
│   ├── example.env        # Environment template
│   ├── http.env          # HTTP mode config
│   └── stdio.env         # STDIO mode config
├── scripts/               # Cross-platform startup scripts
│   ├── run-http-mode.sh   # Linux/macOS HTTP launcher
│   ├── run-http-mode.bat  # Windows HTTP launcher
│   ├── run-stdio-mode.sh  # Linux/macOS STDIO launcher
│   └── run-stdio-mode.bat # Windows STDIO launcher
├── src/                   # Java source code
├── target/                # Compiled artifacts
├── docs/                  # Documentation
├── docker-compose.yml     # Multi-service deployment
├── Dockerfile            # Container definition
└── README.md             # This file
```

## 📚 Documentation

### User Guides
- **[USAGE_GUIDE.md](USAGE_GUIDE.md)** - 📖 **Complete usage guide** with examples and troubleshooting
- **[MCP_SERVER_FIXES.md](MCP_SERVER_FIXES.md)** - Technical details about recent MCP server fixes
- **[.env.example](.env.example)** - Environment variable reference

### Developer Guides
- **[CLAUDE.md](CLAUDE.md)** - Developer guide for working with this repository
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Complete deployment instructions
- **[WORKING_SOLUTION.md](WORKING_SOLUTION.md)** - Implementation details and architecture

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md).

### Development Setup
```bash
# Fork and clone
git clone https://github.com/YOUR_USERNAME/mcp-conductor.git

# Create feature branch
git checkout -b feature/amazing-feature

# Make changes and test
mvn clean test

# Commit with conventional commits
git commit -m "feat: add amazing feature"
```

## 🐛 Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Port 8080 in use | Change `SERVER_PORT` in `config/http.env` |
| SSH connection failed | Verify SSH key path and server access |
| Commands blocked | Check `SECURITY_COMMAND_ALLOWED` in config |
| Docker not available | Install Docker and check permissions |
| Claude Code connection failed | Check `scripts/run-stdio-mode.sh` permissions |
| Python not found | Install Python 3.8+ for STDIO mode |

### Debug Mode
```bash
# Enable debug logging
export LOG_LEVEL=DEBUG
./scripts/run-http-mode.sh
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **[Anthropic](https://anthropic.com)** - For the Model Context Protocol and Claude
- **[Spring AI Team](https://spring.io/projects/spring-ai)** - For MCP server implementation
- **[Spring Boot Community](https://spring.io/projects/spring-boot)** - For the excellent framework
- **DevOps Community** - For feedback and contributions

---

<div align="center">

### 🎉 **Ready to Transform Your DevOps with AI?**

**[⭐ Star this Repository](https://github.com/alishahidi/mcp-conductor/stargazers)** • **[🚀 Get Started](#-quick-start)** • **[🤝 Contribute](#-contributing)**

**Built with ❤️ for the AI-powered infrastructure future**

</div>