#!/bin/bash
# MCP Conductor - STDIO Mode Launcher
# Production MCP STDIO server for Claude Code integration

set -e

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "🔌 Starting MCP Conductor - STDIO Mode"
echo "Project Directory: $PROJECT_DIR"

# Load environment
if [ -f "config/stdio.env" ]; then
    echo "Loading STDIO configuration..."
    export $(grep -v '^#' config/stdio.env | grep -v '^$' | xargs)
else
    echo "⚠️ Warning: config/stdio.env not found, using defaults"
fi

# Check Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Error: Python 3 not found"
    echo "Please install Python 3.8+ and ensure it's in PATH"
    exit 1
fi

# Check if mcp-server.py exists
if [ ! -f "bin/mcp-server.py" ]; then
    echo "❌ Error: bin/mcp-server.py not found"
    echo "Please ensure the MCP server file exists"
    exit 1
fi

# Make sure the script is executable
chmod +x bin/mcp-server.py

echo "Starting MCP STDIO server..."
exec python3 bin/mcp-server.py