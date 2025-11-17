# Production Deployment Guide

Getting MCP Conductor ready for production requires more than just running `java -jar`. This guide walks you through a proper production deployment that's secure, reliable, and maintainable.

## Pre-Deployment Checklist

Before deploying to production:

- [ ] All tests pass (`mvn test`)
- [ ] Security audit completed
- [ ] SSH keys generated and distributed
- [ ] Server access verified
- [ ] Monitoring configured
- [ ] Backup strategy defined
- [ ] Rollback plan documented

## Deployment Options

### Option 1: Systemd Service (Recommended for Linux)

This is the most straightforward approach for Linux servers.

#### Step 1: Create Service User

```bash
# Create dedicated user (no shell access)
sudo useradd -r -s /bin/false mcpconductor

# Create directories
sudo mkdir -p /opt/mcp-conductor
sudo mkdir -p /var/log/mcp-conductor
sudo chown mcpconductor:mcpconductor /opt/mcp-conductor
sudo chown mcpconductor:mcpconductor /var/log/mcp-conductor
```

#### Step 2: Install Application

```bash
# Build the JAR
mvn clean package -DskipTests

# Copy to installation directory
sudo cp target/mcp-conductor.jar /opt/mcp-conductor/
sudo chown mcpconductor:mcpconductor /opt/mcp-conductor/mcp-conductor.jar
```

#### Step 3: Create Configuration

```bash
# Create environment file
sudo nano /opt/mcp-conductor/.env
```

Add your configuration:

```bash
SPRING_PROFILES_ACTIVE=prod
SSH_DEFAULT_HOST=prod.example.com
SSH_DEFAULT_USERNAME=deploy
SSH_PRIVATE_KEY_PATH=/opt/mcp-conductor/.ssh/id_rsa
SECURITY_PASSWORD=your-strong-password-here
```

Secure it:

```bash
sudo chown mcpconductor:mcpconductor /opt/mcp-conductor/.env
sudo chmod 600 /opt/mcp-conductor/.env
```

#### Step 4: Create Systemd Service

```bash
sudo nano /etc/systemd/system/mcp-conductor.service
```

Add this content:

```ini
[Unit]
Description=MCP Conductor - DevOps Automation Server
After=network.target

[Service]
Type=simple
User=mcpconductor
Group=mcpconductor
WorkingDirectory=/opt/mcp-conductor
EnvironmentFile=/opt/mcp-conductor/.env
ExecStart=/usr/bin/java -Xmx512m -Xms256m -jar /opt/mcp-conductor/mcp-conductor.jar
Restart=always
RestartSec=10
StandardOutput=append:/var/log/mcp-conductor/output.log
StandardError=append:/var/log/mcp-conductor/error.log

# Security settings
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/var/log/mcp-conductor
ReadWritePaths=/opt/mcp-conductor/logs

[Install]
WantedBy=multi-user.target
```

#### Step 5: Enable and Start

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable auto-start
sudo systemctl enable mcp-conductor

# Start service
sudo systemctl start mcp-conductor

# Check status
sudo systemctl status mcp-conductor
```

#### Managing the Service

```bash
# Start
sudo systemctl start mcp-conductor

# Stop
sudo systemctl stop mcp-conductor

# Restart
sudo systemctl restart mcp-conductor

# View logs
sudo journalctl -u mcp-conductor -f

# View last 100 lines
sudo journalctl -u mcp-conductor -n 100
```

### Option 2: Docker Container

Perfect for containerized environments or Kubernetes.

#### Create Dockerfile

We already have one, but here's what it does:

```dockerfile
FROM openjdk:21-jre-slim
WORKDIR /app
COPY target/mcp-conductor.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

#### Build Image

```bash
# Build
docker build -t mcp-conductor:1.0.0 .

# Tag for registry
docker tag mcp-conductor:1.0.0 your-registry.com/mcp-conductor:1.0.0

# Push
docker push your-registry.com/mcp-conductor:1.0.0
```

#### Run Container

```bash
docker run -d \
  --name mcp-conductor \
  --restart unless-stopped \
  -v /path/to/.env:/app/.env:ro \
  -v /path/to/ssh-keys:/app/.ssh:ro \
  -v /path/to/logs:/app/logs \
  -p 8080:8080 \
  mcp-conductor:1.0.0
```

#### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mcp-conductor:
    image: mcp-conductor:1.0.0
    container_name: mcp-conductor
    restart: unless-stopped
    env_file:
      - .env
    volumes:
      - ./ssh-keys:/app/.ssh:ro
      - ./logs:/app/logs
    ports:
      - "8080:8080"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

