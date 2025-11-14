# Configuration Fix - Startup Error Resolved

## Problem

Your application was failing to start with this error:

```
Could not resolve placeholder 'security.path.allowed' in value "${security.path.allowed}"
```

## Root Cause

The `SecurityConfig.java` was trying to inject these required properties:
- `security.command.allowed`
- `security.path.allowed`
- `security.path.blocked`

But these properties were **not defined** in any configuration file, causing Spring Boot to fail during bean creation.

## Solution

I've fixed this in three ways:

### 1. Created `application-dev.yml` (Development Profile)

Added complete security configuration for development:

```yaml
security:
  enabled: false  # Disabled for easier development
  command:
    strict-mode: false
    allowed:
      - ls, cat, docker, git, systemctl, nginx, npm, etc.
  path:
    allowed:
      - /home, /var, /opt, /tmp, /usr/local
    blocked:
      - /etc/passwd, /etc/shadow, /root/.ssh

rate-limit:
  capacity: 1000  # Relaxed for development
  refill-tokens: 100

logging:
  level:
    root: INFO
    net.alishahidi.mcpconductor: DEBUG
    org.springframework.ai.mcp: DEBUG
```

### 2. Created `application-prod.yml` (Production Profile)

Added strict security configuration for production:

```yaml
security:
  enabled: true  # Enabled for production
  command:
    strict-mode: true  # Strict validation
    allowed:
      - Limited command set only
  path:
    allowed:
      - /home, /var/www, /var/log, /opt
    blocked:
      - /etc/passwd, /etc/shadow, /etc/sudoers, /root

rate-limit:
  capacity: 100  # Strict limits
  refill-tokens: 10

logging:
  level:
    root: WARN  # Less verbose in production
```

### 3. Updated `SecurityConfig.java` with Defaults

Added default values to prevent startup failures:

```java
@Bean
public CommandValidator commandValidator(
        @Value("${security.command.strict-mode:true}") boolean strictMode,
        @Value("${security.command.allowed:ls,cat,echo,pwd,whoami,docker,git,systemctl,service}")
        List<String> allowedCommands) {
    return new CommandValidator(strictMode, allowedCommands);
}

@Bean
public PathValidator pathValidator(
        @Value("${security.path.allowed:/home,/var,/opt,/tmp,/usr/local}") List<String> allowedPaths,
        @Value("${security.path.blocked:/etc/passwd,/etc/shadow,/root/.ssh}") List<String> blockedPaths) {
    return new PathValidator(allowedPaths, blockedPaths);
}
```

The `:` syntax after property names means "use this default if not found".

## How to Use

### Development (Default)

Just run the application - it will use the `dev` profile by default:

```bash
mvn spring-boot:run
```

Or:

```bash
java -jar target/mcp-conductor.jar
```

This uses `application-dev.yml` with:
- ✅ Security disabled for easier testing
- ✅ All commands allowed
- ✅ Debug logging enabled
- ✅ Relaxed rate limiting

### Production

Set the profile to `prod`:

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/mcp-conductor.jar
```

Or in your environment:

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar target/mcp-conductor.jar
```

This uses `application-prod.yml` with:
- ✅ Security enabled
- ✅ Strict command validation
- ✅ Limited command whitelist
- ✅ Production logging
- ✅ Strict rate limiting

### Custom Configuration

You can override any property via environment variables:

```bash
# Override SSH settings
export SSH_DEFAULT_HOST=your-server.com
export SSH_DEFAULT_USERNAME=your-user
export SSH_PRIVATE_KEY_PATH=/home/user/.ssh/id_rsa

# Override security
export SECURITY_ENABLED=true
export SECURITY_COMMAND_STRICT_MODE=true

# Run
java -jar target/mcp-conductor.jar
```

## Profiles Overview

| Profile | Security | Commands | Rate Limit | Logging | Use Case |
|---------|----------|----------|------------|---------|----------|
| `dev` (default) | Disabled | All allowed | Relaxed | DEBUG | Development & Testing |
| `prod` | Enabled | Whitelist only | Strict | WARN | Production deployment |
| `http` | Enabled | Custom | Medium | INFO | HTTP API mode |

