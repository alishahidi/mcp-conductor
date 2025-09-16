@echo off
rem MCP Conductor - STDIO Mode Launcher (Windows)
rem Production MCP STDIO server for Claude Code integration

setlocal enabledelayedexpansion

echo 🔌 Starting MCP Conductor - STDIO Mode

rem Get project directory
set "PROJECT_DIR=%~dp0.."
cd /d "%PROJECT_DIR%"

echo Project Directory: %PROJECT_DIR%

rem Load environment if exists
if exist "config\stdio.env" (
    echo Loading STDIO configuration...
    for /f "usebackq tokens=1,* delims==" %%a in ("config\stdio.env") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
) else (
    echo ⚠️ Warning: config\stdio.env not found, using defaults
)

rem Check Python
python --version >nul 2>&1
if errorlevel 1 (
    python3 --version >nul 2>&1
    if errorlevel 1 (
        echo ❌ Error: Python not found
        echo Please install Python 3.8+ and add to PATH
        pause
        exit /b 1
    ) else (
        set PYTHON_CMD=python3
    )
) else (
    set PYTHON_CMD=python
)

rem Check if mcp-server.py exists
if not exist "bin\mcp-server.py" (
    echo ❌ Error: bin\mcp-server.py not found
    echo Please ensure the MCP server file exists
    pause
    exit /b 1
)

echo Starting MCP STDIO server...
%PYTHON_CMD% bin\mcp-server.py