@echo off
REM ==============================================
REM Run tests on BrowserStack
REM ==============================================
REM Usage: run_browserstack.bat [android|ios]

set PLATFORM=%1
if "%PLATFORM%"=="" set PLATFORM=android

echo.
echo ================================================
echo Starting tests on BrowserStack - %PLATFORM%
echo ================================================
echo.

REM Validate BrowserStack credentials
if "%BROWSERSTACK_USERNAME%"=="" (
    echo WARNING: BROWSERSTACK_USERNAME not set
    echo Set environment variables or configure in config.properties
    echo.
)

if "%BROWSERSTACK_ACCESS_KEY%"=="" (
    echo WARNING: BROWSERSTACK_ACCESS_KEY not set
    echo Set environment variables or configure in config.properties
    echo.
)

REM Run tests with BrowserStack profile
if "%PLATFORM%"=="android" (
    mvn clean test -Pbs-android
) else if "%PLATFORM%"=="ios" (
    mvn clean test -Pbs-ios
) else (
    echo Invalid platform: %PLATFORM%
    echo Usage: run_browserstack.bat [android^|ios]
    pause
    exit /b 1
)

echo.
echo ================================================
echo Test execution completed
echo Check reports at: target\reports
echo ================================================
pause
