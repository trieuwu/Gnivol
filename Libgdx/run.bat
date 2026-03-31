@echo off
title Gnivol Game Launcher
echo ========================================
echo   Gnivol Game - Build and Run
echo ========================================
echo.

cd /d "%~dp0"

echo Building and launching game...
echo The game will auto-rebuild when you change code.
echo Press Ctrl+C to stop.
echo.

call gradlew.bat desktop:run --continuous
pause
