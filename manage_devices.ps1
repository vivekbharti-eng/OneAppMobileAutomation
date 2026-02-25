#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Android device management utility
.DESCRIPTION
    Helper script to manage Android devices and emulators
.PARAMETER Action
    Action to perform: list, info, logcat, install, uninstall, screenshot, clear
.EXAMPLE
    .\manage_devices.ps1 list
    .\manage_devices.ps1 info
    .\manage_devices.ps1 logcat
    .\manage_devices.ps1 install
    .\manage_devices.ps1 uninstall
    .\manage_devices.ps1 screenshot
    .\manage_devices.ps1 clear
#>

param(
    [Parameter(Position=0, Mandatory=$false)]
    [ValidateSet('list', 'info', 'logcat', 'install', 'uninstall', 'screenshot', 'clear', 'help')]
    [string]$Action = 'list'
)

$appPackage = "zw.co.cassavasmartech.ecocash"
$appPath = "C:\Users\vivek.bharti\Downloads\EcoCash Preprod (1).apk"

function Write-Header($message) {
    Write-Host ""
    Write-Host "================================================" -ForegroundColor Yellow
    Write-Host $message -ForegroundColor Yellow
    Write-Host "================================================" -ForegroundColor Yellow
    Write-Host ""
}

function Write-Success($message) {
    Write-Host "✓ $message" -ForegroundColor Green
}

function Write-Error-Custom($message) {
    Write-Host "✗ $message" -ForegroundColor Red
}

function Write-Info($message) {
    Write-Host "ℹ $message" -ForegroundColor Cyan
}

function Show-Help {
    Write-Header "Android Device Management - EcoCash"
    Write-Host "Available commands:"
    Write-Host ""
    Write-Host "  list         - List all connected devices" -ForegroundColor Green
    Write-Host "  info         - Show detailed device information" -ForegroundColor Green
    Write-Host "  logcat       - Show live device logs" -ForegroundColor Green
    Write-Host "  install      - Install EcoCash app on device" -ForegroundColor Green
    Write-Host "  uninstall    - Uninstall EcoCash app from device" -ForegroundColor Green
    Write-Host "  screenshot   - Take device screenshot" -ForegroundColor Green
    Write-Host "  clear        - Clear EcoCash app data" -ForegroundColor Green
    Write-Host "  help         - Show this help" -ForegroundColor Green
    Write-Host ""
}

function Get-ConnectedDevices {
    $devices = adb devices | Select-String "device$"
    return $devices
}

switch ($Action) {
    'list' {
        Write-Header "Connected Android Devices"
        adb devices -l
        Write-Host ""
        $devices = Get-ConnectedDevices
        Write-Info "Total devices: $($devices.Count)"
    }
    
    'info' {
        Write-Header "Device Information"
        
        $devices = Get-ConnectedDevices
        if ($devices.Count -eq 0) {
            Write-Error-Custom "No devices connected"
            exit 1
        }
        
        Write-Info "Device Properties:"
        Write-Host "  Model: $(adb shell getprop ro.product.model)" -ForegroundColor Cyan
        Write-Host "  Brand: $(adb shell getprop ro.product.brand)" -ForegroundColor Cyan
        Write-Host "  Android Version: $(adb shell getprop ro.build.version.release)" -ForegroundColor Cyan
        Write-Host "  SDK Level: $(adb shell getprop ro.build.version.sdk)" -ForegroundColor Cyan
        Write-Host "  Serial: $(adb get-serialno)" -ForegroundColor Cyan
        
        Write-Host ""
        Write-Info "EcoCash App Status:"
        $installed = adb shell pm list packages | Select-String $appPackage
        if ($installed) {
            Write-Success "App installed: $appPackage"
            $version = adb shell dumpsys package $appPackage | Select-String "versionName"
            if ($version) {
                Write-Host "  $version" -ForegroundColor Cyan
            }
        } else {
            Write-Error-Custom "App not installed"
        }
    }
    
    'logcat' {
        Write-Header "Device Logs (Press Ctrl+C to stop)"
        Write-Info "Filtering for: $appPackage"
        Write-Host ""
        adb logcat | Select-String -Pattern $appPackage
    }
    
    'install' {
        Write-Header "Installing EcoCash App"
        
        if (-not (Test-Path $appPath)) {
            Write-Error-Custom "APK not found at: $appPath"
            Write-Info "Update the path in manage_devices.ps1"
            exit 1
        }
        
        Write-Info "Installing from: $appPath"
        adb install -r $appPath
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "App installed successfully"
        } else {
            Write-Error-Custom "Installation failed"
        }
    }
    
    'uninstall' {
        Write-Header "Uninstalling EcoCash App"
        
        Write-Info "Uninstalling: $appPackage"
        adb uninstall $appPackage
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "App uninstalled successfully"
        } else {
            Write-Error-Custom "Uninstallation failed"
        }
    }
    
    'screenshot' {
        Write-Header "Taking Screenshot"
        
        $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
        $devicePath = "/sdcard/screenshot_$timestamp.png"
        $localPath = "target\screenshots\manual_$timestamp.png"
        
        Write-Info "Capturing screenshot..."
        adb shell screencap -p $devicePath
        
        Write-Info "Pulling screenshot..."
        New-Item -ItemType Directory -Force -Path "target\screenshots" | Out-Null
        adb pull $devicePath $localPath
        
        Write-Info "Cleaning up device..."
        adb shell rm $devicePath
        
        Write-Success "Screenshot saved: $localPath"
        
        # Open screenshot
        Start-Process $localPath
    }
    
    'clear' {
        Write-Header "Clearing EcoCash App Data"
        
        Write-Info "Clearing app data: $appPackage"
        adb shell pm clear $appPackage
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "App data cleared successfully"
            Write-Info "The app is now in fresh state"
        } else {
            Write-Error-Custom "Failed to clear app data"
        }
    }
    
    'help' {
        Show-Help
    }
    
    default {
        Show-Help
    }
}
