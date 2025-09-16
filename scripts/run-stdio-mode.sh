#!/bin/bash
# start-mcp-stdio.sh - Working MCP STDIO launcher for Claude Code

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "🔌 Starting MCP Conductor - STDIO Mode" >&2

# Check if Python 3 is available
if ! command -v python3 &> /dev/null; then
    echo "❌ Error: Python 3 not found" >&2
    exit 1
fi

# Check if JAR exists, if not build it
if [ ! -f "target/mcp-conductor.jar" ]; then
    echo "📦 Building project..." >&2
    mvn clean package -DskipTests -q
fi

# Make Python script executable
chmod +x bin/mcp-server.py

# Run the MCP STDIO bridge
exec python3 bin/mcp-server.py