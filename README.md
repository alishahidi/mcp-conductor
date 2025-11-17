# MCP Conductor

**Manage your Linux infrastructure through natural language using AI**

MCP Conductor is a secure bridge between Claude AI (or any AI assistant) and your Linux servers. Instead of remembering complex commands or writing scripts, just tell Claude what you need in plain English.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![MCP](https://img.shields.io/badge/MCP-Compatible-purple.svg)](https://modelcontextprotocol.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## What Can You Do?

Talk to Claude like you would a DevOps engineer:

- **"Check disk space on all production servers"** → Runs `df -h` on your entire fleet
- **"Restart nginx on the web servers"** → Safely restarts services
- **"Deploy the latest code to staging"** → Pulls from Git and restarts your app
- **"Install Docker on the new server"** → Handles package installation
- **"Show me the top 5 processes by CPU usage"** → Monitors system resources

All with built-in safety checks, audit logs, and rate limiting.

## Key Features

**Security First**
- Multi-layer command validation prevents dangerous operations
- SSH key authentication (no passwords stored)
- Complete audit trail of every action
- Rate limiting to prevent abuse
- Path traversal protection for file operations

**Comprehensive Tools**
- Command execution (single, parallel, or scripts)
- Docker container management
- File operations (read, write, chmod, chown)
- System monitoring (CPU, memory, disk, processes)
- Service management (start, stop, restart)
- Package management (apt, yum, dnf, pacman, zypper, brew)
- Git operations
- Nginx configuration

**Production Ready**
- Connection pooling for performance
- Prometheus metrics integration
- Health check endpoints
- Structured logging
- Exception handling with helpful error messages

## Platform Support

### The MCP Server (This Application)

Can run on:
- ✅ Linux (Ubuntu, CentOS, Fedora, Arch, any distribution)
- ✅ Windows (Windows 10/11, Windows Server)
- ✅ macOS (Intel or Apple Silicon)

The application auto-detects your platform and configures itself accordingly.

### Target Servers (What You Manage)

**Linux servers only:**
- Ubuntu / Debian
- CentOS / RHEL / Rocky / AlmaLinux
- Fedora
- Arch Linux
- openSUSE / SUSE Enterprise
- Any Linux distribution with SSH access

Why Linux only? Because SSH is the standard for Linux management, and we support all major Linux package managers. (Windows servers would need WinRM, which is a different protocol - not currently supported.)

## Quick Start

### Installation

```bash
# Clone the repository
git clone https://github.com/alishahidi/mcp-conductor.git
cd mcp-conductor

# Build with Maven (Java 21 required)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn clean package -DskipTests

# Run the server
java -jar target/mcp-conductor.jar
```

### Configuration

Create a `.env` file with your SSH configuration:

```bash
# Your Linux servers
SSH_DEFAULT_HOST=your-server.example.com
SSH_DEFAULT_PORT=22
SSH_DEFAULT_USERNAME=youruser
SSH_PRIVATE_KEY_PATH=/home/youruser/.ssh/id_rsa

# Multiple servers (optional)
SSH_SERVERS_PRODUCTION_HOST=prod.example.com
SSH_SERVERS_PRODUCTION_USERNAME=admin
SSH_SERVERS_PRODUCTION_KEY=/path/to/prod-key

SSH_SERVERS_STAGING_HOST=staging.example.com
SSH_SERVERS_STAGING_USERNAME=deploy
SSH_SERVERS_STAGING_KEY=/path/to/staging-key
```

### Connect to Claude Desktop

Add to your Claude Desktop configuration (`~/.config/Claude/claude_desktop_config.json` on Linux/Mac, or `%APPDATA%\Claude\claude_desktop_config.json` on Windows):

```json
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": [
        "-jar",
        "/full/path/to/mcp-conductor/target/mcp-conductor.jar"
      ]
    }
  }
}
```

Restart Claude Desktop, and you're ready to go!

## Documentation

Comprehensive documentation is available in the `docs/` folder:

### Getting Started
- **[Getting Started Guide](docs/guides/getting-started.md)** - Installation, configuration, and first steps
- **[Tools Reference](docs/tools/tools-reference.md)** - Complete list of available operations

### Architecture & Development
- **[Architecture Overview](docs/architecture/overview.md)** - How everything works under the hood
- **[Security Guide](docs/security/security-guide.md)** - Security features and best practices

### Deployment
- **[Production Deployment](docs/deployment/production-deployment.md)** - Deploy to production with systemd, Docker, or Kubernetes

### For Contributors
- **[CLAUDE.md](CLAUDE.md)** - Development guidelines for Claude Code users

## Example Use Cases

### Infrastructure Monitoring

> "Show me system stats for all servers"

Claude will gather CPU, memory, disk usage across your fleet and present it in an easy-to-read format.

### Deployment Automation

> "Deploy the main branch to staging, run the database migrations, and restart the application"

Claude executes the deployment steps in order, with safety checks at each stage.

### Incident Response

> "Production server is slow. Show me the top CPU-consuming processes and check disk space"

Claude quickly diagnoses the issue and provides actionable information.

### Configuration Management

> "Install nginx on web-01, create a virtual host for app.example.com, and enable it"

Claude handles package installation and configuration automatically.

### Multi-Server Operations

> "Update all packages on staging servers and restart them one at a time"

Claude coordinates updates across multiple servers safely.

## Security

Security isn't optional - it's built into every operation:

1. **Input Validation** - All commands checked before execution
2. **Command Whitelisting** - Optional strict mode for extra safety
3. **Path Protection** - Prevents directory traversal attacks
4. **Audit Logging** - Every action is logged with full context
5. **Rate Limiting** - Prevents runaway operations
6. **SSH Keys Only** - No password authentication

Dangerous patterns are automatically blocked:
- Disk wipers (`dd if=/dev/zero`)
- Fork bombs (`:(){ :|:& };:`)
- System destruction (`rm -rf /`)
- Sensitive file access (`/etc/shadow`)

See the [Security Guide](docs/security/security-guide.md) for complete details.

## Requirements

- **Java 21 or newer** - The application runs on Java 21
- **Maven 3.8+** - For building (or use provided wrapper)
- **SSH access to Linux servers** - With key-based authentication
- **Docker** (optional) - Only needed for container management features

## Supported Linux Package Managers

MCP Conductor works with all major Linux distributions:

| Package Manager | Distributions | Market Share |
|----------------|---------------|--------------|
| `apt` | Ubuntu, Debian, Mint, Pop!_OS | ~32% |
| `yum` | CentOS/RHEL 6-7 | ~12% |
| `dnf` | CentOS/RHEL 8+, Fedora, Rocky, AlmaLinux | ~12% |
| `pacman` | Arch, Manjaro, EndeavourOS | ~1% |
| `zypper` | openSUSE, SUSE Enterprise | ~2% |
| `brew` | Homebrew on any Linux | Cross-distro |

**Coverage: >95% of production Linux servers**

## Architecture

MCP Conductor follows a clean architecture pattern:

```
Claude Desktop (or any AI)
    ↓ (MCP Protocol)
MCP Server Layer (Spring AI)
    ↓
Tool Layer (8 MCP tools)
    ↓
Service Layer (Business logic)
    ↓
Security Layer (Validation, sanitization, rate limiting)
    ↓
SSH Connection Pool
    ↓
Your Linux Servers
```

Every request passes through multiple security layers before executing.

See [Architecture Overview](docs/architecture/overview.md) for detailed information.

## Monitoring

Built-in observability features:

- **Prometheus Metrics** - Available at `/actuator/prometheus`
  - Request rates per tool
  - Success/failure rates
  - Execution times
  - Connection pool stats

- **Health Checks** - Available at `/actuator/health`
  - SSH connectivity to configured servers
  - Docker daemon availability
  - Application internal state

- **Audit Logs** - All operations logged to `logs/audit.log`
  - Who did what
  - When it happened
  - What the result was

Import the provided Grafana dashboard for visualization.

## Contributing

Contributions are welcome! Here's how you can help:

1. **Report bugs** - Open an issue with details
2. **Suggest features** - Tell us what you'd like to see
3. **Submit PRs** - Fix bugs or add features
4. **Improve docs** - Documentation can always be better

Please read the [Architecture Overview](docs/architecture/overview.md) before contributing code.

## Troubleshooting

### "Failed to connect to SSH server"
- Verify server hostname/IP in `.env`
- Check SSH key permissions: `chmod 600 ~/.ssh/id_rsa`
- Test SSH manually: `ssh -i ~/.ssh/id_rsa user@server.example.com`

### "Permission denied (publickey)"
- Ensure SSH key path in `.env` is correct
- Verify key is in `~/.ssh/authorized_keys` on remote server
- Check you're using the correct username

### "Docker daemon not responding"
- Make sure Docker is running: `sudo systemctl status docker`
- On Windows, ensure Docker Desktop is running
- Check Docker socket path matches your OS

See the [Getting Started Guide](docs/guides/getting-started.md) for more troubleshooting help.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with [Spring AI MCP](https://spring.io/projects/spring-ai) for MCP protocol support
- Uses [JSch](http://www.jcraft.com/jsch/) for SSH connections
- Inspired by the need for safe AI-driven infrastructure management

## Support

- **Documentation**: Check the `docs/` folder
- **Issues**: [GitHub Issues](https://github.com/alishahidi/mcp-conductor/issues)
- **Discussions**: [GitHub Discussions](https://github.com/alishahidi/mcp-conductor/discussions)

---

**Built with ❤️ for DevOps teams who want to work smarter, not harder**
