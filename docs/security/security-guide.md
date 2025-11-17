# Security Guide

Security is critical when you're giving an AI access to your infrastructure. This guide explains the security measures built into MCP Conductor and best practices for keeping your systems safe.

## Security Philosophy

MCP Conductor follows these principles:

**Defense in depth** - Multiple layers of security, so if one fails, others catch it
**Principle of least privilege** - Only give access to what's absolutely needed
**Audit everything** - Log all operations for accountability
**Fail securely** - When something goes wrong, fail in the safest way possible
**No shortcuts** - Security checks can't be disabled or bypassed

## Built-in Security Features

### Multi-Layer Command Validation

Every command goes through several checks before execution:

**Step 1: Whitelist Check**
Commands are checked against a whitelist of allowed operations. In strict mode, only explicitly allowed commands can run.

**Step 2: Pattern Detection**
We scan for known dangerous patterns:
- Disk wipers (`dd if=/dev/zero of=/dev/sda`)
- Fork bombs (`:(){ :|:& };:`)
- System corruption (`rm -rf /`)
- Data exfiltration attempts

**Step 3: Sanitization**
Remove dangerous characters that could enable command injection:
- Null bytes (`\0`)
- Carriage returns (`\r`)
- Suspicious encoding (hex, URL encoding)

**Step 4: Path Validation**
File paths are checked for:
- Directory traversal (`../../../etc/passwd`)
- Access to sensitive files (`/etc/shadow`, `/etc/sudoers`)
- Symlink exploits

### SSH Security

We use SSH keys exclusively - no password authentication.

**Key Management Best Practices:**

1. **Use separate keys per environment**
```bash
# Development
~/.ssh/dev_key

# Staging
~/.ssh/staging_key

# Production
~/.ssh/prod_key
```

2. **Set correct permissions**
```bash
chmod 600 ~/.ssh/prod_key
chmod 644 ~/.ssh/prod_key.pub
```

3. **Use different usernames**
   Don't use `root` - create dedicated users:
```bash
# On your server
sudo adduser mcpconductor
sudo usermod -aG sudo mcpconductor  # Only if needed
```

4. **Restrict SSH access in authorized_keys**
   You can limit what commands a key can run:
```bash
# In ~/.ssh/authorized_keys on the server
command="/usr/local/bin/allowed-commands-only.sh" ssh-rsa AAAAB3...
```

### Rate Limiting

We prevent abuse with token bucket rate limiting:

```bash
# In your .env file
RATE_LIMIT_CAPACITY=100              # Total tokens available
RATE_LIMIT_REFILL_TOKENS=100         # Tokens added per refill
RATE_LIMIT_REFILL_DURATION_MINUTES=1 # Refill every minute
```

This means:
- You can do 100 operations quickly (burst)
- After that, you're limited to 100 per minute
- If you exceed this, requests are blocked

**Why this matters:**
If something goes wrong (bug, malicious input, infinite loop), rate limiting prevents catastrophic damage.

### Audit Logging

Every operation is logged to `logs/audit.log`:

```
[2024-11-17 10:30:45] USER:admin SERVER:production COMMAND:systemctl restart nginx SUCCESS
[2024-11-17 10:31:12] USER:admin SERVER:staging COMMAND:apt-get install nginx SUCCESS
[2024-11-17 10:32:03] USER:admin SERVER:production COMMAND:rm -rf / BLOCKED:DANGEROUS_PATTERN
```

These logs include:
- Timestamp
- User/session ID
- Target server
- Command attempted
- Result (success/failure/blocked)
- Reason for blocking

**Important:** Audit logs are separate from application logs and should be treated as security-critical data.

### Network Security

**Firewall recommendations:**

```bash
# On the server running MCP Conductor
# Only allow outbound SSH
sudo ufw allow out 22/tcp

# If using Docker remotely
sudo ufw allow out 2376/tcp
```

**On your managed servers:**

```bash
# Only allow SSH from specific IPs
sudo ufw allow from YOUR_MCP_SERVER_IP to any port 22
sudo ufw deny 22
```

## Configuration Security

### Environment Variables

Never commit sensitive data to Git. Use environment variables:

```bash
# .env file (add to .gitignore!)
SSH_PRIVATE_KEY_PATH=/secure/location/key
DOCKER_CERT_PATH=/secure/location/docker-certs
SECURITY_PASSWORD=random-strong-password-here
```

### Secret Management

For production, consider using:
- **HashiCorp Vault** - Secret management
- **AWS Secrets Manager** - If running on AWS
- **Azure Key Vault** - If running on Azure
- **Environment variables from orchestration** - Kubernetes secrets, etc.

Example with Vault:

```bash
# Store secret
vault kv put secret/mcp-conductor ssh_key=@/path/to/key

# Retrieve in application
SSH_PRIVATE_KEY=$(vault kv get -field=ssh_key secret/mcp-conductor)
```

## Recommended Server Hardening

### 1. Disable Root Login

On all managed servers:

```bash
# Edit /etc/ssh/sshd_config
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes

# Restart SSH
sudo systemctl restart sshd
```

### 2. Enable Fail2Ban

Protect against brute force attacks:

