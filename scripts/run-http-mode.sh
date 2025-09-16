#!/bin/bash
# MCP Conductor - HTTP Mode Launcher
# Cross-platform startup script for HTTP API mode

set -e

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "🌐 Starting MCP Conductor - HTTP Mode"
echo "Project Directory: $PROJECT_DIR"

# Load environment
if [ -f "config/http.env" ]; then
    echo "Loading HTTP configuration..."
    export $(grep -v '^#' config/http.env | grep -v '^$' | xargs)
else
    echo "⚠️ Warning: config/http.env not found, using defaults"
fi

# Check if JAR exists
if [ ! -f "target/mcp-conductor.jar" ]; then
    echo "❌ Error: target/mcp-conductor.jar not found"
    echo "Please run: mvn clean package -DskipTests"
    exit 1
fi

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java not found"
    echo "Please install Java 21+ and set JAVA_HOME"
    exit 1
fi

echo "Starting HTTP server on port ${SERVER_PORT:-8080}..."
exec java -jar target/mcp-conductor.jar