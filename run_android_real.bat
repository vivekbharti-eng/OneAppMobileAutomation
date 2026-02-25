@echo off
REM ==============================================
REM Run EcoCash tests on Android Real Device
REM ==============================================
REM Auto-detects connected Android devices and runs tests

setlocal enabledelayedexpansion

echo.
echo ================================================
echo EcoCash Test Automation - Real Device
echo ================================================
echo.

REM Check if ADB is available
where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] ADB not found in PATH
    echo Please ensure Android SDK is installed and added to PATH
    pause
    exit /b 1
)

echo [INFO] Checking for connected devices...
adb devices -l

REM Count actual devices (skip header line)
for /f "skip=1 tokens=2" %%a in ('adb devices 2^>nul') do (
    if "%%a"=="device" (
        echo [SUCCESS] Android device detected
        goto :device_found
    )
)

echo [ERROR] No Android device connected
echo Please connect a device and try again
pause
exit /b 1

:device_found

REM Check if Appium is running
echo.
echo [INFO] Checking Appium server...
powershell -Command "try { Invoke-WebRequest -Uri 'http://localhost:4723/status' -UseBasicParsing -TimeoutSec 2 | Out-Null; exit 0 } catch { exit 1 }" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Appium server not running
    echo [INFO] Please start Appium: appium
    echo [INFO] Waiting 5 seconds for manual start...
    timeout /t 5 /nobreak
)

REM Run tests with local-android-real profile
echo.
echo [INFO] Starting test execution...
echo [INFO] Profile: local-android-real
echo [INFO] Tags: @smoke (default)
echo.

mvn clean test -Plocal-android-real

REM Check test result
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo [SUCCESS] Test execution completed successfully
    echo ================================================
) else (
    echo.
    echo ================================================
    echo [FAILURE] Test execution failed
    echo ================================================
)

echo.
echo Reports: target\reports
echo Screenshots: target\screenshots
echo Logs: target\logs
echo.
pause
