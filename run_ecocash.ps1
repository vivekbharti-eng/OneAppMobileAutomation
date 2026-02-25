#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Run EcoCash automation tests on Android device
.DESCRIPTION
    Comprehensive test runner for EcoCash mobile app with automatic environment setup
.PARAMETER Device
    Device type: real, emulator (default: real)
.PARAMETER Tags
    Cucumber tags to filter tests (default: @smoke)
.PARAMETER Clean
    Perform clean build before tests (default: true)
.PARAMETER Report
    Open report after execution (default: false)
.EXAMPLE
    .\run_ecocash.ps1
    .\run_ecocash.ps1 -Device real -Tags "@smoke"
    .\run_ecocash.ps1 -Device emulator -Tags "@regression" -Report
#>

param(
    [Parameter(Position=0)]
    [ValidateSet('real', 'emulator')]
    [string]$Device = 'real',
    
    [Parameter(Position=1)]
    [string]$Tags = '@smoke',
    
    [switch]$Clean = $true,
    [switch]$Report = $false
)

# Color functions
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Header($message) {
    Write-Host ""
    Write-ColorOutput Yellow "================================================"
    Write-ColorOutput Yellow $message
    Write-ColorOutput Yellow "================================================"
    Write-Host ""
}

function Write-Success($message) {
    Write-ColorOutput Green "✓ $message"
}

function Write-Error-Custom($message) {
    Write-ColorOutput Red "✗ $message"
}

function Write-Info($message) {
    Write-ColorOutput Cyan "ℹ $message"
}

# Main execution
Write-Header "EcoCash Test Automation Runner"

Write-Info "Configuration:"
Write-Host "  Device Type: $Device"
Write-Host "  Test Tags: $Tags"
Write-Host "  Clean Build: $Clean"
Write-Host ""

# Check prerequisites
Write-Info "Checking prerequisites..."

# Check Java
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Success "Java found: $javaVersion"
} catch {
    Write-Error-Custom "Java not found. Please install Java JDK 11 or higher"
    exit 1
}

# Check Maven
try {
    $mavenVersion = mvn -version | Select-Object -First 1
    Write-Success "Maven found: $mavenVersion"
} catch {
    Write-Error-Custom "Maven not found. Please install Maven 3.6+"
    exit 1
}

# Check ADB
try {
    $adbVersion = adb version 2>&1 | Select-Object -First 1
    Write-Success "ADB found: $adbVersion"
} catch {
    Write-Error-Custom "ADB not found. Please install Android SDK Platform Tools"
    exit 1
}

# Check for connected devices
Write-Info "Checking connected devices..."
$devices = adb devices | Select-String "device$"
if ($devices.Count -eq 0) {
    Write-Error-Custom "No Android device/emulator connected"
    Write-Host "Please connect a device or start an emulator"
    exit 1
}
Write-Success "Device(s) connected:"
adb devices -l | Select-Object -Skip 1

Write-Host ""

# Check Appium server
Write-Info "Checking Appium server..."
try {
    $appiumCheck = Invoke-WebRequest -Uri "http://localhost:4723/status" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
    Write-Success "Appium server is running"
} catch {
    Write-Error-Custom "Appium server not running on port 4723"
    Write-Info "Starting Appium server in background..."
    
    try {
        $appiumProcess = Start-Process -FilePath "appium" -WindowStyle Hidden -PassThru
        Write-Info "Waiting for Appium to start..."
        Start-Sleep -Seconds 5
        
        $appiumCheck = Invoke-WebRequest -Uri "http://localhost:4723/status" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        Write-Success "Appium server started successfully"
    } catch {
        Write-Error-Custom "Failed to start Appium. Please start it manually: appium"
        exit 1
    }
}

Write-Host ""

# Determine Maven profile
$profile = "local-android-$Device"
Write-Info "Using Maven profile: $profile"

# Build Maven command
$mavenCmd = "mvn"
if ($Clean) {
    $mavenCmd += " clean"
}
$mavenCmd += " test -P$profile"

if ($Tags) {
    $mavenCmd += " '-Dcucumber.filter.tags=$Tags'"
}

# Run tests
Write-Header "Executing EcoCash Tests"
Write-Info "Command: $mavenCmd"
Write-Host ""

$startTime = Get-Date

# Execute Maven command
Invoke-Expression $mavenCmd

$endTime = Get-Date
$duration = $endTime - $startTime

Write-Host ""
Write-Header "Test Execution Completed"

Write-Info "Duration: $($duration.ToString('mm\:ss'))"
Write-Info "Reports location: target\reports\"
Write-Info "Screenshots: target\screenshots\"
Write-Info "Logs: target\logs\"

# Check test results
$testResults = Test-Path "target\surefire-reports\testng-results.xml"
if ($testResults) {
    [xml]$xmlResults = Get-Content "target\surefire-reports\testng-results.xml"
    $passed = $xmlResults.'testng-results'.'passed'
    $failed = $xmlResults.'testng-results'.'failed'
    $skipped = $xmlResults.'testng-results'.'skipped'
    
    Write-Host ""
    Write-Info "Test Summary:"
    Write-Success "Passed: $passed"
    if ($failed -gt 0) {
        Write-Error-Custom "Failed: $failed"
    } else {
        Write-Host "  Failed: $failed"
    }
    Write-Host "  Skipped: $skipped"
}

# Open report if requested
if ($Report) {
    Write-Host ""
    Write-Info "Opening test report..."
    $reportPath = "target\reports"
    if (Test-Path $reportPath) {
        Start-Process explorer $reportPath
    }
}

Write-Host ""
Write-ColorOutput Green "[OK] Done!"
