#!/bin/bash
# MCP Conductor - Production Setup Verification
# Verify all components are properly configured

set -e

echo "🔍 MCP Conductor - Production Setup Verification"
echo "================================================"

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

success_count=0
total_checks=0

check() {
    local name="$1"
    local command="$2"
    total_checks=$((total_checks + 1))
    
    printf "%-40s" "$name"
    if eval "$command" >/dev/null 2>&1; then
        echo -e "${GREEN}✓ PASS${NC}"
        success_count=$((success_count + 1))
    else
        echo -e "${RED}✗ FAIL${NC}"
    fi
}

echo
echo "📁 Directory Structure Checks"
echo "-----------------------------"
check "bin/ directory exists" "[ -d 'bin' ]"
check "config/ directory exists" "[ -d 'config' ]"
check "scripts/ directory exists" "[ -d 'scripts' ]"
check "MCP STDIO server exists" "[ -f 'bin/mcp-server.py' ]"
check "HTTP launcher exists" "[ -f 'scripts/run-http-mode.sh' ]"
check "STDIO launcher exists" "[ -f 'scripts/run-stdio-mode.sh' ]"

echo
echo "⚙️ Configuration Checks"
echo "----------------------"
check "Example config exists" "[ -f 'config/example.env' ]"
check "HTTP config exists" "[ -f 'config/http.env' ]"
check "STDIO config exists" "[ -f 'config/stdio.env' ]"
check "Prometheus config exists" "[ -f 'config/prometheus.yml' ]"

echo
echo "🔧 Build Checks"
echo "---------------"
check "Maven POM exists" "[ -f 'pom.xml' ]"
check "JAR file exists" "[ -f 'target/mcp-conductor.jar' ]"
check "Source directory exists" "[ -d 'src/main/java' ]"

echo
echo "🖥️ System Requirements"
echo "----------------------"
check "Java available" "command -v java"
check "Python3 available" "command -v python3"
check "Maven available" "command -v mvn"

echo
echo "📜 Cross-platform Scripts"
echo "-------------------------"
check "HTTP script executable" "[ -x 'scripts/run-http-mode.sh' ]"
check "STDIO script executable" "[ -x 'scripts/run-stdio-mode.sh' ]"
check "MCP server executable" "[ -x 'bin/mcp-server.py' ]"
check "Windows HTTP script exists" "[ -f 'scripts/run-http-mode.bat' ]"
check "Windows STDIO script exists" "[ -f 'scripts/run-stdio-mode.bat' ]"

echo
echo "📚 Documentation"
echo "----------------"
check "README.md exists" "[ -f 'README.md' ]"
check "CLAUDE.md exists" "[ -f 'CLAUDE.md' ]"
check "License exists" "[ -f 'LICENSE' ]"

echo
echo "🐳 Container Support"
echo "-------------------"
check "Dockerfile exists" "[ -f 'Dockerfile' ]"
check "Docker Compose exists" "[ -f 'docker-compose.yml' ]"

echo
echo "📊 Results"
echo "=========="
echo -e "Passed: ${GREEN}$success_count${NC}/$total_checks checks"

if [ $success_count -eq $total_checks ]; then
    echo -e "${GREEN}🎉 All checks passed! Production setup verified.${NC}"
    echo
    echo "🚀 Ready to run:"
    echo "  HTTP Mode:  ./scripts/run-http-mode.sh"
    echo "  STDIO Mode: ./scripts/run-stdio-mode.sh"
    echo
    echo "📋 Next steps:"
    echo "  1. Configure environment in config/http.env"
    echo "  2. Set up SSH keys for remote access"
    echo "  3. Test HTTP mode: curl http://localhost:8080/actuator/health"
    echo "  4. Register with Claude Code for STDIO mode"
    exit 0
else
    failed=$((total_checks - success_count))
    echo -e "${RED}❌ $failed check(s) failed. Please fix issues before deployment.${NC}"
    exit 1
fi