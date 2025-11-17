# Tools Reference

This is a complete reference for all the operations Claude can perform through MCP Conductor. Think of these as the "commands" available to the AI.

## Command Execution Tools

### execute_command

Run a single shell command on a Linux server.

**When to use:**
- Quick one-off commands
- Checking status
- Simple operations

**Parameters:**
- `serverName` - Which server to run on (e.g., "production", "staging")
- `command` - The shell command to execute
- `sudo` - Whether to use sudo (true/false)

**Examples:**

Check who's logged in:
```
serverName: production
command: who
sudo: false
```

Restart a service:
```
serverName: production
command: systemctl restart nginx
sudo: true
```

**Notes:**
- Commands go through security validation
- Dangerous commands are automatically blocked
- All executions are logged in audit logs

### execute_script

Run a multi-line shell script.

**When to use:**
- Complex operations requiring multiple commands
- Conditional logic
- Scripts that need to maintain state between commands

**Parameters:**
- `serverName` - Target server
- `script` - Multi-line shell script
- `sudo` - Run with sudo privileges

**Example:**

Deploy application:
```
serverName: production
script: |
  cd /var/www/app
  git pull origin main
  npm install
  pm2 restart app
sudo: false
```

**Notes:**
- Script runs as a single unit
- If one command fails, execution stops (unless you use `|| true`)
- Useful for deployment scripts

### execute_parallel

Run the same command on multiple servers simultaneously.

**When to use:**
- Checking status across all servers
- Deploying to multiple servers
- Gathering information from fleet

**Parameters:**
- `serverNames` - List of servers (e.g., ["web1", "web2", "web3"])
- `command` - Command to run on all servers
- `sudo` - Use sudo

**Example:**

Check disk space on all web servers:
```
serverNames: ["web1", "web2", "web3"]
command: df -h /var/www
sudo: false
```

**Notes:**
- Executes in parallel for speed
- Returns combined results
- If one fails, others continue

## Docker Management Tools

### docker_list_containers

List all Docker containers on a server.

**Parameters:**
- `serverName` - Target server
- `all` - Show all containers (including stopped)

**Example:**
```
serverName: production
all: true
```

**Returns:**
- Container IDs
- Names
- Status (running/stopped)
- Images
- Ports

### docker_start_container

Start a stopped container.

**Parameters:**
- `serverName` - Target server
- `containerId` - Container ID or name

**Example:**
```
serverName: production
containerId: nginx-proxy
```

### docker_stop_container

Stop a running container.

**Parameters:**
- `serverName` - Target server
- `containerId` - Container ID or name

**Example:**
```
serverName: production
containerId: nginx-proxy
```

### docker_restart_container

Restart a container (stop then start).

**Parameters:**
- `serverName` - Target server
- `containerId` - Container ID or name

### docker_logs

View container logs.

**Parameters:**
- `serverName` - Target server
- `containerId` - Container ID or name
- `tail` - Number of lines to show (default: 100)

**Example:**
```
serverName: production
containerId: app-server
tail: 200
```

### docker_inspect

Get detailed information about a container.

**Parameters:**
- `serverName` - Target server
- `containerId` - Container ID or name

**Returns:**
- Full container configuration
- Network settings
- Mounts
- Environment variables

## File Operations Tools

### read_file

Read contents of a file on a server.

**Parameters:**
- `serverName` - Target server
- `filePath` - Full path to file
- `sudo` - Read with sudo privileges

**Example:**
```
serverName: production
filePath: /var/log/nginx/error.log
sudo: true
```

**Notes:**
- Path validation prevents directory traversal
- Can't read sensitive files like `/etc/shadow`
- Large files are truncated (max 10MB)

### write_file

Write content to a file on a server.

**Parameters:**
- `serverName` - Target server
- `filePath` - Full path to file
- `content` - Content to write
- `sudo` - Write with sudo privileges

**Example:**
```
serverName: production
filePath: /etc/nginx/sites-available/mysite
content: |
  server {
    listen 80;
    server_name example.com;
    ...
  }
sudo: true
```

**Notes:**
- Creates file if it doesn't exist
- Overwrites existing content
- Permissions preserved if file exists

