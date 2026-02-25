@echo off
REM ==============================================
REM Run EcoCash tests on Android Emulator
REM ==============================================

setlocal enabledelayedexpansion

echo.
echo ================================================
echo EcoCash Test Automation - Emulator
echo ================================================
echo.

REM Check if ADB is available
where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] ADB not found in PATH
    pause
    exit /b 1
)

echo [INFO] Checking for emulator...
adb devices -l | findstr "emulator"
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] No emulator detected
    echo [INFO] Starting emulator (if available)...
    echo Please wait or start emulator manually
    timeout /t 3 /nobreak
)

echo.
echo [INFO] Running tests on emulator...
echo.

mvn clean test -Plocal-android-emulator

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Test execution completed
) else (
    echo.
    echo [FAILURE] Test execution failed
)

echo.
echo Check reports at: target\reports
echo.
pause
