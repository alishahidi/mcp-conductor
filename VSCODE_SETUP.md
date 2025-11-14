# 🚀 VS Code Integration Guide (GitHub Copilot / GPT Codex)

Complete guide for using MCP Conductor with VS Code, GitHub Copilot, and OpenAI Codex

## 📋 Prerequisites

- **VS Code 1.102+** (with MCP support)
- **GitHub Copilot** subscription OR **OpenAI Codex** extension
- **MCP Conductor** built and running (you already have this! ✅)

## ✅ Your Server Status

Your MCP server is ready:
```
✅ 54 tools registered
✅ STDIO mode enabled
✅ Java 21 with Spring Boot 3.3.6
✅ Spring AI MCP 1.1.0-RC1
```

---

## 🎯 Method 1: Workspace Configuration (.vscode/mcp.json)

**Best for:** Sharing with your team via Git

### 1. Create Configuration File

In your project root, create `.vscode/mcp.json`:

```bash
mkdir -p .vscode
nano .vscode/mcp.json
```

### 2. Add MCP Conductor Configuration

**With SSH Password:**
```json
{
  "servers": {
    "mcp-conductor": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"
      ],
      "env": {
        "JAVA_HOME": "/usr/lib/jvm/java-1.21.0-openjdk-amd64",
        "SSH_DEFAULT_HOST": "your-server.com",
        "SSH_DEFAULT_USERNAME": "your-username",
        "SSH_DEFAULT_PASSWORD": "${input:ssh-password}"
      }
    }
  },
  "inputs": [
    {
      "type": "promptString",
      "id": "ssh-password",
      "description": "SSH Password for remote server",
      "password": true
    }
  ]
}
```

**With SSH Key (More Secure):**
```json
{
  "servers": {
    "mcp-conductor": {
      "type": "stdio",
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

### 3. Reload VS Code

- Press `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac)
- Type "Developer: Reload Window"
- Press Enter

### 4. Trust the MCP Server

VS Code will show a trust dialog for the MCP server. Click **"Allow"**.

---

## 🎯 Method 2: User-Level Configuration (Global)

**Best for:** Personal use across all projects

### 1. Open Command Palette

Press `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac)

### 2. Add Server Command

Type: **"MCP: Add Server"**

### 3. Fill in the Details

When prompted, enter:

```
Server Name: mcp-conductor
Type: stdio
Command: java
Arguments: -jar /home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar
```

### 4. Add Environment Variables

```
JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64
SSH_DEFAULT_HOST=your-server.com
SSH_DEFAULT_USERNAME=your-username
SSH_DEFAULT_PASSWORD=your-password
```

---

## 🎯 Method 3: Using Environment File (.env)

**Best for:** Keeping credentials secure

### 1. Create .env File

```bash
# .vscode/mcp.env
JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64
SSH_DEFAULT_HOST=your-server.com
SSH_DEFAULT_USERNAME=your-username
SSH_DEFAULT_PASSWORD=your-password
SSH_DEFAULT_PORT=22
```

### 2. Configure mcp.json to Use .env

```json
{
  "servers": {
    "mcp-conductor": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"
      ],
      "envFile": "${workspaceFolder}/.vscode/mcp.env"
    }
  }
}
```

### 3. Add .env to .gitignore

```bash
echo ".vscode/mcp.env" >> .gitignore
```

---

## 🎯 Method 4: Dev Container Support

**Best for:** Containerized development environments

Add to `.devcontainer/devcontainer.json`:

```json
{
  "name": "My Project",
  "image": "mcr.microsoft.com/devcontainers/base:ubuntu",
  "mcp": {
    "servers": {
      "mcp-conductor": {
        "type": "stdio",
        "command": "java",
        "args": [
          "-jar",
          "/workspace/target/mcp-conductor.jar"
        ],
        "env": {
          "SSH_DEFAULT_HOST": "your-server.com",
          "SSH_DEFAULT_USERNAME": "your-username"
        }
      }
    }
  }
}
```

---

## 🔧 Using MCP Tools in VS Code

### 1. In GitHub Copilot Chat

Click the **Tools** icon (🔨) in the chat input, or:

```
@workspace List all Docker containers on the server using mcp-conductor

@workspace Check disk space on my remote server

@workspace Show me system stats from the server
```

### 2. Using # Notation

```
Can you #mcp-conductor execute 'docker ps' on the server?