### chmod_file

Change file permissions.

**Parameters:**
- `serverName` - Target server
- `filePath` - Full path to file
- `permissions` - Octal permissions (e.g., "644", "755")
- `sudo` - Change with sudo privileges

**Example:**
```
serverName: production
filePath: /var/www/uploads
permissions: 755
sudo: true
```

### chown_file

Change file ownership.

**Parameters:**
- `serverName` - Target server
- `filePath` - Full path to file
- `owner` - New owner (user:group)
- `sudo` - Change with sudo privileges

**Example:**
```
serverName: production
filePath: /var/www/app
owner: www-data:www-data
sudo: true
```

## System Monitoring Tools

### get_system_info

Get comprehensive system information.

**Parameters:**
- `serverName` - Target server

**Returns:**
- CPU usage and load
- Memory usage (RAM and swap)
- Disk space
- System uptime
- OS details

**Example:**
```
serverName: production
```

### get_process_list

List running processes, sorted by CPU usage.

**Parameters:**
- `serverName` - Target server
- `limit` - Number of processes to return (default: 20)

**Example:**
```
serverName: production
limit: 10
```

**Returns:**
- Process ID
- CPU and memory usage
- User
- Command

### get_service_status

Check status of a systemd service.

**Parameters:**
- `serverName` - Target server
- `serviceName` - Name of service (e.g., "nginx", "docker")

**Example:**
```
serverName: production
serviceName: nginx
```

**Returns:**
- Running/stopped status
- Whether enabled at boot
- Recent log entries

### get_disk_usage

Show disk space usage for all mounted filesystems.

**Parameters:**
- `serverName` - Target server

**Returns:**
- Filesystem
- Size, used, available
- Percentage used
- Mount point

### get_memory_usage

Show detailed memory usage.

**Parameters:**
- `serverName` - Target server

**Returns:**
- Total RAM
- Used/free memory
- Buffer/cache usage
- Swap usage

### get_network_info

Show network interface information.

**Parameters:**
- `serverName` - Target server

**Returns:**
- Interface names
- IP addresses (IPv4 and IPv6)
- Interface status

### get_load_average

Show system load average (1min, 5min, 15min).

**Parameters:**
- `serverName` - Target server

**Returns:**
- Load averages
- System uptime

### get_top_processes

Show processes consuming most resources.

**Parameters:**
- `serverName` - Target server
- `sortBy` - "cpu" or "memory" (default: "cpu")
- `count` - Number of processes (default: 10)

**Example:**
```
serverName: production
sortBy: memory
count: 5
```

## Service Management Tools

### start_service

Start a systemd service.

**Parameters:**
- `serverName` - Target server
- `serviceName` - Service to start

**Example:**
```
serverName: production
serviceName: nginx
```

### stop_service

Stop a systemd service.

**Parameters:**
- `serverName` - Target server
- `serviceName` - Service to stop

### restart_service

Restart a systemd service.

**Parameters:**
- `serverName` - Target server
- `serviceName` - Service to restart

**Example:**
```
serverName: production
serviceName: nginx
```

**Note:** Restart is stop + start, so brief downtime occurs

### enable_service

Enable service to start at boot.

**Parameters:**
- `serverName` - Target server
- `serviceName` - Service to enable

### disable_service

Disable service from starting at boot.

**Parameters:**
- `serverName` - Target server
- `serviceName` - Service to disable

## Package Management Tools

### package_install

Install a package using the system's package manager.

**Parameters:**
- `packageName` - Name of package to install
- `serverName` - Target server
- `packageManager` - Which package manager ("apt", "yum", "dnf", "pacman", "zypper", "brew")

**Example:**
```
packageName: nginx
serverName: production
packageManager: apt
```

**Supported package managers:**
- `apt` - Ubuntu, Debian
- `yum` - CentOS/RHEL 6-7
- `dnf` - CentOS/RHEL 8+, Fedora
- `pacman` - Arch Linux
- `zypper` - openSUSE
- `brew` - Homebrew on Linux

### package_uninstall

Remove a package.

**Parameters:**
- `packageName` - Package to remove
- `serverName` - Target server
- `packageManager` - Which package manager

