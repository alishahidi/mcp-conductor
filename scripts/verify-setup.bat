@echo off
rem MCP Conductor - Production Setup Verification (Windows)
rem Verify all components are properly configured

setlocal enabledelayedexpansion

echo 🔍 MCP Conductor - Production Setup Verification
echo ================================================

rem Get project directory
set "PROJECT_DIR=%~dp0.."
cd /d "%PROJECT_DIR%"

set success_count=0
set total_checks=0

rem Check function simulation
:check
set /a total_checks+=1
if %~2 (
    echo %~1: ✓ PASS
    set /a success_count+=1
) else (
    echo %~1: ✗ FAIL
)
goto :eof

echo.
echo 📁 Directory Structure Checks
echo -----------------------------
if exist "bin" (call :check "bin/ directory exists" 1) else (call :check "bin/ directory exists" 0)
if exist "config" (call :check "config/ directory exists" 1) else (call :check "config/ directory exists" 0)
if exist "scripts" (call :check "scripts/ directory exists" 1) else (call :check "scripts/ directory exists" 0)
if exist "bin\mcp-server.py" (call :check "MCP STDIO server exists" 1) else (call :check "MCP STDIO server exists" 0)
if exist "scripts\run-http-mode.bat" (call :check "HTTP launcher exists" 1) else (call :check "HTTP launcher exists" 0)
if exist "scripts\run-stdio-mode.bat" (call :check "STDIO launcher exists" 1) else (call :check "STDIO launcher exists" 0)

echo.
echo ⚙️ Configuration Checks
echo ----------------------
if exist "config\example.env" (call :check "Example config exists" 1) else (call :check "Example config exists" 0)
if exist "config\http.env" (call :check "HTTP config exists" 1) else (call :check "HTTP config exists" 0)
if exist "config\stdio.env" (call :check "STDIO config exists" 1) else (call :check "STDIO config exists" 0)
if exist "config\prometheus.yml" (call :check "Prometheus config exists" 1) else (call :check "Prometheus config exists" 0)

echo.
echo 🔧 Build Checks
echo ---------------
if exist "pom.xml" (call :check "Maven POM exists" 1) else (call :check "Maven POM exists" 0)
if exist "target\mcp-conductor.jar" (call :check "JAR file exists" 1) else (call :check "JAR file exists" 0)
if exist "src\main\java" (call :check "Source directory exists" 1) else (call :check "Source directory exists" 0)

echo.
echo 🖥️ System Requirements
echo ----------------------
java -version >nul 2>&1
if errorlevel 1 (call :check "Java available" 0) else (call :check "Java available" 1)

python --version >nul 2>&1 || python3 --version >nul 2>&1
if errorlevel 1 (call :check "Python available" 0) else (call :check "Python available" 1)

mvn --version >nul 2>&1
if errorlevel 1 (call :check "Maven available" 0) else (call :check "Maven available" 1)

echo.
echo 📚 Documentation
echo ----------------
if exist "README.md" (call :check "README.md exists" 1) else (call :check "README.md exists" 0)
if exist "CLAUDE.md" (call :check "CLAUDE.md exists" 1) else (call :check "CLAUDE.md exists" 0)
if exist "LICENSE" (call :check "License exists" 1) else (call :check "License exists" 0)

echo.
echo 🐳 Container Support
echo -------------------
if exist "Dockerfile" (call :check "Dockerfile exists" 1) else (call :check "Dockerfile exists" 0)
if exist "docker-compose.yml" (call :check "Docker Compose exists" 1) else (call :check "Docker Compose exists" 0)

echo.
echo 📊 Results
echo ==========
echo Passed: %success_count%/%total_checks% checks

if %success_count%==%total_checks% (
    echo 🎉 All checks passed! Production setup verified.
    echo.
    echo 🚀 Ready to run:
    echo   HTTP Mode:  .\scripts\run-http-mode.bat
    echo   STDIO Mode: .\scripts\run-stdio-mode.bat
    echo.
    echo 📋 Next steps:
    echo   1. Configure environment in config\http.env
    echo   2. Set up SSH keys for remote access
    echo   3. Test HTTP mode with curl or browser
    echo   4. Register with Claude Code for STDIO mode
) else (
    echo ❌ Some checks failed. Please fix issues before deployment.
)

pause