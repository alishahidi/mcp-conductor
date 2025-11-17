# Getting Started with MCP Conductor

Welcome! This guide will help you get MCP Conductor up and running on your system. Whether you're setting it up for the first time or just need a refresher, we've got you covered.

## What You'll Need

Before diving in, make sure you have these installed on your machine:

- **Java 21 or newer** - The application runs on Java 21, so you'll need that version or higher
- **Maven 3.8+** - For building the project (though we provide scripts to make this easier)
- **SSH access to your Linux servers** - You'll need credentials (preferably SSH keys) for the servers you want to manage
- **Docker** (optional) - Only if you want to manage Docker containers

## Quick Setup

### Step 1: Get the Code

Clone the repository to your local machine:

```bash
git clone https://github.com/alishahidi/mcp-conductor.git
cd mcp-conductor
```

### Step 2: Configure Your SSH Connections

Create a `.env` file in the project root. Here's a basic example:

```bash
# SSH Configuration for your Linux servers
SSH_DEFAULT_HOST=your-server.example.com
SSH_DEFAULT_PORT=22
SSH_DEFAULT_USERNAME=youruser
SSH_PRIVATE_KEY_PATH=/home/youruser/.ssh/id_rsa

# If you have multiple servers, you can configure them like this:
SSH_SERVERS_PRODUCTION_HOST=prod.example.com
SSH_SERVERS_PRODUCTION_USERNAME=admin
SSH_SERVERS_PRODUCTION_KEY=/path/to/prod-key

SSH_SERVERS_STAGING_HOST=staging.example.com
SSH_SERVERS_STAGING_USERNAME=deploy
SSH_SERVERS_STAGING_KEY=/path/to/staging-key
```

**Important notes:**
- Replace the values with your actual server details
- Use SSH keys instead of passwords for better security
- Make sure your SSH keys don't have passphrases (or configure ssh-agent)

### Step 3: Build the Application

We've made this super simple. Just run:

```bash
# This sets the correct Java version and builds everything
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn clean package -DskipTests
```

If Maven complains about Java version, double-check that JAVA_HOME points to Java 21.

### Step 4: Run the Application

Start the server:

```bash
java -jar target/mcp-conductor.jar
```

That's it! The server is now running and ready to accept commands from Claude.

## Connecting to Claude Desktop

To use MCP Conductor with Claude Desktop, you need to add it to Claude's configuration:

### For Linux/macOS

Edit `~/.config/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": [
        "-jar",
        "/full/path/to/mcp-conductor/target/mcp-conductor.jar"
      ],
      "env": {
        "SPRING_PROFILES_ACTIVE": "prod"
      }
    }
  }
}
```

### For Windows

Edit `%APPDATA%\Claude\claude_desktop_config.json` with the same configuration (adjust paths for Windows).

### Restart Claude

After saving the configuration, restart Claude Desktop completely. The MCP Conductor server will now be available when you chat with Claude.

## Verifying Everything Works

Once you've started the server, you can verify it's working:

### Check the Logs

The application will print startup logs. Look for:

```
MCP Conductor started successfully
Docker client initialized for host: unix:///var/run/docker.sock
SSH connections configured for 2 servers
```

### Try a Simple Command

In Claude Desktop, try asking:

> "Can you check the disk space on my production server?"

Claude should connect to MCP Conductor, which will then SSH into your server and return the disk usage information.

## Troubleshooting Common Issues

### "Failed to connect to SSH server"

**Problem:** The application can't reach your server.

**Solutions:**
- Verify the hostname/IP is correct in your `.env` file
- Make sure your SSH key has the correct permissions: `chmod 600 ~/.ssh/id_rsa`
- Test SSH manually: `ssh -i ~/.ssh/id_rsa user@server.example.com`
- Check if the server's firewall allows SSH connections

### "Permission denied (publickey)"

**Problem:** SSH authentication is failing.

**Solutions:**
- Make sure the SSH key path in `.env` is correct
- Verify the key is added to `~/.ssh/authorized_keys` on the remote server
- Check that you're using the correct username
- Try connecting manually first to troubleshoot

### "Docker daemon not responding"

**Problem:** Docker isn't available or isn't running.

**Solutions:**
- Make sure Docker is installed: `docker --version`
- Check if Docker is running: `sudo systemctl status docker`
- Start Docker if needed: `sudo systemctl start docker`
- On Windows, make sure Docker Desktop is running

### "Java version mismatch"

**Problem:** The application requires Java 21.

**Solutions:**
- Check your Java version: `java -version`
- Install Java 21 if needed (we recommend OpenJDK)
- Set JAVA_HOME correctly before building

## What's Next?

Now that you have MCP Conductor running, here's what you can do:

1. **Explore the available commands** - Check out `docs/tools/tools-reference.md` for a full list
2. **Secure your setup** - Read `docs/security/security-guide.md` for best practices
3. **Set up multiple servers** - Add more servers to your `.env` file
4. **Configure Docker** - If you want container management capabilities
5. **Monitor your infrastructure** - Use Claude to check system health, logs, and more

## Getting Help

If you run into issues:

- Check the troubleshooting section above
- Look through the other docs in the `docs/` folder
- Review the logs - they usually contain helpful error messages
- Open an issue on GitHub if you think you've found a bug

Happy automating!
