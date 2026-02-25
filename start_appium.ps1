#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Start and manage Appium server
.DESCRIPTION
    Utility to start, stop, and check Appium server status
.PARAMETER Action
    Action: start, stop, restart, status (default: start)
.PARAMETER Port
    Port number for Appium server (default: 4723)
.PARAMETER Log
    Enable detailed logging (default: false)
.EXAMPLE
    .\start_appium.ps1
    .\start_appium.ps1 start -Port 4723
    .\start_appium.ps1 stop
    .\start_appium.ps1 restart
    .\start_appium.ps1 status
#>

param(
    [Parameter(Position=0)]
    [ValidateSet('start', 'stop', 'restart', 'status')]
    [string]$Action = 'start',
    
    [int]$Port = 4723,
    [switch]$Log = $false
)

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

function Test-AppiumRunning {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$Port/status" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

function Get-AppiumProcess {
    return Get-Process | Where-Object { $_.ProcessName -like "*node*" -and $_.CommandLine -like "*appium*" }
}

function Stop-AppiumServer {
    Write-Info "Stopping Appium server..."
    
    # Find Appium processes
    $processes = Get-Process | Where-Object { 
        $_.ProcessName -eq "node" -or $_.ProcessName -eq "appium"
    }
    
    if ($processes) {
        $processes | ForEach-Object {
            try {
                Stop-Process -Id $_.Id -Force
                Write-Success "Stopped process: $($_.Id)"
            } catch {
                Write-Error-Custom "Failed to stop process: $($_.Id)"
            }
        }
    } else {
        Write-Info "No Appium processes found"
    }
}

function Start-AppiumServer {
    Write-Info "Starting Appium server on port $Port..."
    
    $logFile = "target\logs\appium_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"
    New-Item -ItemType Directory -Force -Path "target\logs" | Out-Null
    
    $appiumArgs = @(
        "--port", $Port,
        "--log-timestamp",
        "--local-timezone"
    )
    
    if ($Log) {
        $appiumArgs += @("--log", $logFile, "--log-level", "debug")
        Write-Info "Detailed logging enabled: $logFile"
    } else {
        $appiumArgs += @("--log-level", "info")
    }
    
    try {
        $process = Start-Process -FilePath "appium" -ArgumentList $appiumArgs -WindowStyle Hidden -PassThru
        Write-Info "Appium process started (PID: $($process.Id))"
        
        Write-Info "Waiting for server to start..."
        $maxAttempts = 10
        $attempt = 0
        
        while ($attempt -lt $maxAttempts) {
            Start-Sleep -Seconds 1
            if (Test-AppiumRunning) {
                Write-Success "Appium server is running on http://localhost:$Port"
                Write-Info "Process ID: $($process.Id)"
                if ($Log) {
                    Write-Info "Log file: $logFile"
                }
                return $true
            }
            $attempt++
        }
        
        Write-Error-Custom "Appium server failed to start within timeout"
        return $false
        
    } catch {
        Write-Error-Custom "Failed to start Appium: $_"
        return $false
    }
}

# Main execution
Write-Header "Appium Server Management"

switch ($Action) {
    'start' {
        if (Test-AppiumRunning) {
            Write-Success "Appium server is already running on port $Port"
            exit 0
        }
        
        Start-AppiumServer
    }
    
    'stop' {
        if (-not (Test-AppiumRunning)) {
            Write-Info "Appium server is not running"
            exit 0
        }
        
        Stop-AppiumServer
        Start-Sleep -Seconds 2
        
        if (-not (Test-AppiumRunning)) {
            Write-Success "Appium server stopped successfully"
        } else {
            Write-Error-Custom "Failed to stop Appium server"
        }
    }
    
    'restart' {
        Write-Info "Restarting Appium server..."
        
        if (Test-AppiumRunning) {
            Stop-AppiumServer
            Start-Sleep -Seconds 2
        }
        
        Start-AppiumServer
    }
    
    'status' {
        if (Test-AppiumRunning) {
            Write-Success "Appium server is running on http://localhost:$Port"
            
            try {
                $response = Invoke-RestMethod -Uri "http://localhost:$Port/status" -Method Get
                Write-Info "Server Status:"
                Write-Host "  Ready: $($response.value.ready)" -ForegroundColor Cyan
                Write-Host "  Build: $($response.value.build.version)" -ForegroundColor Cyan
            } catch {
                Write-Info "Could not retrieve detailed status"
            }
        } else {
            Write-Info "Appium server is not running"
        }
    }
}
