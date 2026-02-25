@echo off
REM Script to run Android tests on Windows
REM Usage: 
REM   run_android.bat                    (runs on local device)
REM   run_android.bat local @smoke       (runs smoke tests locally)
REM   run_android.bat browserstack       (runs on BrowserStack)
REM   run_android.bat browserstack @smoke (runs smoke tests on BrowserStack)

echo ==========================================
echo Running Android Tests
echo ==========================================

set EXECUTION_TYPE=%1
set TAGS=%2

REM Default to local execution if not specified
if "%EXECUTION_TYPE%"=="" set EXECUTION_TYPE=local

REM Validate execution type
if "%EXECUTION_TYPE%"=="local" (
    echo Execution Type: Local
    echo.
    
    REM Check if device is connected for local execution
    adb devices > nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo Error: ADB not found or no device connected
        echo Please connect a device or start an emulator
        exit /b 1
    )
    
    echo Device connected:
    adb devices
    
    echo.
    echo Make sure Appium server is running...
    echo If not started, open a new terminal and run: appium
    echo.
    timeout /t 3
    
) else if "%EXECUTION_TYPE%"=="browserstack" (
    echo Execution Type: BrowserStack
    echo.
    echo Make sure BrowserStack credentials are configured
    echo.
    timeout /t 2
) else (
    REM If first parameter looks like tags, use it as tags with local execution
    set TAGS=%EXECUTION_TYPE%
    set EXECUTION_TYPE=local
    echo Execution Type: Local (default)
    echo.
    
    REM Check device for local execution
    adb devices > nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo Error: ADB not found or no device connected
        exit /b 1
    )
)

REM Run tests based on profile
set PROFILE=%EXECUTION_TYPE%-android

if "%TAGS%"=="" (
    echo Running all Android tests on %EXECUTION_TYPE%...
    mvn clean test -P%PROFILE%
) else (
    echo Running Android tests with tags: %TAGS% on %EXECUTION_TYPE%...
    mvn clean test -P%PROFILE% -Dcucumber.filter.tags="%TAGS%"
)

echo ==========================================
echo Test execution completed!
echo Check reports at: target\reports\
echo ==========================================
pause
