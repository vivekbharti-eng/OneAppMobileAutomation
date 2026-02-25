# Shell Integration Guide - EcoCash Test Automation

## Overview
Complete shell integration for EcoCash mobile test automation with PowerShell (Windows), Bash (Linux/Mac), and Batch scripts.

---

## PowerShell Scripts (Windows - Recommended)

### 1. **run_ecocash.ps1** - Main Test Runner
Comprehensive test execution with automatic environment validation.

```powershell
# Run smoke tests on real device (default)
.\run_ecocash.ps1

# Run specific tests
.\run_ecocash.ps1 -Device real -Tags "@smoke"
.\run_ecocash.ps1 -Device real -Tags "@login"
.\run_ecocash.ps1 -Device emulator -Tags "@regression"

# Run and open report
.\run_ecocash.ps1 -Report

# Skip clean build
.\run_ecocash.ps1 -Clean:$false
```

**Features:**
- ✓ Automatic prerequisite checks (Java, Maven, ADB, Appium)
- ✓ Device connectivity validation
- ✓ Appium server auto-start
- ✓ Color-coded output
- ✓ Test summary with pass/fail counts
- ✓ Automatic report opening

---

### 2. **quick_test.ps1** - Quick Test Launcher
Simplified runner for common test scenarios.

```powershell
# Quick smoke test
.\quick_test.ps1

# Specific test types
.\quick_test.ps1 smoke
.\quick_test.ps1 login
.\quick_test.ps1 logout
.\quick_test.ps1 regression
.\quick_test.ps1 all
```

---

### 3. **manage_devices.ps1** - Device Management
Android device utilities and app management.

```powershell
# List connected devices
.\manage_devices.ps1 list

# Show device info
.\manage_devices.ps1 info

# Monitor live logs
.\manage_devices.ps1 logcat

# Install EcoCash app
.\manage_devices.ps1 install

# Uninstall app
.\manage_devices.ps1 uninstall

# Take screenshot
.\manage_devices.ps1 screenshot

# Clear app data (logout)
.\manage_devices.ps1 clear
```

---

### 4. **start_appium.ps1** - Appium Server Management
Control Appium server lifecycle.

```powershell
# Start server
.\start_appium.ps1 start

# Stop server
.\start_appium.ps1 stop

# Restart server
.\start_appium.ps1 restart

# Check status
.\start_appium.ps1 status

# Start with custom port and logging
.\start_appium.ps1 start -Port 4725 -Log
```

---

## Batch Scripts (Windows - Basic)

### run_android_real.bat
```batch
run_android_real.bat
```
Run tests on connected real Android device.

### run_android_emulator.bat
```batch
run_android_emulator.bat
```
Run tests on Android emulator.

### run_android.bat
```batch
# Run all tests locally
run_android.bat

# Run with tags
run_android.bat local @smoke

# Run on BrowserStack
run_android.bat browserstack @regression
```

### run_browserstack.bat
```batch
# Run on BrowserStack Android
run_browserstack.bat android

# Run on BrowserStack iOS
run_browserstack.bat ios
```

---

## Bash Scripts (Linux/Mac)

### run_android.sh
```bash
# Make executable
chmod +x run_android.sh

# Run tests
./run_android.sh local @smoke
./run_android.sh browserstack @regression
```

### run_android_real.sh
```bash
chmod +x run_android_real.sh
./run_android_real.sh
```

---

## Common Workflows

### Daily Testing Workflow
```powershell
# 1. Check device connection
.\manage_devices.ps1 list

# 2. Ensure Appium is running
.\start_appium.ps1 status

# 3. Clear app data for fresh test
.\manage_devices.ps1 clear

# 4. Run smoke tests
.\quick_test.ps1 smoke
```

### Full Regression Workflow
```powershell
# Run complete test suite
.\run_ecocash.ps1 -Device real -Tags "@regression" -Report
```

### Debugging Workflow
```powershell
# Clear app data
.\manage_devices.ps1 clear

# Monitor logs in one terminal
.\manage_devices.ps1 logcat

# Run specific test in another terminal
.\run_ecocash.ps1 -Tags "@login"

# Take screenshot if issue occurs
.\manage_devices.ps1 screenshot
```

### CI/CD Integration
```powershell
# Headless execution without pauses
mvn clean test -Plocal-android-real '-Dcucumber.filter.tags=@smoke'
```

---

## Setup Instructions

### PowerShell Execution Policy (First Time)
If you get "script execution disabled" error:

```powershell
# Check current policy
Get-ExecutionPolicy

# Set policy (Run as Administrator)
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Make Scripts Executable (Linux/Mac)
```bash
chmod +x *.sh
```

---

## Script Features Comparison

| Feature | PowerShell (.ps1) | Batch (.bat) | Bash (.sh) |
|---------|-------------------|--------------|------------|
| Color Output | ✓ | ✗ | ✓ |
| Error Handling | ✓ | Limited | ✓ |
| Pre-checks | ✓ | Basic | ✓ |
| Auto Appium Start | ✓ | ✗ | ✓ |
| Test Summary | ✓ | ✗ | ✗ |
| Cross-Platform | Windows | Windows | Linux/Mac |
| Recommended | ✓ | Basic use | ✓ |

---

## Environment Variables

Optional environment variables for advanced configuration:

```powershell
# BrowserStack credentials
$env:BROWSERSTACK_USERNAME = "your_username"
$env:BROWSERSTACK_ACCESS_KEY = "your_key"

# Custom Appium server
$env:APPIUM_HOST = "localhost"
$env:APPIUM_PORT = "4723"

# Device preferences
$env:ANDROID_SERIAL = "10BF7S243X0030Z"  # Force specific device
```

---

## Troubleshooting

### PowerShell Script Won't Run
```powershell
# Run with explicit execution policy
powershell -ExecutionPolicy Bypass -File .\run_ecocash.ps1
```

### Appium Connection Failed
```powershell
# Check if port is in use
netstat -ano | findstr :4723

# Restart Appium
.\start_appium.ps1 restart
```

### Device Not Detected
```powershell
# Check ADB
adb devices

# Restart ADB server
adb kill-server
adb start-server

# Re-check devices
.\manage_devices.ps1 list
```

### App Installation Failed
```powershell
# Update app path in manage_devices.ps1
# Line: $appPath = "C:\path\to\your\EcoCash.apk"

# Try manual install
.\manage_devices.ps1 install
```

---

## Maven Profiles Reference

Available in pom.xml:

- `local-android-real` - Real Android device (auto-detect)
- `local-android-emulator` - Android emulator
- `bs-android` - BrowserStack Android
- `bs-ios` - BrowserStack iOS

---

## Quick Reference Card

```
┌─────────────────────────────────────────────────┐
│  EcoCash Test Automation - Quick Reference      │
├─────────────────────────────────────────────────┤
│  .\quick_test.ps1 smoke     → Run smoke tests   │
│  .\manage_devices.ps1 list  → List devices      │
│  .\manage_devices.ps1 clear → Logout/clean app  │
│  .\start_appium.ps1 status  → Check Appium      │
│  .\run_ecocash.ps1 -Report  → Run + open report │
└─────────────────────────────────────────────────┘
```

---

## Support

For issues or questions:
1. Check logs in `target/logs/`
2. Review test reports in `target/reports/`
3. Run `.\manage_devices.ps1 info` for device diagnostics
4. Use `.\start_appium.ps1 -Log` for detailed Appium logs

---

**Last Updated:** February 10, 2026  
**Framework Version:** 1.0-SNAPSHOT