Run it:

```bash
docker-compose up -d
```

### Option 3: Kubernetes Deployment

For large-scale deployments.

#### Create ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mcp-conductor-config
data:
  application.yml: |
    spring:
      profiles:
        active: prod
```

#### Create Secret

```bash
kubectl create secret generic mcp-conductor-secrets \
  --from-file=ssh-key=/path/to/key \
  --from-literal=security-password=your-password
```

#### Create Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mcp-conductor
spec:
  replicas: 2
  selector:
    matchLabels:
      app: mcp-conductor
  template:
    metadata:
      labels:
        app: mcp-conductor
    spec:
      containers:
      - name: mcp-conductor
        image: your-registry.com/mcp-conductor:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SSH_PRIVATE_KEY_PATH
          value: "/app/.ssh/id_rsa"
        volumeMounts:
        - name: ssh-keys
          mountPath: /app/.ssh
          readOnly: true
        - name: logs
          mountPath: /app/logs
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
      volumes:
      - name: ssh-keys
        secret:
          secretName: mcp-conductor-secrets
      - name: logs
        emptyDir: {}
```

Apply:

```bash
kubectl apply -f deployment.yaml
```

## Configuration for Production

### Environment-Specific Settings

#### application-prod.yml

Create `src/main/resources/application-prod.yml`:

```yaml
spring:
  application:
    name: mcp-conductor

logging:
  level:
    root: INFO
    net.alishahidi.mcpconductor: INFO
  file:
    name: /var/log/mcp-conductor/application.log
    max-size: 10MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,info
  metrics:
    export:
      prometheus:
        enabled: true

security:
  require-ssl: false  # Set to true if using HTTPS

rate-limit:
  capacity: 100
  refill-tokens: 100
  refill-duration-minutes: 1
```

### JVM Tuning

For optimal performance:

```bash
JAVA_OPTS="-Xms256m -Xmx512m \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:+HeapDumpOnOutOfMemoryError \
           -XX:HeapDumpPath=/var/log/mcp-conductor/heap-dump.hprof"
```

Add to your systemd service or Docker command.

## Monitoring Setup

### Prometheus Integration

MCP Conductor exposes metrics at `/actuator/prometheus`.

#### Prometheus Configuration

Add to `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'mcp-conductor'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
```

### Grafana Dashboard

Import our pre-built dashboard:

1. Open Grafana
2. Go to Dashboards → Import
3. Upload `docs/monitoring/grafana-dashboard.json`
4. Select your Prometheus data source

Key metrics to monitor:
- Request rate per tool
- Success/failure rates
- SSH connection pool utilization
- Command execution times
- Rate limiting events

### Log Aggregation

#### Using Filebeat + ELK Stack

Install Filebeat:

```bash
sudo apt-get install filebeat
```

Configure `/etc/filebeat/filebeat.yml`:

```yaml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/mcp-conductor/*.log
    - /var/log/mcp-conductor/audit.log
  fields:
    service: mcp-conductor
    environment: production

output.elasticsearch:
  hosts: ["elasticsearch:9200"]

setup.kibana:
  host: "kibana:5601"
```

Start Filebeat:

```bash
sudo systemctl enable filebeat
sudo systemctl start filebeat
```

## High Availability

### Load Balancing

Use Nginx or HAProxy to load balance multiple instances:

#### Nginx Configuration

```nginx
upstream mcp-conductor {
    least_conn;
    server mcp-conductor-1:8080 max_fails=3 fail_timeout=30s;
    server mcp-conductor-2:8080 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name mcp.example.com;

    location / {
        proxy_pass http://mcp-conductor;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

### Database Replication

If you add database support later, configure master-slave replication for HA.

## Backup Strategy

### What to Backup

1. **Configuration files**
   - `.env` file
   - `application-prod.yml`
   - SSH keys

2. **Audit logs**
   - `/var/log/mcp-conductor/audit.log`
   - Essential for compliance and security

3. **Application state**
   - Any local databases or state files

### Backup Script

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups/mcp-conductor"
DATE=$(date +%Y%m%d-%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR/$DATE

# Backup configuration
cp /opt/mcp-conductor/.env $BACKUP_DIR/$DATE/
cp /opt/mcp-conductor/.ssh/id_rsa $BACKUP_DIR/$DATE/ 2>/dev/null

# Backup logs
cp -r /var/log/mcp-conductor $BACKUP_DIR/$DATE/

# Compress
tar -czf $BACKUP_DIR/mcp-conductor-$DATE.tar.gz $BACKUP_DIR/$DATE/
rm -rf $BACKUP_DIR/$DATE

# Keep only last 30 days
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete

echo "Backup completed: mcp-conductor-$DATE.tar.gz"
```