```bash
sudo apt-get install fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

### 3. Configure Sudo Properly

If MCP Conductor needs sudo access, be specific:

```bash
# In /etc/sudoers.d/mcpconductor
mcpconductor ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart nginx
mcpconductor ALL=(ALL) NOPASSWD: /usr/bin/systemctl status *
mcpconductor ALL=(ALL) NOPASSWD: /usr/sbin/service nginx *
```

This allows only specific commands without password.

### 4. Use AppArmor or SELinux

Add mandatory access control:

```bash
# Ubuntu/Debian (AppArmor)
sudo apt-get install apparmor apparmor-utils
sudo aa-status

# RHEL/CentOS (SELinux)
sudo setenforce 1
sudo getenforce
```

## Threat Modeling

### What We Protect Against

**Command injection** ✓
Prevented by input sanitization and validation

**Path traversal** ✓
Prevented by path validation

**Privilege escalation** ✓
Prevented by using non-root users and sudo restrictions

**Denial of service** ✓
Prevented by rate limiting

**Data exfiltration** ✓
Prevented by command pattern detection

**Unauthorized access** ✓
Prevented by SSH key authentication

### What You Need to Protect Against

**Compromised MCP Conductor server**
If the server running MCP Conductor is compromised, attackers have access to your SSH keys.

**Mitigations:**
- Keep MCP Conductor server patched
- Use firewall rules
- Monitor logs for suspicious activity
- Rotate SSH keys regularly

**Claude account compromise**
If someone gains access to your Claude account, they can use MCP Conductor.

**Mitigations:**
- Use strong authentication for Claude
- Enable 2FA if available
- Monitor audit logs
- Use session timeouts

**Insider threats**
Someone with legitimate access could misuse it.

**Mitigations:**
- Audit logs show everything
- Use separate keys per person if possible
- Implement approval workflows for sensitive operations
- Regular access reviews

## Incident Response

If you suspect a security issue:

### 1. Immediate Actions

```bash
# Stop MCP Conductor
pkill -f mcp-conductor.jar

# Check audit logs
tail -f logs/audit.log

# Check for unauthorized SSH sessions on managed servers
who
last

# Review SSH logs on managed servers
sudo tail -f /var/log/auth.log
```

### 2. Investigation

```bash
# Check what commands were run
grep "COMMAND:" logs/audit.log

# Look for blocked attempts
grep "BLOCKED:" logs/audit.log

# Check recent SSH connections
last -i | head -20
```

### 3. Remediation

```bash
# Rotate SSH keys immediately
ssh-keygen -t rsa -b 4096 -f ~/.ssh/new_prod_key

# Update authorized_keys on servers
# Remove old key, add new key

# Update .env with new key path
SSH_PRIVATE_KEY_PATH=/path/to/new_prod_key

# Restart MCP Conductor with new keys
java -jar target/mcp-conductor.jar
```

## Compliance Considerations

### Logging Requirements

For compliance (SOC 2, ISO 27001, etc.), you need:

- **Who** - User/session identifier
- **What** - Action performed
- **Where** - Target server
- **When** - Timestamp
- **Result** - Success/failure

MCP Conductor logs all of these in audit logs.

### Access Control

You may need to demonstrate:
- Least privilege access
- Separation of duties
- Regular access reviews

Document your access model and keep records of reviews.

### Data Protection

If managing servers in EU/GDPR context:
- Audit logs may contain personal data
- Implement log retention policies
- Ensure logs are stored securely
- Have a process for log deletion requests

## Security Checklist

Before deploying to production:

- [ ] All SSH keys use 4096-bit RSA or Ed25519
- [ ] Keys have restrictive permissions (600)
- [ ] Separate keys per environment
- [ ] No root login on managed servers
- [ ] Sudo configured with NOPASSWD for specific commands only
- [ ] Fail2Ban installed and configured
- [ ] UFW/iptables rules restrict SSH access
- [ ] Rate limiting configured appropriately
- [ ] Audit logging enabled
- [ ] `.env` file in `.gitignore`
- [ ] Strong password for SECURITY_PASSWORD
- [ ] Monitoring alerts configured
- [ ] Incident response plan documented
- [ ] Log retention policy defined

## Regular Security Maintenance

### Monthly Tasks

- Review audit logs for anomalies
- Check for failed authentication attempts
- Verify SSH keys haven't been compromised
- Update allowed command whitelist if needed

### Quarterly Tasks

- Rotate SSH keys
- Review and update sudo permissions
- Audit who has access
- Update security documentation
- Test incident response procedures

### Annual Tasks

- Full security audit
- Penetration testing
- Update threat model
- Review compliance requirements
- Training on security best practices

## Getting Security Help

If you discover a security vulnerability:

1. **Don't open a public issue** - That alerts attackers
2. **Email the maintainers directly** - We'll respond quickly
3. **Provide details** - Steps to reproduce, impact assessment
4. **Give us time** - We'll work on a fix before public disclosure

For security questions or concerns, feel free to open a discussion (not an issue) on GitHub.

## Summary

MCP Conductor has strong security built in, but security is a shared responsibility. The application protects against common attacks, but you need to:

- Configure SSH securely
- Harden your servers
- Monitor logs
- Respond to incidents
- Keep everything updated

Follow the practices in this guide, and you'll have a solid security posture for your infrastructure automation.
