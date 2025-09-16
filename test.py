#!/usr/bin/env python3
"""
Test script to verify MCP STDIO communication
Run this to test your MCP server before connecting to Claude Code
"""

import json
import subprocess
import sys
import time
from pathlib import Path

def test_mcp_server():
    """Test MCP server communication"""
    
    project_dir = Path(__file__).parent
    server_script = project_dir / "bin" / "mcp-server.py"
    
    if not server_script.exists():
        print(f"❌ Error: {server_script} not found")
        return False
        
    print("🧪 Testing MCP Server...")
    
    # Start the server
    process = subprocess.Popen(
        [sys.executable, str(server_script)],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )
    
    try:
        # Test 1: Initialize
        print("\n1️⃣ Testing initialize...")
        request = {
            "jsonrpc": "2.0",
            "id": 1,
            "method": "initialize",
            "params": {
                "protocolVersion": "2024-11-05",
                "capabilities": {},
                "clientInfo": {
                    "name": "test-client",
                    "version": "1.0.0"
                }
            }
        }
        
        process.stdin.write(json.dumps(request) + "\n")
        process.stdin.flush()
        
        response_line = process.stdout.readline()
        if response_line:
            response = json.loads(response_line)
            if response.get("result", {}).get("serverInfo", {}).get("name") == "mcp-conductor":
                print("✅ Initialize successful")
            else:
                print(f"❌ Unexpected response: {response}")
                return False
        else:
            print("❌ No response received")
            return False
            
        # Test 2: List tools
        print("\n2️⃣ Testing tools/list...")
        request = {
            "jsonrpc": "2.0",
            "id": 2,
            "method": "tools/list"
        }
        
        process.stdin.write(json.dumps(request) + "\n")
        process.stdin.flush()
        
        response_line = process.stdout.readline()
        if response_line:
            response = json.loads(response_line)
            tools = response.get("result", {}).get("tools", [])
            print(f"✅ Found {len(tools)} tools:")
            for tool in tools:
                print(f"   - {tool['name']}: {tool['description']}")
        else:
            print("❌ No response received")
            return False
            
        # Test 3: Call a tool
        print("\n3️⃣ Testing tool execution...")
        request = {
            "jsonrpc": "2.0",
            "id": 3,
            "method": "tools/call",
            "params": {
                "name": "get_system_info",
                "arguments": {
                    "serverName": "localhost"
                }
            }
        }
        
        process.stdin.write(json.dumps(request) + "\n")
        process.stdin.flush()
        
        response_line = process.stdout.readline()
        if response_line:
            response = json.loads(response_line)
            content = response.get("result", {}).get("content", [])
            if content and content[0].get("text"):
                print("✅ Tool execution successful")
                print(f"   Response preview: {content[0]['text'][:100]}...")
            else:
                print(f"❌ Unexpected response: {response}")
                return False
        else:
            print("❌ No response received")
            return False
            
        print("\n✅ All tests passed! MCP server is working correctly.")
        return True
        
    except Exception as e:
        print(f"❌ Test failed with error: {e}")
        return False
        
    finally:
        # Cleanup
        process.terminate()
        process.wait(timeout=5)
        
        # Check stderr for any errors
        stderr = process.stderr.read()
        if stderr:
            print("\n📝 Server logs:")
            print(stderr)

if __name__ == "__main__":
    success = test_mcp_server()
    sys.exit(0 if success else 1)