Schedule with cron:

```bash
# Run daily at 2 AM
0 2 * * * /usr/local/bin/backup.sh
```

## Disaster Recovery

### Recovery Time Objective (RTO)

Target: 15 minutes from incident to service restoration

### Recovery Point Objective (RPO)

Target: Maximum 1 hour of data loss (audit logs)

### Recovery Procedure

1. **Provision new server** (or use standby)
2. **Restore from backup**
   ```bash
   tar -xzf /backups/mcp-conductor-YYYYMMDD.tar.gz
   ```
3. **Verify configuration**
   ```bash
   cat .env  # Check all settings
   ```
4. **Start service**
   ```bash
   sudo systemctl start mcp-conductor
   ```
5. **Verify health**
   ```bash
   curl http://localhost:8080/actuator/health
   ```
6. **Update DNS** (if IP changed)
7. **Test functionality**
8. **Monitor logs**

## Rolling Updates

### Zero-Downtime Deployment

1. **Build new version**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Deploy to staging first**
   ```bash
   # Test thoroughly in staging
   ```

3. **Deploy to production instances one at a time**
   ```bash
   # Instance 1
   sudo systemctl stop mcp-conductor
   sudo cp target/mcp-conductor.jar /opt/mcp-conductor/
   sudo systemctl start mcp-conductor

   # Wait and verify
   sleep 30
   curl http://instance-1:8080/actuator/health

   # Instance 2
   # Repeat process
   ```

### Rollback Procedure

```bash
# Stop service
sudo systemctl stop mcp-conductor

# Restore previous version
sudo cp /opt/mcp-conductor/mcp-conductor.jar.backup /opt/mcp-conductor/mcp-conductor.jar

# Start service
sudo systemctl start mcp-conductor

# Verify
sudo systemctl status mcp-conductor
```

## Troubleshooting Production Issues

### Service Won't Start

```bash
# Check logs
sudo journalctl -u mcp-conductor -n 100

# Common issues:
# - Port already in use
# - Missing SSH keys
# - Invalid configuration
# - Insufficient memory
```

### High Memory Usage

```bash
# Check current usage
ps aux | grep mcp-conductor

# Get heap dump
sudo -u mcpconductor jmap -dump:live,format=b,file=/tmp/heap.hprof <PID>

# Analyze with tools like VisualVM or Eclipse MAT
```

### SSH Connection Failures

```bash
# Test SSH manually
ssh -i /path/to/key user@host

# Check SSH logs on target server
sudo tail -f /var/log/auth.log

# Verify key permissions
ls -l /opt/mcp-conductor/.ssh/
```

## Security Hardening for Production

See `docs/security/security-guide.md` for comprehensive security practices.

Quick checklist:
- [ ] Non-root user
- [ ] SSH keys only (no passwords)
- [ ] Firewall rules configured
- [ ] Audit logging enabled
- [ ] Regular security updates
- [ ] Log monitoring and alerts
- [ ] Incident response plan

## Performance Tuning

### Connection Pooling

Tune SSH connection pool in `application-prod.yml`:

```yaml
ssh:
  pool:
    max-size: 10
    max-wait-millis: 5000
    eviction-interval-millis: 60000
```

### Rate Limiting

Adjust based on usage patterns:

```yaml
rate-limit:
  capacity: 200  # Higher for production
  refill-tokens: 200
  refill-duration-minutes: 1
```

## Maintenance Windows

Schedule regular maintenance:

### Weekly Tasks
- Review error logs
- Check disk space
- Verify backups

### Monthly Tasks
- Apply security updates
- Review audit logs
- Performance tuning
- Capacity planning

### Quarterly Tasks
- Disaster recovery drill
- Security audit
- Update documentation

## Getting Support

For production issues:

1. **Check logs first** - Most issues are obvious in logs
2. **Review documentation** - Common issues are documented
3. **Search GitHub issues** - Someone may have had same problem
4. **Open support ticket** - Provide logs and config (redact secrets!)

## Summary

Production deployment requires attention to security, reliability, and maintainability. This guide covers the essentials, but every environment is different. Adapt these practices to your specific needs, and always test thoroughly before deploying to production.

Remember: Monitor everything, backup regularly, and have a rollback plan. Things will go wrong - be prepared!