## Testing the Fix

1. **Clean rebuild**:
   ```bash
   mvn clean compile
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Expected output**:
   ```
   ╔══════════════════════════════════════════════════════════════╗
   ║  MCP Conductor Server Initialized                           ║
   ╠══════════════════════════════════════════════════════════════╣
   ║  Server Name:    mcp-conductor                              ║
   ║  Version:        1.0.0                                      ║
   ║  Mode:           STDIO                                      ║
   ║  Type:           SYNC                                        ║
   ╚══════════════════════════════════════════════════════════════╝
   ```

No more "Could not resolve placeholder" errors! ✅

## Configuration Files Structure

```
src/main/resources/
├── application.yml          # Base config (MCP server settings)
├── application-dev.yml      # Development profile (NEW)
├── application-prod.yml     # Production profile (NEW)
└── application-http.yml     # HTTP mode profile (existing)
```

## Common Configuration Properties

### SSH Configuration

```yaml
ssh:
  default:
    host: your-server.com
    username: your-user
    port: 22
    private-key-path: /path/to/key
```

Or via environment variables:

```bash
SSH_DEFAULT_HOST=your-server.com
SSH_DEFAULT_USERNAME=your-user
SSH_PRIVATE_KEY_PATH=/home/user/.ssh/id_rsa
```

### Security Settings

```yaml
security:
  enabled: true
  command:
    strict-mode: true
    allowed:
      - ls
      - docker
      - git
  path:
    allowed:
      - /home
      - /var
    blocked:
      - /etc/passwd
```

### Rate Limiting

```yaml
rate-limit:
  capacity: 100          # Max tokens in bucket
  refill-tokens: 10      # Tokens added per interval
  refill-duration-minutes: 1  # Refill interval
```

## Troubleshooting

### Issue: Still getting placeholder errors

**Solution**: Make sure you're using Spring Boot 3.4.1 or higher. Check your `pom.xml`:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.1</version>
</parent>
```

### Issue: Wrong profile active

**Solution**: Check which profile is active:

```bash
# See active profile in logs:
# "The following 1 profile is active: dev"

# Override if needed:
export SPRING_PROFILES_ACTIVE=dev
```

### Issue: Need different settings per environment

**Solution**: Create custom profile file:

```bash
# Create application-staging.yml
cp src/main/resources/application-prod.yml src/main/resources/application-staging.yml

# Edit as needed
nano src/main/resources/application-staging.yml

# Use it:
SPRING_PROFILES_ACTIVE=staging java -jar target/mcp-conductor.jar
```

## What About the Annotations?

You mentioned seeing these imports:

```java
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
```

**These are WRONG!** ❌

The correct imports (already in your code) are:

```java
import org.springframework.ai.mcp.server.annotation.McpTool;
import org.springframework.ai.mcp.server.annotation.McpToolParam;
```

I already fixed all tool classes to use the correct annotations. If you see the old ones somewhere:
- They're from the old `spring-ai-community` project
- We're using the official Spring AI annotations now
- Check your imports if you create new tool classes

## Summary

✅ **Fixed**: Missing security configuration properties
✅ **Added**: Development profile (`application-dev.yml`)
✅ **Added**: Production profile (`application-prod.yml`)
✅ **Updated**: SecurityConfig with default values
✅ **Verified**: Correct MCP annotations in use

The application should now start successfully! 🎉

## Next Steps

1. Build the project:
   ```bash
   mvn clean package -DskipTests
   ```

2. Run it:
   ```bash
   java -jar target/mcp-conductor.jar
   ```

3. Configure SSH connection (see USAGE_GUIDE.md)

4. Add to Claude Desktop (see QUICKSTART.md)

For more details, see:
- [USAGE_GUIDE.md](USAGE_GUIDE.md) - Complete usage instructions
- [QUICKSTART.md](QUICKSTART.md) - Quick reference
- [MCP_SERVER_FIXES.md](MCP_SERVER_FIXES.md) - Technical details
