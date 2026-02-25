@echo off
REM ==============================================
REM EcoCash Test Automation Launcher
REM ==============================================
REM Main launcher with menu selection

:menu
cls
echo.
echo ================================================
echo     EcoCash Test Automation Framework
echo ================================================
echo.
echo Select an option:
echo.
echo   1. Run Smoke Tests (Real Device)
echo   2. Run Login Tests (Real Device)
echo   3. Run All Tests (Real Device)
echo   4. Run Tests on Emulator
echo   5. Manage Devices (List/Info)
echo   6. Start Appium Server
echo   7. Clear App Data (Logout)
echo   8. Open Test Reports
echo   9. Use PowerShell Scripts (Advanced)
echo   0. Exit
echo.
echo ================================================

set /p choice="Enter your choice (0-9): "

if "%choice%"=="1" goto smoke
if "%choice%"=="2" goto login
if "%choice%"=="3" goto all
if "%choice%"=="4" goto emulator
if "%choice%"=="5" goto devices
if "%choice%"=="6" goto appium
if "%choice%"=="7" goto clear
if "%choice%"=="8" goto reports
if "%choice%"=="9" goto powershell
if "%choice%"=="0" goto exit

echo Invalid choice! Press any key to continue...
pause >nul
goto menu

:smoke
echo.
echo Running Smoke Tests...
mvn clean test -Plocal-android-real "-Dcucumber.filter.tags=@smoke"
pause
goto menu

:login
echo.
echo Running Login Tests...
mvn clean test -Plocal-android-real "-Dcucumber.filter.tags=@login"
pause
goto menu

:all
echo.
echo Running All Tests...
mvn clean test -Plocal-android-real
pause
goto menu

:emulator
echo.
echo Running Tests on Emulator...
call run_android_emulator.bat
goto menu

:devices
echo.
echo Connected Devices:
adb devices -l
echo.
pause
goto menu

:appium
echo.
echo Starting Appium Server...
echo Server will run in background. Close the Appium window to stop.
start "Appium Server" appium
timeout /t 3
goto menu

:clear
echo.
echo Clearing EcoCash App Data...
adb shell pm clear zw.co.cassavasmartech.ecocash
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] App data cleared
) else (
    echo [ERROR] Failed to clear app data
)
pause
goto menu

:reports
echo.
echo Opening Reports Folder...
if exist "target\reports" (
    explorer target\reports
) else (
    echo No reports folder found. Run tests first.
)
pause
goto menu

:powershell
echo.
echo ================================================
echo PowerShell Scripts (Advanced Users)
echo ================================================
echo.
echo Available scripts:
echo   .\run_ecocash.ps1      - Main test runner
echo   .\quick_test.ps1       - Quick launcher
echo   .\manage_devices.ps1   - Device management
echo   .\start_appium.ps1     - Appium control
echo.
echo Example:
echo   powershell -ExecutionPolicy Bypass -File .\run_ecocash.ps1
echo.
echo See SHELL_INTEGRATION.md for detailed documentation
echo.
pause
goto menu

:exit
echo.
echo Exiting... Goodbye!
timeout /t 2
exit /b 0