#mcp-conductor check the disk usage on /var
```

### 3. In Agent Mode

Enable agent mode and ask:

```
Deploy my app to the server:
1. Pull latest code
2. Build Docker image
3. Restart containers
```

Copilot will automatically use mcp-conductor tools!

---

## 🎯 For OpenAI Codex Extension

### 1. Install Codex Extension

Install from VS Code marketplace: **"OpenAI Codex"**

### 2. Configure MCP Server

Same as above - Codex uses the same `.vscode/mcp.json` format:

```json
{
  "servers": {
    "mcp-conductor": {
      "type": "stdio",
      "command": "java",
      "args": ["-jar", "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"],
      "env": {
        "SSH_DEFAULT_HOST": "your-server.com",
        "SSH_DEFAULT_USERNAME": "your-username",
        "SSH_DEFAULT_PASSWORD": "${input:ssh-password}"
      }
    }
  },
  "inputs": [
    {
      "type": "promptString",
      "id": "ssh-password",
      "description": "SSH Password",
      "password": true
    }
  ]
}
```

### 3. Use with GPT-5-Codex

In Codex settings, select **GPT-5-Codex** model (optimized for coding).

### 4. Access MCP Tools

Use `codex.list_mcp_resources` or ask Codex directly:

```
Use mcp-conductor to list Docker containers
```

---

## 📝 Complete Example Configuration

Here's a production-ready configuration:

```json
{
  "servers": {
    "mcp-conductor": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"
      ],
      "env": {
        "JAVA_HOME": "/usr/lib/jvm/java-1.21.0-openjdk-amd64",
        "SSH_DEFAULT_HOST": "${input:ssh-host}",
        "SSH_DEFAULT_USERNAME": "${input:ssh-username}",
        "SSH_DEFAULT_PASSWORD": "${input:ssh-password}",
        "SSH_DEFAULT_PORT": "22",
        "SPRING_PROFILES_ACTIVE": "dev"
      }
    }
  },
  "inputs": [
    {
      "type": "promptString",
      "id": "ssh-host",
      "description": "SSH Server Host",
      "default": "192.168.1.100"
    },
    {
      "type": "promptString",
      "id": "ssh-username",
      "description": "SSH Username",
      "default": "ali"
    },
    {
      "type": "promptString",
      "id": "ssh-password",
      "description": "SSH Password",
      "password": true
    }
  ]
}
```

---

## ✅ Verify Installation

### 1. Check MCP Status

Open Command Palette (`Ctrl+Shift+P`) and type:
```
MCP: Show Status
```

You should see `mcp-conductor` listed as **Connected** ✅

### 2. View Available Tools

In Copilot Chat, click the **Tools** icon (🔨). You should see:
- execute_command
- docker_list_containers
- file_read
- git_clone
- system_stats
- ...and 49 more tools!

### 3. Test with Simple Command

In Copilot Chat:
```
@workspace Use mcp-conductor to list Docker containers
```

---

## 🐛 Troubleshooting

### Issue: "MCP server not found"

**Solution 1: Check configuration path**
```bash
cat .vscode/mcp.json
```

**Solution 2: Reload window**
Press `Ctrl+Shift+P` → "Developer: Reload Window"

**Solution 3: Reset MCP trust**
Press `Ctrl+Shift+P` → "MCP: Reset Trust"

### Issue: "Command not found: java"

**Solution: Use absolute path**
```json
{
  "command": "/usr/lib/jvm/java-1.21.0-openjdk-amd64/bin/java"
}
```

### Issue: "Connection failed to SSH server"

**Test SSH manually:**
```bash
ssh username@your-server.com
```

**Check MCP server logs:**
1. Open Output panel: `Ctrl+Shift+U`
2. Select "MCP" from dropdown
3. Look for connection errors

### Issue: "Tools not showing in Copilot"

**Enable MCP gallery:**
1. Open Settings: `Ctrl+,`
2. Search: `chat.mcp.gallery.enabled`
3. Check the box

**Restart VS Code completely**

### Issue: "Password prompt not appearing"

**Use environment file instead:**
```json
{
  "envFile": "${workspaceFolder}/.vscode/mcp.env"
}
```

---

## 🔒 Security Best Practices

### 1. Use SSH Keys Instead of Passwords

```bash
# Generate key
ssh-keygen -t ed25519 -C "mcp-conductor"

