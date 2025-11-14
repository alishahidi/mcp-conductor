# 🚀 Claude Code Integration Guide

Complete guide for adding MCP Conductor to Claude Code (Web/CLI)

## ✅ Your Server Status

Based on your logs, your MCP server is **running successfully**:
```
✅ 54 tools registered
✅ STDIO mode enabled
✅ Spring Boot 3.3.6
✅ Spring AI 1.1.0-RC1
✅ MCP protocol ready
```

---

## 📋 Quick Setup for Claude Code

### Option 1: Using Claude CLI (Recommended)

```bash
# Add your MCP server to Claude Code
claude mcp add --transport stdio mcp-conductor --scope user -- \
  java -jar /absolute/path/to/mcp-conductor/target/mcp-conductor.jar
```

**Important**: Replace `/absolute/path/to` with your actual path!

### Option 2: With Environment Variables (SSH Password)

```bash
claude mcp add-json mcp-conductor --scope user '{
  "command": "java",
  "args": [
    "-jar",
    "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"
  ],
  "env": {
    "JAVA_HOME": "/usr/lib/jvm/java-1.21.0-openjdk-amd64",
    "SSH_DEFAULT_HOST": "your-server.com",
    "SSH_DEFAULT_USERNAME": "your-username",
    "SSH_DEFAULT_PASSWORD": "your-password",
    "SSH_DEFAULT_PORT": "22"
  }
}'
```

### Option 3: With SSH Key (More Secure)

```bash
claude mcp add-json mcp-conductor --scope user '{
  "command": "java",
  "args": [
    "-jar",
    "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"
  ],
  "env": {
    "JAVA_HOME": "/usr/lib/jvm/java-1.21.0-openjdk-amd64",
    "SSH_DEFAULT_HOST": "your-server.com",
    "SSH_DEFAULT_USERNAME": "your-username",
    "SSH_PRIVATE_KEY_PATH": "/home/ali/.ssh/id_rsa"
  }
}'
```

---

## 🔧 Configuration Scopes

Choose the right scope for your needs:

### Local Scope (Default)
```bash
# Only available in current project
claude mcp add --transport stdio mcp-conductor -- java -jar /path/to/mcp-conductor.jar
```

### User Scope (Recommended)
```bash
# Available across all your projects
claude mcp add --transport stdio mcp-conductor --scope user -- \
  java -jar /path/to/mcp-conductor.jar
```

### Project Scope (Team Sharing)
```bash
# Stored in .mcp.json for team sharing
claude mcp add --transport stdio mcp-conductor --scope project -- \
  java -jar /path/to/mcp-conductor.jar
```

---

## 🔑 SSH Authentication Methods

### Method 1: Username + Password

**Configuration:**
```bash
claude mcp add-json mcp-conductor --scope user '{
  "command": "java",
  "args": ["-jar", "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"],
  "env": {
    "SSH_DEFAULT_HOST": "192.168.1.100",
    "SSH_DEFAULT_USERNAME": "admin",
    "SSH_DEFAULT_PASSWORD": "your_password_here"
  }
}'
```

**Security Note**: Passwords in config files are less secure. Consider using SSH keys for production.

### Method 2: SSH Key (Recommended)

**Configuration:**
```bash
claude mcp add-json mcp-conductor --scope user '{
  "command": "java",
  "args": ["-jar", "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"],
  "env": {
    "SSH_DEFAULT_HOST": "your-server.com",
    "SSH_DEFAULT_USERNAME": "your-username",
    "SSH_PRIVATE_KEY_PATH": "/home/ali/.ssh/id_rsa"
  }
}'
```

### Method 3: SSH Key with Passphrase

**Configuration:**
```bash
claude mcp add-json mcp-conductor --scope user '{
  "command": "java",
  "args": ["-jar", "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"],
  "env": {
    "SSH_DEFAULT_HOST": "your-server.com",
    "SSH_DEFAULT_USERNAME": "your-username",
    "SSH_PRIVATE_KEY_PATH": "/home/ali/.ssh/id_rsa",
    "SSH_PASSPHRASE": "key-passphrase"
  }
}'
```

