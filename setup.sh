#!/bin/bash

# MCP Conductor Setup Script
# This script helps you set up the MCP server for use with Claude Code

set -e

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║         MCP Conductor Setup Assistant                       ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check Java installation
info "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    success "Java found: $JAVA_VERSION"

    # Check if Java 21 is available
    if [ -d "/usr/lib/jvm/java-21-openjdk-amd64" ]; then
        export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
        success "Java 21 found at $JAVA_HOME"
    else
        warning "Java 21 not found at standard location. Using system Java."
    fi
else
    error "Java not found! Please install Java 21 or higher."
    exit 1
fi

# Check Maven installation
info "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    success "Maven found: $MVN_VERSION"
else
    error "Maven not found! Please install Maven."
    exit 1
fi

# Build the project
echo ""
info "Building MCP Conductor..."
echo ""

if [ "$1" == "--skip-tests" ]; then
    mvn clean package -DskipTests -q
else
    mvn clean package -q
fi

if [ $? -eq 0 ]; then
    success "Build completed successfully!"
    JAR_PATH="$(pwd)/target/mcp-conductor.jar"
    success "JAR created at: $JAR_PATH"
else
    error "Build failed! Check the output above for errors."
    exit 1
fi

# Test the server
echo ""
info "Testing the MCP server..."
echo ""

# Start server in background and capture output
timeout 10s java -jar target/mcp-conductor.jar > /tmp/mcp-test.log 2>&1 &
SERVER_PID=$!

# Wait a bit for server to start
sleep 5

# Check if server is still running
if ps -p $SERVER_PID > /dev/null; then
    success "Server started successfully!"
    kill $SERVER_PID 2>/dev/null || true

    # Show relevant logs
    if grep -q "MCP Conductor Server Initialized" /tmp/mcp-test.log; then
        success "Server initialization confirmed!"
    fi
else
    warning "Server may have stopped. Check logs:"
    cat /tmp/mcp-test.log
fi

# SSH Setup
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║         SSH Configuration                                    ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

read -p "Do you want to configure SSH settings? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    read -p "SSH Host (e.g., your-server.com): " SSH_HOST
    read -p "SSH Username: " SSH_USER
    read -p "SSH Port [22]: " SSH_PORT
    SSH_PORT=${SSH_PORT:-22}

    echo ""
    echo "Choose authentication method:"
    echo "1) Private Key (Recommended)"
    echo "2) Password (Not recommended for production)"
    read -p "Enter choice (1 or 2): " AUTH_CHOICE

    if [ "$AUTH_CHOICE" == "1" ]; then
        read -p "Private key path [~/.ssh/id_rsa]: " KEY_PATH
        KEY_PATH=${KEY_PATH:-~/.ssh/id_rsa}
        KEY_PATH=$(eval echo $KEY_PATH)

        if [ -f "$KEY_PATH" ]; then
            success "Private key found at $KEY_PATH"
            SSH_KEY_PATH=$KEY_PATH

            # Test SSH connection
            info "Testing SSH connection..."
            if ssh -i "$KEY_PATH" -o BatchMode=yes -o ConnectTimeout=5 "$SSH_USER@$SSH_HOST" exit 2>/dev/null; then
                success "SSH connection successful!"
            else
                warning "Could not connect to SSH server. Please verify your credentials."
            fi
        else
            error "Private key not found at $KEY_PATH"
        fi
    else
        read -s -p "SSH Password: " SSH_PASS
        echo ""
        warning "Password authentication is less secure. Consider using SSH keys."
    fi
fi

# Generate Claude Desktop config
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║         Claude Desktop Configuration                        ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

read -p "Generate Claude Desktop config file? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    CONFIG_FILE="claude_desktop_config_generated.json"

    cat > "$CONFIG_FILE" <<EOF
{
  "mcpServers": {
    "mcp-conductor": {
      "command": "java",
      "args": [
        "-jar",
        "$JAR_PATH"
      ],
      "env": {
EOF

    # Add JAVA_HOME if available
    if [ -n "$JAVA_HOME" ]; then
        echo "        \"JAVA_HOME\": \"$JAVA_HOME\"," >> "$CONFIG_FILE"
    fi

    # Add SSH settings if configured
    if [ -n "$SSH_HOST" ]; then
        echo "        \"SSH_DEFAULT_HOST\": \"$SSH_HOST\"," >> "$CONFIG_FILE"
        echo "        \"SSH_DEFAULT_USERNAME\": \"$SSH_USER\"," >> "$CONFIG_FILE"
        echo "        \"SSH_DEFAULT_PORT\": \"$SSH_PORT\"," >> "$CONFIG_FILE"

        if [ -n "$SSH_KEY_PATH" ]; then
            echo "        \"SSH_PRIVATE_KEY_PATH\": \"$SSH_KEY_PATH\"," >> "$CONFIG_FILE"
        elif [ -n "$SSH_PASS" ]; then
            echo "        \"SSH_DEFAULT_PASSWORD\": \"$SSH_PASS\"," >> "$CONFIG_FILE"
        fi
    fi

    cat >> "$CONFIG_FILE" <<EOF
        "LOG_LEVEL": "INFO",
        "MCP_LOG_LEVEL": "DEBUG"
      }
    }
  }
}
EOF

    success "Configuration file created: $CONFIG_FILE"
    echo ""
    info "Copy this config to Claude Desktop configuration:"

    # Detect OS and show appropriate path
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "  ~/Library/Application Support/Claude/claude_desktop_config.json"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "  ~/.config/Claude/claude_desktop_config.json"
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
        echo "  %APPDATA%\\Claude\\claude_desktop_config.json"
    fi

    echo ""
    info "Or merge the content with your existing config if you have one."
fi

# Final instructions
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║         Setup Complete!                                      ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

success "MCP Conductor is ready to use!"
echo ""
echo "Next steps:"
echo ""
echo "1. Copy the generated config to Claude Desktop:"
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "   cp $CONFIG_FILE ~/Library/Application\ Support/Claude/claude_desktop_config.json"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "   mkdir -p ~/.config/Claude"
    echo "   cp $CONFIG_FILE ~/.config/Claude/claude_desktop_config.json"
fi
echo ""
echo "2. Restart Claude Desktop"
echo ""
echo "3. Look for the 🔌 icon to see available MCP tools"
echo ""
echo "4. Test with MCP Inspector (optional):"
echo "   npx @modelcontextprotocol/inspector java -jar $JAR_PATH"
echo ""
echo "5. Read the usage guide:"
echo "   cat USAGE_GUIDE.md"
echo ""

info "For detailed documentation, see:"
echo "  - USAGE_GUIDE.md - Complete usage instructions"
echo "  - MCP_SERVER_FIXES.md - Technical details about the fixes"
echo "  - .env.example - Environment variable reference"
echo ""

success "Happy automating! 🚀"
