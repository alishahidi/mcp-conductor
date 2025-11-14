# 🚀 MCP Conductor Quick Start

Get your MCP server running in 5 minutes!

## ⚡ Super Quick Setup (Automated)

```bash
# 1. Build and configure automatically
./setup.sh

# 2. Follow the prompts to configure SSH

# 3. Copy generated config to Claude Desktop
# (The script will tell you where)

# 4. Restart Claude Desktop

# 5. Done! 🎉
```

## 📋 Manual Setup (3 Steps)

### 1️⃣ Build the Server

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn clean package -DskipTests
```

### 2️⃣ Configure Claude Desktop

Edit the config file for your OS:

**macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
**Linux:** `~/.config/Claude/claude_desktop_config.json`
**Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

Add this (update paths and SSH details):

```json
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": [
        "-jar",
        "/FULL/PATH/TO/mcp-conductor/target/mcp-conductor.jar"
      ],
      "env": {
        "SSH_DEFAULT_HOST": "your-server.com",
        "SSH_DEFAULT_USERNAME": "your-username",
        "SSH_PRIVATE_KEY_PATH": "/home/user/.ssh/id_rsa"
      }
    }
  }
}
```

### 3️⃣ Restart Claude Desktop

Close and reopen Claude Desktop. Look for the 🔌 icon!

## 🧪 Test It

Ask Claude:

```
You: List all Docker containers on the server

Claude: [Uses docker_list_containers tool]
```

```
You: Check disk space

Claude: [Uses system_stats or disk_usage tool]
```

```
You: Execute 'uname -a' on the server

Claude: [Uses execute_command tool]
```

## 🔑 SSH Configuration

### Option 1: SSH Key (Recommended)

```json
{
  "env": {
    "SSH_DEFAULT_HOST": "server.com",
    "SSH_DEFAULT_USERNAME": "user",
    "SSH_PRIVATE_KEY_PATH": "/home/user/.ssh/id_rsa"
  }
}
```

### Option 2: Password (Not Recommended)

```json
{
  "env": {
    "SSH_DEFAULT_HOST": "server.com",
    "SSH_DEFAULT_USERNAME": "user",
    "SSH_DEFAULT_PASSWORD": "your-password"
  }
}
```

### Set up SSH Keys (Best Practice)

```bash
# Generate key
ssh-keygen -t rsa -b 4096

# Copy to server
ssh-copy-id user@your-server.com

# Test connection
ssh user@your-server.com
```

## 🛠️ Available Tools

Once configured, ask Claude to:

**Server Management:**
- Execute commands
- Run scripts
- Monitor system resources
- Manage services

**Docker:**
- List/start/stop containers
- Pull images
- View logs
- Execute commands in containers

**Files:**
- Read/write files
- List directories
- Search file contents
- Manage permissions

**Git:**
- Clone repositories
- Commit and push
- Branch management

**Packages:**
- Install/update packages
- Search repositories

**Nginx:**
- Create virtual hosts
- Configure SSL
- Manage sites

**System Monitoring:**
- CPU/RAM usage
- Disk space
- Process list
- Network stats

## 🐛 Troubleshooting

### "Server not found" in Claude Desktop

1. Check config file path is correct
2. Verify JSON is valid (no trailing commas!)
3. Restart Claude Desktop
4. Check Java is installed: `java -version`

### "SSH connection failed"

1. Test SSH manually: `ssh user@your-server.com`
2. Check SSH key path is absolute (no ~)
3. Verify key permissions: `chmod 600 ~/.ssh/id_rsa`
4. Try password auth temporarily to debug

### Build fails

```bash
# Check Java version (needs 21+)
java -version

# Check Maven
mvn -version

# Clean build
mvn clean install -U
```

### Server doesn't start

```bash
# Test manually
java -jar target/mcp-conductor.jar

# Check logs
# Look for "MCP Conductor Server Initialized"
```

## 📚 Full Documentation

- **[USAGE_GUIDE.md](USAGE_GUIDE.md)** - Complete usage guide with examples
- **[MCP_SERVER_FIXES.md](MCP_SERVER_FIXES.md)** - Technical details
- **[.env.example](.env.example)** - All environment variables

## 💡 Example Conversations

### Deploy an App

```
You: Deploy my Node.js app:
1. Clone from https://github.com/user/myapp
2. Install dependencies
3. Build for production
4. Start with PM2

Claude: I'll help deploy your application...
[Executes git clone, npm install, npm build, pm2 start]
```

### Check Server Health

```
You: Give me a health check of the server - disk space,
memory usage, and running services

Claude: Let me check the server health...
[Uses multiple tools: disk_usage, system_stats, service_list]
```

### Setup Nginx

```
You: Setup an Nginx reverse proxy for myapp.com pointing
to localhost:3000

Claude: I'll configure Nginx for you...
[Uses nginx_create_site and nginx_reload]
```

## 🎯 Tips

1. **Use SSH Keys** - More secure than passwords
2. **Test Connection** - Try `ssh user@server` before configuring
3. **Absolute Paths** - Always use full paths in config (no ~)
4. **Restart Claude** - After any config changes
5. **Check Logs** - Run server manually to see errors

## 🔒 Security

- Use SSH keys, not passwords
- Restrict commands in production
- Enable audit logging
- Use dedicated service account on servers
- Keep credentials in environment variables

## ⚙️ Advanced

### Multiple Servers

```json
{
  "env": {
    "SSH_PROD_HOST": "prod.example.com",
    "SSH_PROD_USERNAME": "deploy",
    "SSH_STAGING_HOST": "staging.example.com",
    "SSH_STAGING_USERNAME": "deploy"
  }
}
```

### Debug Mode

```json
{
  "env": {
    "LOG_LEVEL": "DEBUG",
    "MCP_LOG_LEVEL": "DEBUG"
  }
}
```

### Custom Ports

```json
{
  "env": {
    "SSH_DEFAULT_PORT": "2222"
  }
}
```

## 🆘 Need Help?

1. Check [USAGE_GUIDE.md](USAGE_GUIDE.md) for detailed instructions
2. Review [MCP_SERVER_FIXES.md](MCP_SERVER_FIXES.md) for technical details
3. Test with MCP Inspector: `npx @modelcontextprotocol/inspector java -jar target/mcp-conductor.jar`
4. Open an issue on GitHub

## ✅ Quick Checklist

- [ ] Java 21+ installed
- [ ] Maven installed
- [ ] Project built (`mvn clean package -DskipTests`)
- [ ] SSH access to target server working
- [ ] Claude Desktop config file updated
- [ ] Full paths used in config (no ~)
- [ ] Claude Desktop restarted
- [ ] Tested with simple command

---

**That's it! You're ready to automate your infrastructure with AI! 🎉**

For more advanced usage, check out [USAGE_GUIDE.md](USAGE_GUIDE.md)
