@echo off
REM Quick launcher for PowerShell test runner
REM Double-click this file to run smoke tests

echo Starting EcoCash Smoke Tests...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0run_ecocash.ps1" -Device real -Tags "@smoke"

echo.
echo Press any key to exit...
pause >nul
