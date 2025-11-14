# MCP Conductor Usage Guide

## Table of Contents
1. [Setup with Claude Code](#setup-with-claude-code)
2. [SSH Server Configuration](#ssh-server-configuration)
3. [Using the MCP Tools](#using-the-mcp-tools)
4. [Common Use Cases](#common-use-cases)
5. [Troubleshooting](#troubleshooting)

---

## Setup with Claude Code

### Option 1: Claude Desktop (Recommended)

1. **Build your MCP server first**:
```bash
cd /path/to/mcp-conductor
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn clean package -DskipTests
```

2. **Locate Claude Desktop config file**:

**On macOS**:
```bash
~/Library/Application Support/Claude/claude_desktop_config.json
```

**On Windows**:
```
%APPDATA%\Claude\claude_desktop_config.json
```

**On Linux**:
```bash
~/.config/Claude/claude_desktop_config.json
```

3. **Add MCP Conductor to config**:

```json
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/mcp-conductor/target/mcp-conductor.jar"
      ],
      "env": {
        "JAVA_HOME": "/usr/lib/jvm/java-21-openjdk-amd64",
        "SSH_DEFAULT_HOST": "your-server.com",
        "SSH_DEFAULT_USERNAME": "your-username",
        "SSH_DEFAULT_PORT": "22"
      }
    }
  }
}
```

4. **Restart Claude Desktop**

5. **Verify it's working**:
   - Open Claude Desktop
   - Look for the 🔌 icon (MCP tools indicator)
   - You should see "mcp-conductor" listed

### Option 2: Claude Code CLI

1. **Create MCP config file**:
```bash
mkdir -p ~/.config/claude-code
nano ~/.config/claude-code/mcp_config.json
```

2. **Add configuration**:
```json
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/mcp-conductor/target/mcp-conductor.jar"
      ],
      "env": {
        "SSH_DEFAULT_HOST": "your-server.com",
        "SSH_DEFAULT_USERNAME": "your-username"
      }
    }
  }
}
```

### Option 3: MCP Inspector (Testing)

For testing and debugging your MCP server:

```bash
# Install MCP Inspector
npm install -g @modelcontextprotocol/inspector

# Run inspector with your server
cd /path/to/mcp-conductor
npx @modelcontextprotocol/inspector java -jar target/mcp-conductor.jar
```

This will open a web interface at `http://localhost:6274` where you can:
- See all registered tools
- Test tool execution
- View JSON schemas
- Debug MCP communication

---

## SSH Server Configuration

### Method 1: Environment Variables (Recommended for Single Server)

Set default SSH connection in your MCP config:

```json
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": ["-jar", "/path/to/mcp-conductor/target/mcp-conductor.jar"],
      "env": {
        "SSH_DEFAULT_HOST": "192.168.1.100",
        "SSH_DEFAULT_USERNAME": "admin",
        "SSH_DEFAULT_PORT": "22",
        "SSH_DEFAULT_PASSWORD": "your-password",
        "SSH_PRIVATE_KEY_PATH": "/home/user/.ssh/id_rsa",
        "SSH_PASSPHRASE": "optional-key-passphrase"
      }
    }
  }
}
```

### Method 2: Application Properties (Multiple Servers)

Create `application-prod.yml`:

```yaml
ssh:
  servers:
    production:
      host: prod-server.com
      username: deploy
      port: 22
      privateKeyPath: /home/user/.ssh/prod_key

    staging:
      host: staging-server.com
      username: deploy
      port: 22
      privateKeyPath: /home/user/.ssh/staging_key

    development:
      host: dev-server.local
      username: developer
      port: 22
      password: dev-password

  connection-pool:
    max-size: 10
    min-idle: 2
    max-idle-time: 300000
```

Then run with:
```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/mcp-conductor.jar
```

### SSH Authentication Methods

#### Using Password Authentication
```json
{
  "env": {
    "SSH_DEFAULT_HOST": "server.com",
    "SSH_DEFAULT_USERNAME": "user",
    "SSH_DEFAULT_PASSWORD": "password"
  }
}
```

#### Using Private Key (Recommended)
```json
{
  "env": {
    "SSH_DEFAULT_HOST": "server.com",
    "SSH_DEFAULT_USERNAME": "user",
    "SSH_PRIVATE_KEY_PATH": "/home/user/.ssh/id_rsa"
  }
}
```

#### Using Private Key with Passphrase
```json
{
  "env": {
    "SSH_DEFAULT_HOST": "server.com",
    "SSH_DEFAULT_USERNAME": "user",
    "SSH_PRIVATE_KEY_PATH": "/home/user/.ssh/id_rsa",
    "SSH_PASSPHRASE": "key-passphrase"
  }
}
```

### Setting Up SSH Keys (Best Practice)

1. **Generate SSH key** (if you don't have one):
```bash
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
```

2. **Copy key to your server**:
```bash
ssh-copy-id user@your-server.com
```

3. **Test connection**:
```bash
ssh user@your-server.com
```

4. **Use the key path in MCP config**:
```json
{
  "env": {
    "SSH_DEFAULT_HOST": "your-server.com",
    "SSH_DEFAULT_USERNAME": "user",
    "SSH_PRIVATE_KEY_PATH": "/home/user/.ssh/id_rsa"
  }
}
```

---

## Using the MCP Tools

Once configured, you can use the tools through Claude Code by simply asking!

### Command Execution

**Example conversation with Claude**:

```
You: Execute 'ls -la /var/www' on the server

Claude: [Uses execute_command tool]
Tool: execute_command
Parameters:
  - command: "ls -la /var/www"
  - serverName: "default"
  - useSudo: false

Result: [Shows directory listing]
```

**With sudo**:
```
You: Check system logs using sudo

Claude: [Uses execute_command with sudo]
Tool: execute_command
Parameters:
  - command: "tail -n 50 /var/log/syslog"
  - serverName: "default"
  - useSudo: true
```

### Script Execution

**Example**:
```
You: Run this deployment script on the server:
#!/bin/bash
cd /var/www/myapp
git pull origin main
npm install
pm2 restart myapp

Claude: [Uses execute_script tool]
Tool: execute_script
Parameters:
  - script: [multi-line script]
  - serverName: "default"
  - useSudo: false
  - stopOnError: true
```

### Docker Management

**List containers**:
```
You: Show me all Docker containers

Claude: [Uses docker_list_containers]
Tool: docker_list_containers
Parameters:
  - showAll: true
```

**Run a container**:
```
You: Start a PostgreSQL database container

Claude: [Uses docker_run_container]
Tool: docker_run_container
Parameters:
  - imageName: "postgres:14"
  - containerName: "my-postgres"
  - environment: {"POSTGRES_PASSWORD": "mypassword"}
  - ports: {"5432": "5432"}
  - volumes: ["/var/lib/postgresql/data:/var/lib/postgresql/data"]
```

### File Operations

**Read a file**:
```
You: Show me the nginx config file

Claude: [Uses file_read]
Tool: file_read
Parameters:
  - filePath: "/etc/nginx/nginx.conf"
  - serverName: "default"
```

**Write a file**:
```
You: Create a new config file at /etc/myapp/config.json with this content:
{
  "port": 3000,
  "database": "mongodb://localhost"
}

Claude: [Uses file_write]
```

### Git Operations

**Clone repository**:
```
You: Clone the repository https://github.com/user/repo.git to /var/www/myapp

Claude: [Uses git_clone]
Tool: git_clone
Parameters:
  - repositoryUrl: "https://github.com/user/repo.git"
  - targetPath: "/var/www/myapp"
  - serverName: "default"
```

**Commit and push changes**:
```
You: Commit all changes with message "Update configuration" and push to main

Claude: [Uses git_commit and git_push]
```

### System Monitoring

**Check system stats**:
```
You: What's the current CPU and memory usage?

Claude: [Uses system_stats]
Tool: system_stats
Parameters:
  - serverName: "default"
```

**Check disk usage**:
```
You: Show disk usage

Claude: [Uses disk_usage]
```

**List processes**:
```
You: Show me all running processes

Claude: [Uses process_list]
```

---

## Common Use Cases

### 1. Deploy a Web Application

```
You: Deploy my Node.js app:
1. Clone from https://github.com/user/myapp.git to /var/www/myapp
2. Install dependencies
3. Build the app
4. Restart PM2 process

Claude: I'll help you deploy the application step by step.
[Executes multiple commands using execute_script]
```

### 2. Setup Nginx Server

```
You: Setup Nginx for my app:
1. Install nginx
2. Create a config file for myapp.com pointing to port 3000
3. Enable the site and restart nginx

Claude: [Uses package_install, file_write, and service_restart tools]
```

### 3. Database Backup

```
You: Create a backup of the PostgreSQL database named 'production'

Claude: [Uses execute_command to run pg_dump]
Tool: execute_command
Parameters:
  - command: "pg_dump -U postgres production > /backups/prod_$(date +%Y%m%d).sql"
  - useSudo: true
```

### 4. Monitor Application

```
You: Check if my app is running and show me the logs

Claude:
1. [Uses process_list to find the app]
2. [Uses file_read to show recent logs]
3. [Provides status report]
```

### 5. Docker Stack Deployment

```
You: Deploy this Docker Compose stack:
- PostgreSQL database
- Redis cache
- Node.js API server
All with proper networking and volumes

Claude: [Uses multiple docker_* tools to set up the stack]
```

---

## Available Tools Reference

### Command Execution Tools
- `execute_command` - Run single command
- `execute_script` - Run multi-line script
- `execute_parallel_commands` - Run commands on multiple servers

### Docker Management Tools
- `docker_list_containers` - List containers
- `docker_pull_image` - Pull Docker image
- `docker_run_container` - Run new container
- `docker_stop_container` - Stop container
- `docker_remove_container` - Remove container
- `docker_get_logs` - Get container logs
- `docker_inspect_container` - Inspect container details
- `docker_list_images` - List Docker images
- `docker_remove_image` - Remove Docker image
- `docker_create_network` - Create Docker network
- `docker_list_networks` - List Docker networks
- `docker_execute_command` - Execute command in container

### File Operations Tools
- `file_read` - Read file contents
- `file_write` - Write/create file
- `file_append` - Append to file
- `file_delete` - Delete file
- `file_copy` - Copy file
- `file_move` - Move/rename file
- `file_search` - Search in files
- `file_list_directory` - List directory contents
- `file_create_directory` - Create directory
- `file_delete_directory` - Delete directory
- `file_get_info` - Get file metadata
- `file_set_permissions` - Set file permissions

### Git Operations Tools
- `git_clone` - Clone repository
- `git_pull` - Pull changes
- `git_commit` - Commit changes
- `git_push` - Push to remote
- `git_branch` - Manage branches
- `git_checkout` - Checkout branch
- `git_status` - Get repo status
- `git_log` - View commit history
- `git_diff` - View changes
- `git_tag` - Manage tags

### Service Management Tools
- `service_start` - Start service
- `service_stop` - Stop service
- `service_restart` - Restart service
- `service_status` - Check service status
- `service_enable` - Enable service on boot
- `service_disable` - Disable service on boot
- `service_list` - List all services

### Package Management Tools
- `package_install` - Install packages
- `package_uninstall` - Remove packages
- `package_update` - Update package lists
- `package_upgrade` - Upgrade packages
- `package_search` - Search packages
- `package_list_installed` - List installed packages

### Nginx Configuration Tools
- `nginx_create_site` - Create new site config
- `nginx_enable_site` - Enable site
- `nginx_disable_site` - Disable site
- `nginx_test_config` - Test configuration
- `nginx_reload` - Reload Nginx
- `nginx_get_config` - Get site config
- `nginx_delete_site` - Delete site config

### System Monitoring Tools
- `system_stats` - Get CPU/RAM/disk stats
- `disk_usage` - Check disk usage
- `process_list` - List running processes
- `network_stats` - Network statistics
- `system_uptime` - System uptime
- `log_tail` - Tail log files
- `find_files` - Find files by pattern

---

## Troubleshooting

### Issue: "SSH Connection Failed"

**Possible causes**:
1. Wrong credentials
2. Network connectivity issues
3. SSH key not configured

**Solutions**:
```bash
# Test SSH connection manually
ssh user@your-server.com

# Check SSH key permissions
chmod 600 ~/.ssh/id_rsa

# Verify server is accessible
ping your-server.com

# Check SSH service on server
sudo systemctl status ssh
```

### Issue: "Tool not found" in Claude

**Solution**:
1. Restart Claude Desktop
2. Check MCP config file is valid JSON
3. Verify server is running:
```bash
# Test server manually
java -jar target/mcp-conductor.jar
# Look for initialization logs
```

### Issue: "Permission Denied" errors

**Solution**:
1. Use `useSudo: true` for privileged operations
2. Configure sudo on your server:
```bash
# Add user to sudoers
sudo usermod -aG sudo your-username

# Or configure passwordless sudo
sudo visudo
# Add: your-username ALL=(ALL) NOPASSWD: ALL
```

### Issue: "Rate limit exceeded"

**Solution**:
Adjust rate limiting in `application.yml`:
```yaml
security:
  rate-limit:
    capacity: 100        # Increase from default
    refill-rate: 10      # Tokens per second
    refill-period: 1000  # Milliseconds
```

### Issue: "Command timeout"

**Solution**:
For long-running commands, use background execution or increase timeout:
```yaml
command:
  timeout: 600  # 10 minutes in seconds
```

---

## Security Best Practices

### 1. Use SSH Keys Instead of Passwords
```bash
# Generate key
ssh-keygen -t ed25519 -C "mcp-conductor"

# Use in config
"SSH_PRIVATE_KEY_PATH": "/path/to/private/key"
```

### 2. Restrict Commands
Edit `application.yml`:
```yaml
security:
  command-whitelist:
    - "ls"
    - "cat"
    - "docker"
    - "git"
    - "npm"
    - "pm2"

  blocked-patterns:
    - "rm -rf /"
    - "dd if="
    - ":(){ :|:& };:"
```

### 3. Use Dedicated Service Account
```bash
# On your server, create dedicated user
sudo useradd -m -s /bin/bash mcp-conductor
sudo usermod -aG docker mcp-conductor

# Grant specific sudo permissions
sudo visudo
# Add: mcp-conductor ALL=(ALL) NOPASSWD: /usr/bin/systemctl, /usr/bin/docker
```

### 4. Enable Audit Logging
```yaml
logging:
  file:
    name: /var/log/mcp-conductor/audit.log
  level:
    net.alishahidi.mcpconductor.security: DEBUG
```

### 5. Firewall Configuration
```bash
# Only allow SSH from specific IPs
sudo ufw allow from 192.168.1.0/24 to any port 22
sudo ufw enable
```

---

## Advanced Configuration

### Multiple SSH Servers

Create `servers.yml`:
```yaml
servers:
  production:
    host: prod.example.com
    username: deploy
    privateKey: /keys/prod_key

  staging:
    host: staging.example.com
    username: deploy
    privateKey: /keys/staging_key

  database:
    host: db.example.com
    username: dbadmin
    privateKey: /keys/db_key
    port: 2222
```

### Custom Docker Configuration
```yaml
docker:
  host: unix:///var/run/docker.sock
  tls-verify: true
  cert-path: /path/to/certs
  registry:
    url: registry.example.com
    username: ${DOCKER_REGISTRY_USER}
    password: ${DOCKER_REGISTRY_PASS}
```

### Performance Tuning
```yaml
performance:
  connection-pool:
    ssh:
      max-size: 20
      min-idle: 5
      max-wait: 30000

  command:
    timeout: 300
    buffer-size: 8192

  cache:
    enabled: true
    ttl: 300  # 5 minutes
```

---

## Getting Help

### View Server Logs
```bash
# If running in foreground
java -jar target/mcp-conductor.jar

# View specific log levels
LOGGING_LEVEL_ROOT=DEBUG java -jar target/mcp-conductor.jar
```

### Enable Debug Mode
```json
{
  "env": {
    "LOG_LEVEL": "DEBUG",
    "MCP_LOG_LEVEL": "DEBUG"
  }
}
```

### Test Individual Tools
Use MCP Inspector:
```bash
npx @modelcontextprotocol/inspector java -jar target/mcp-conductor.jar
```

---

## Example Claude Conversations

### Deploying an Application
```
You: I need to deploy my Next.js application to the server:
- Clone from https://github.com/myuser/myapp
- Install dependencies
- Build for production
- Configure Nginx reverse proxy on port 3000
- Start with PM2

Claude: I'll help you deploy your Next.js application. Let me break this down:

1. First, I'll clone the repository...
   [Uses git_clone tool]

2. Installing dependencies...
   [Uses execute_command: "npm install"]

3. Building for production...
   [Uses execute_command: "npm run build"]

4. Setting up Nginx...
   [Uses nginx_create_site tool]

5. Starting with PM2...
   [Uses execute_command: "pm2 start npm --name myapp -- start"]

Your application is now deployed and running!
```

### System Maintenance
```
You: Perform system maintenance:
- Update all packages
- Clean Docker images
- Check disk space
- Backup important files to /backups

Claude: I'll perform the maintenance tasks:

1. Updating packages...
   [Uses package_update and package_upgrade]

2. Cleaning Docker images...
   [Uses docker_* tools to remove unused images]

3. Checking disk space...
   [Uses disk_usage tool]

4. Creating backups...
   [Uses file_copy for important files]

Maintenance completed! Here's a summary...
```

---

## Next Steps

1. **Test your setup**: Use MCP Inspector to verify all tools work
2. **Configure security**: Set up SSH keys and command whitelists
3. **Add multiple servers**: Configure server profiles for different environments
4. **Monitor usage**: Enable audit logging and review regularly
5. **Customize**: Adjust rate limits and timeouts based on your needs

For more help, check the project documentation or create an issue on GitHub.
