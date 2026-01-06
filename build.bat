@echo off
REM Quick build script for QOL Mod
REM Use this instead of gradlew.bat directly if you have PowerShell execution issues

echo ========================================
echo QOL Mod - Quick Build Script
echo ========================================
echo.

if "%1"=="" (
    echo Usage: build.bat [command]
    echo.
    echo Commands:
    echo   build       - Build the mod
    echo   clean       - Clean build files
    echo   genSources  - Generate Minecraft sources
    echo   runClient   - Run Minecraft client
    echo   runServer   - Run Minecraft server
    echo   idea        - Sync IntelliJ IDEA
    echo.
    goto :eof
)

echo Running: gradlew.bat %*
echo.
cmd /c gradlew.bat %*
