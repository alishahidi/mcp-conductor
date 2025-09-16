#!/bin/bash
# start-http.sh - HTTP mode for testing

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "🌐 Starting MCP Conductor - HTTP Mode"

# Check if JAR exists
if [ ! -f "target/mcp-conductor.jar" ]; then
    echo "📦 Building project..."
    mvn clean package -DskipTests
fi

# Load HTTP environment
if [ -f "config/http.env" ]; then
    export $(grep -v '^#' config/http.env | xargs)
fi

# Run in HTTP mode
export SPRING_PROFILES_ACTIVE=http
java -jar target/mcp-conductor.jar