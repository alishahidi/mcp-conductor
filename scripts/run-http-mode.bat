@echo off
rem MCP Conductor - HTTP Mode Launcher (Windows)
rem Cross-platform startup script for HTTP API mode

setlocal enabledelayedexpansion

echo 🌐 Starting MCP Conductor - HTTP Mode

rem Get project directory
set "PROJECT_DIR=%~dp0.."
cd /d "%PROJECT_DIR%"

echo Project Directory: %PROJECT_DIR%

rem Load environment if exists
if exist "config\http.env" (
    echo Loading HTTP configuration...
    for /f "usebackq tokens=1,* delims==" %%a in ("config\http.env") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
) else (
    echo ⚠️ Warning: config\http.env not found, using defaults
)

rem Check if JAR exists
if not exist "target\mcp-conductor.jar" (
    echo ❌ Error: target\mcp-conductor.jar not found
    echo Please run: mvn clean package -DskipTests
    pause
    exit /b 1
)

rem Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Java not found
    echo Please install Java 21+ and add to PATH
    pause
    exit /b 1
)

if not defined SERVER_PORT set SERVER_PORT=8080
echo Starting HTTP server on port %SERVER_PORT%...

java -jar target\mcp-conductor.jar

pause