---

## 📁 Manual Configuration (.mcp.json)

If you prefer to edit configuration manually:

### 1. Create/Edit .mcp.json

**For User Scope:**
```bash
# Location varies by OS:
# Linux: ~/.config/claude-code/.mcp.json
# macOS: ~/Library/Application Support/Claude Code/.mcp.json
# Windows: %APPDATA%\Claude Code\.mcp.json

nano ~/.config/claude-code/.mcp.json
```

**For Project Scope:**
```bash
# Create in your project root
nano .mcp.json
```

### 2. Add Configuration

**With SSH Password:**
```json
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": [
        "-jar",
        "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"
      ],
      "env": {
        "JAVA_HOME": "/usr/lib/jvm/java-1.21.0-openjdk-amd64",
        "SSH_DEFAULT_HOST": "your-server.com",
        "SSH_DEFAULT_USERNAME": "your-username",
        "SSH_DEFAULT_PASSWORD": "your-password",
        "SSH_DEFAULT_PORT": "22"
      }
    }
  }
}
```

**With SSH Key:**
```json
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": [
        "-jar",
        "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"
      ],
      "env": {
        "JAVA_HOME": "/usr/lib/jvm/java-1.21.0-openjdk-amd64",
        "SSH_DEFAULT_HOST": "your-server.com",
        "SSH_DEFAULT_USERNAME": "your-username",
        "SSH_PRIVATE_KEY_PATH": "/home/ali/.ssh/id_rsa"
      }
    }
  }
}
```

---

## ✅ Verify Installation

### 1. List MCP Servers
```bash
claude mcp list
```

**Expected output:**
```
mcp-conductor: ✓ Connected
```

### 2. Get Server Details
```bash
claude mcp get mcp-conductor
```

### 3. Test in Claude Code

Open Claude Code and try these commands:

```
You: List all Docker containers on my server

You: Check disk space on the server

You: Show me system stats
```

---

## 🎯 Complete Setup Example (Your Path)

Based on your setup, here's the exact command to use:

```bash
claude mcp add-json mcp-conductor --scope user '{
  "command": "java",
  "args": [
    "-jar",
    "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"
  ],
  "env": {
    "JAVA_HOME": "/usr/lib/jvm/java-1.21.0-openjdk-amd64",
    "SSH_DEFAULT_HOST": "YOUR_SERVER_IP_OR_HOSTNAME",
    "SSH_DEFAULT_USERNAME": "YOUR_USERNAME",
    "SSH_DEFAULT_PASSWORD": "YOUR_PASSWORD"
  }
}'
```

**Replace:**
- `YOUR_SERVER_IP_OR_HOSTNAME` - Your SSH server address (e.g., `192.168.1.100` or `server.example.com`)
- `YOUR_USERNAME` - Your SSH username (e.g., `ali` or `ubuntu`)
- `YOUR_PASSWORD` - Your SSH password

---

## 🔄 Management Commands

### View All Servers
```bash
claude mcp list
```

### Remove Server
```bash
claude mcp remove mcp-conductor
```

### Update Configuration
```bash
# Remove old config
claude mcp remove mcp-conductor

# Add new config
claude mcp add-json mcp-conductor --scope user '{...}'
```

### Test Connection
```bash
claude mcp get mcp-conductor
```

---

## 🐛 Troubleshooting

### Issue: "Server not found"

**Check if server is added:**
```bash
claude mcp list
```

**If not listed, add it:**
```bash
claude mcp add-json mcp-conductor --scope user '{...}'
```

### Issue: "Connection failed"

**Test your SSH connection manually:**
```bash
ssh username@your-server.com
```

**Check server is running:**
```bash
ps aux | grep mcp-conductor
```

**View server logs:**
Run the JAR manually and check for errors:
```bash
java -jar /home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar
```

### Issue: "Command not found"

**Verify Java path:**
```bash
which java
# Or
echo $JAVA_HOME
```