### package_update

Update all packages on the system.

**Parameters:**
- `serverName` - Target server
- `packageManager` - Which package manager

**Example:**
```
serverName: production
packageManager: apt
```

**Note:** This can take a while and may require a reboot for kernel updates

### package_search

Search for packages.

**Parameters:**
- `query` - Search term
- `serverName` - Target server
- `packageManager` - Which package manager

**Example:**
```
query: python
serverName: production
packageManager: apt
```

### package_is_installed

Check if a package is installed.

**Parameters:**
- `packageName` - Package to check
- `serverName` - Target server
- `packageManager` - Which package manager

**Returns:** true or false

## Git Operations Tools

### git_clone

Clone a Git repository.

**Parameters:**
- `serverName` - Target server
- `repository` - Git URL (https or ssh)
- `destination` - Where to clone to
- `branch` - Branch to checkout (optional)

**Example:**
```
serverName: production
repository: https://github.com/user/repo.git
destination: /var/www/app
branch: main
```

### git_pull

Pull latest changes from remote.

**Parameters:**
- `serverName` - Target server
- `repository` - Path to repository on server
- `branch` - Branch to pull (optional)

### git_status

Get repository status.

**Parameters:**
- `serverName` - Target server
- `repository` - Path to repository

**Returns:**
- Current branch
- Modified files
- Untracked files

### git_checkout

Switch to a different branch or commit.

**Parameters:**
- `serverName` - Target server
- `repository` - Path to repository
- `branch` - Branch or commit to checkout

### git_log

View commit history.

**Parameters:**
- `serverName` - Target server
- `repository` - Path to repository
- `limit` - Number of commits (default: 10)

## Nginx Configuration Tools

### nginx_create_site

Create an Nginx virtual host.

**Parameters:**
- `serverName` - Target server
- `siteName` - Site identifier
- `serverName` - Domain name
- `documentRoot` - Path to website files
- `port` - Port to listen on (default: 80)

**Example:**
```
serverName: production
siteName: myapp
serverName: app.example.com
documentRoot: /var/www/myapp
port: 80
```

### nginx_enable_site

Enable a site configuration.

**Parameters:**
- `serverName` - Target server
- `siteName` - Site to enable

### nginx_disable_site

Disable a site configuration.

**Parameters:**
- `serverName` - Target server
- `siteName` - Site to disable

### nginx_test_config

Test Nginx configuration for errors.

**Parameters:**
- `serverName` - Target server

**Returns:** Success or error messages

**Note:** Always test before reloading Nginx!

### nginx_reload

Reload Nginx configuration (zero-downtime).

**Parameters:**
- `serverName` - Target server

## Best Practices

### Error Handling

All tools return structured responses. Check for errors:

```
If response contains "error" or "failed", the operation didn't succeed
If response contains "success", operation completed
```

### Sudo Usage

Use `sudo: true` only when necessary:
- Installing packages
- Modifying system files
- Restarting services
- Changing permissions

### Parallel Operations

Use `execute_parallel` for:
- Checking status across servers
- Deploying to multiple servers
- Gathering metrics

Don't use for:
- Operations that must run in sequence
- Operations with dependencies between servers

### Package Management

Always specify the correct package manager for the Linux distribution:
- Ubuntu/Debian → `apt`
- CentOS/RHEL 7 → `yum`
- CentOS/RHEL 8+, Fedora → `dnf`
- Arch → `pacman`
- openSUSE → `zypper`

## Security Considerations

All tools have built-in security:
- Input validation
- Command whitelisting (optional)
- Path traversal protection
- Rate limiting
- Audit logging

Dangerous operations are automatically blocked:
- `rm -rf /`
- Fork bombs
- Disk wipers
- Sensitive file access

See `docs/security/security-guide.md` for details.

## Getting Help

Each tool includes detailed parameter descriptions that Claude can see. If you're unsure what a tool does or how to use it, just ask Claude!

For example:
> "How do I check disk space on all my servers?"

Claude will use `execute_parallel` with `df -h` command across your server fleet.

## Contributing

Want to add a new tool? See `docs/architecture/extending.md` for guidelines on adding new capabilities.