# Copy to server
ssh-copy-id username@your-server.com
```

Then use:
```json
{
  "env": {
    "SSH_PRIVATE_KEY_PATH": "/home/ali/.ssh/id_ed25519"
  }
}
```

### 2. Use Input Variables for Secrets

Never hardcode passwords in `mcp.json`:
```json
{
  "env": {
    "SSH_DEFAULT_PASSWORD": "${input:ssh-password}"
  }
}
```

### 3. Add .env to .gitignore

```bash
echo ".vscode/mcp.env" >> .gitignore
echo ".vscode/*.env" >> .gitignore
```

### 4. Use Workspace Trust

Enable workspace trust in VS Code settings to prevent unauthorized MCP server execution.

---

## 📋 Available Tools (54 Total)

Once configured, these tools are available in Copilot/Codex:

### Command Execution
- `execute_command` - Run SSH commands
- `execute_script` - Multi-line scripts
- `execute_parallel_commands` - Parallel execution

### Docker Management
- `docker_list_containers`
- `docker_run_container`
- `docker_stop_container`
- `docker_get_logs`
- ...and 8 more

### File Operations
- `file_read`, `file_write`, `file_search`
- `file_list_directory`
- `file_set_permissions`
- ...and 7 more

### Git Operations
- `git_clone`, `git_pull`, `git_commit`
- `git_branch`, `git_checkout`
- ...and 5 more

### System Monitoring
- `system_stats` - CPU/RAM/Disk
- `disk_usage`
- `process_list`
- `network_stats`
- ...and 3 more

Plus: Service management, package management, and Nginx configuration tools!

---

## 🎨 Example Use Cases

### Deploy Application
```
@workspace Deploy my Node.js app:
1. Clone from GitHub
2. Install dependencies
3. Build Docker image
4. Start containers
5. Check health
```

### Monitor System
```
@workspace Check server health:
- CPU and memory usage
- Disk space
- Running containers
- Active services
```

### Manage Docker
```
@workspace Clean up Docker:
1. Stop unused containers
2. Remove old images
3. Prune volumes
4. Show remaining resources
```

---

## 📚 Additional Resources

- **[CLAUDE_CODE_SETUP.md](CLAUDE_CODE_SETUP.md)** - Claude Code integration
- **[USAGE_GUIDE.md](USAGE_GUIDE.md)** - Complete usage guide
- **[QUICKSTART.md](QUICKSTART.md)** - Quick reference
- **[MCP_SERVER_FIXES.md](MCP_SERVER_FIXES.md)** - Technical details

---

## 🚀 Quick Setup (TL;DR)

### 1. Create .vscode/mcp.json
```json
{
  "servers": {
    "mcp-conductor": {
      "type": "stdio",
      "command": "java",
      "args": ["-jar", "/home/ali/IdeaProjects/mcp-conductor/target/mcp-conductor.jar"],
      "env": {
        "JAVA_HOME": "/usr/lib/jvm/java-1.21.0-openjdk-amd64",
        "SSH_DEFAULT_HOST": "YOUR_SERVER",
        "SSH_DEFAULT_USERNAME": "YOUR_USERNAME",
        "SSH_DEFAULT_PASSWORD": "YOUR_PASSWORD"
      }
    }
  }
}
```

### 2. Reload VS Code
`Ctrl+Shift+P` → "Developer: Reload Window"

### 3. Allow Trust
Click **"Allow"** when VS Code asks about the MCP server

### 4. Test
In Copilot Chat: `@workspace List Docker containers using mcp-conductor`

**Done! 🎉**

---

## ⚠️ Important Notes

1. **VS Code 1.102+** required for MCP support
2. **GitHub Copilot** or **OpenAI Codex** subscription needed
3. **Absolute paths** must be used in configuration
4. **Security**: Use SSH keys instead of passwords in production
5. **Trust**: VS Code will ask to trust the MCP server first time

---

## 🆘 Need Help?

- **MCP Status**: `Ctrl+Shift+P` → "MCP: Show Status"
- **Reset Trust**: `Ctrl+Shift+P` → "MCP: Reset Trust"
- **View Logs**: Output panel → Select "MCP"
- **Documentation**: See [VS Code MCP Docs](https://code.visualstudio.com/docs/copilot/customization/mcp-servers)

**Your MCP server is ready for VS Code! 🚀**