**Use absolute Java path if needed:**
```bash
claude mcp add-json mcp-conductor --scope user '{
  "command": "/usr/lib/jvm/java-1.21.0-openjdk-amd64/bin/java",
  "args": ["-jar", "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"],
  "env": {...}
}'
```

### Issue: "Tools not showing in Claude Code"

1. **Restart Claude Code**
2. **Check server status:**
   ```bash
   claude mcp list
   ```
3. **Look for the tools icon** in Claude Code interface
4. **Try using `/mcp` command** in Claude Code

---

## 📚 Available Tools (54 Total)

Once configured, you can ask Claude Code to:

### Server Management
- Execute commands on remote servers
- Run shell scripts
- Monitor system resources
- Manage services (start/stop/restart)

### Docker Operations
- List/start/stop containers
- Pull/remove images
- View container logs
- Execute commands in containers

### File Operations
- Read/write files
- List directories
- Search file contents
- Manage permissions

### Git Operations
- Clone repositories
- Commit and push changes
- Branch management
- View history

### System Monitoring
- CPU/RAM usage
- Disk space
- Process list
- Network statistics

---

## 🔒 Security Best Practices

### 1. Use SSH Keys Instead of Passwords

```bash
# Generate SSH key
ssh-keygen -t ed25519 -C "mcp-conductor"

# Copy to server
ssh-copy-id username@your-server.com

# Use in config (NO password needed)
"SSH_PRIVATE_KEY_PATH": "/home/ali/.ssh/id_ed25519"
```

### 2. Use Environment Variables for Secrets

Create a wrapper script:
```bash
#!/bin/bash
export SSH_DEFAULT_HOST="server.com"
export SSH_DEFAULT_USERNAME="user"
export SSH_DEFAULT_PASSWORD="$MY_SSH_PASSWORD"
java -jar /home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar
```

Then use:
```bash
claude mcp add --transport stdio mcp-conductor --scope user -- \
  /path/to/wrapper-script.sh
```

### 3. Restrict Command Whitelist

Edit `application-dev.yml`:
```yaml
security:
  command:
    allowed:
      - ls
      - docker
      - git
      # Add only commands you need
```

---

## 🎉 Quick Start Script

Save this as `setup-claude-code.sh`:

```bash
#!/bin/bash

# Your configuration
SERVER_HOST="your-server.com"
SERVER_USER="your-username"
SERVER_PASS="your-password"
JAR_PATH="/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"
JAVA_HOME="/usr/lib/jvm/java-1.21.0-openjdk-amd64"

# Add to Claude Code
claude mcp add-json mcp-conductor --scope user "{
  \"command\": \"java\",
  \"args\": [
    \"-jar\",
    \"$JAR_PATH\"
  ],
  \"env\": {
    \"JAVA_HOME\": \"$JAVA_HOME\",
    \"SSH_DEFAULT_HOST\": \"$SERVER_HOST\",
    \"SSH_DEFAULT_USERNAME\": \"$SERVER_USER\",
    \"SSH_DEFAULT_PASSWORD\": \"$SERVER_PASS\"
  }
}"

# Verify
echo "Checking installation..."
claude mcp list

echo "Done! Try asking Claude Code to list Docker containers."
```

Make it executable and run:
```bash
chmod +x setup-claude-code.sh
./setup-claude-code.sh
```

---

## 📖 Next Steps

1. **Add your server to Claude Code** using one of the methods above
2. **Verify it's working** with `claude mcp list`
3. **Test with simple commands** like "list Docker containers"
4. **Explore the 54 available tools** in Claude Code
5. **Check USAGE_GUIDE.md** for more examples

---

## 🆘 Need Help?

- **Documentation**: See [USAGE_GUIDE.md](USAGE_GUIDE.md)
- **Quick Reference**: See [QUICKSTART.md](QUICKSTART.md)
- **Technical Details**: See [MCP_SERVER_FIXES.md](MCP_SERVER_FIXES.md)
- **Test manually**: `java -jar target/mcp-conductor.jar`

**Your MCP server is ready! Just add it to Claude Code and start automating! 🚀**
