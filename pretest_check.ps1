
# =============================================================================
# Pre-Test Validation Script
# Checks: 1) Appium is up  2) ADB device connected  3) UDID & deviceName synced
# Usage:  .\pretest_check.ps1                     (check only)
#         .\pretest_check.ps1 -RunTests           (check + run all tests)
#         .\pretest_check.ps1 -Tags "@smoke"      (check + run tagged tests)
# =============================================================================

param(
    [switch]$RunTests,
    [string]$Tags = ""
)

$RED    = "`e[31m"
$GREEN  = "`e[32m"
$YELLOW = "`e[33m"
$CYAN   = "`e[36m"
$BOLD   = "`e[1m"
$RESET  = "`e[0m"

$CONFIG_FILE  = "src\test\resources\config.properties"
$APPIUM_URL   = "http://127.0.0.1:4723/status"
$PASS = "$GREEN✓$RESET"
$FAIL = "$RED✗$RESET"
$WARN = "$YELLOW⚠$RESET"

$allOk = $true

function Write-Header($msg) {
    Write-Host ""
    Write-Host "$CYAN$BOLD=============================================$RESET"
    Write-Host "$CYAN$BOLD  $msg$RESET"
    Write-Host "$CYAN$BOLD=============================================$RESET"
}

function Read-ConfigProperty($key) {
    $line = Get-Content $CONFIG_FILE | Where-Object { $_ -match "^\s*$key\s*=" } | Select-Object -First 1
    if ($line) { return ($line -split "=", 2)[1].Trim() }
    return $null
}

function Set-ConfigProperty($key, $value) {
    $content = Get-Content $CONFIG_FILE
    $updated = $content | ForEach-Object {
        if ($_ -match "^\s*$key\s*=") { "$key=$value" } else { $_ }
    }
    $updated | Set-Content $CONFIG_FILE -Encoding UTF8
    Write-Host "  $WARN Updated config: $key=$value"
}

# =============================================================================
# STEP 1: Check Appium Server
# =============================================================================
Write-Header "STEP 1: Appium Server Check"

$appiumRunning = $false
try {
    $resp = Invoke-WebRequest -Uri $APPIUM_URL -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
    if ($resp.StatusCode -eq 200) {
        $appiumRunning = $true
        Write-Host "$PASS Appium server is RUNNING at $APPIUM_URL"
    }
} catch {
    Write-Host "$FAIL Appium server is NOT running at $APPIUM_URL"
}

if (-not $appiumRunning) {
    Write-Host "$WARN Attempting to start Appium..."
    try {
        Start-Process -FilePath "appium" -ArgumentList "--address 127.0.0.1 --port 4723 --log-level warn --relaxed-security" -WindowStyle Hidden
        Write-Host "  Waiting for Appium to start..."
        $waited = 0
        while ($waited -lt 30) {
            Start-Sleep -Seconds 1
            $waited++
            try {
                $resp = Invoke-WebRequest -Uri $APPIUM_URL -TimeoutSec 2 -UseBasicParsing -ErrorAction Stop
                if ($resp.StatusCode -eq 200) {
                    $appiumRunning = $true
                    Write-Host "$PASS Appium started successfully (took $waited s)"
                    break
                }
            } catch {}
        }
        if (-not $appiumRunning) {
            Write-Host "$FAIL Appium did not start within 30 seconds"
            Write-Host "  $YELLOW→ Run manually: appium --address 127.0.0.1 --port 4723$RESET"
            $allOk = $false
        }
    } catch {
        Write-Host "$FAIL Could not start Appium: $_"
        Write-Host "  $YELLOW→ Ensure 'appium' is installed: npm install -g appium$RESET"
        $allOk = $false
    }
}

# =============================================================================
# STEP 2: ADB Devices Check
# =============================================================================
Write-Header "STEP 2: ADB Devices Check"

$adbAvailable = $false
try {
    $null = adb version 2>&1
    $adbAvailable = $true
    Write-Host "$PASS ADB is available"
} catch {
    Write-Host "$FAIL ADB not found. Ensure Android SDK platform-tools is in PATH"
    $allOk = $false
}

$connectedDevices = @()
if ($adbAvailable) {
    $adbOutput = adb devices 2>&1
    Write-Host ""
    Write-Host "$CYAN  adb devices output:$RESET"
    $adbOutput | ForEach-Object { Write-Host "    $_" }

    # Parse device lines (skip header and empty lines)
    $connectedDevices = $adbOutput | Select-Object -Skip 1 | Where-Object {
        $_ -match "\S" -and $_ -match "device$"
    } | ForEach-Object { ($_ -split "\s+")[0] }

    if ($connectedDevices.Count -eq 0) {
        Write-Host ""
        Write-Host "$FAIL No authorized devices found in 'adb devices'"
        Write-Host "  $YELLOW→ Connect device via USB and enable USB Debugging$RESET"
        Write-Host "  $YELLOW→ Accept the 'Allow USB Debugging' prompt on your device$RESET"
        $allOk = $false
    } else {
        Write-Host ""
        Write-Host "$PASS Found $($connectedDevices.Count) device(s): $($connectedDevices -join ', ')"
    }
}

# =============================================================================
# STEP 3: Validate & Sync UDID + DeviceName in config.properties
# =============================================================================
Write-Header "STEP 3: Config Validation (UDID & DeviceName)"

$configUdid       = Read-ConfigProperty "android.udid"
$configDeviceName = Read-ConfigProperty "android.deviceName"
$configDeviceType = Read-ConfigProperty "android.deviceType"

Write-Host "  Config UDID        : $CYAN$configUdid$RESET"
Write-Host "  Config DeviceName  : $CYAN$configDeviceName$RESET"
Write-Host "  Config DeviceType  : $CYAN$configDeviceType$RESET"
Write-Host ""

if ($connectedDevices.Count -gt 0 -and $adbAvailable) {

    # --- UDID validation ---
    $matchedUdid = $null
    if ($configUdid -and ($connectedDevices -contains $configUdid)) {
        $matchedUdid = $configUdid
        Write-Host "$PASS UDID '$configUdid' is connected and matches config"
    } else {
        if ($configUdid) {
            Write-Host "$WARN UDID '$configUdid' NOT found in connected devices"
        } else {
            Write-Host "$WARN No UDID set in config.properties"
        }

        # Auto-select first real device
        $matchedUdid = $connectedDevices | Where-Object { $_ -notmatch "^emulator" } | Select-Object -First 1
        if (-not $matchedUdid) { $matchedUdid = $connectedDevices[0] }

        Write-Host "  $YELLOW→ Auto-selecting device: $matchedUdid$RESET"
        Set-ConfigProperty "android.udid" $matchedUdid
        $allOk = $false  # warn user that config was changed
    }

    # --- DeviceName from ADB ---
    if ($matchedUdid) {
        $adbModel = (adb -s $matchedUdid shell getprop ro.product.model 2>&1).Trim()
        if (-not $adbModel) {
            $adbModel = (adb -s $matchedUdid shell getprop ro.product.name 2>&1).Trim()
        }
        $adbVersion = (adb -s $matchedUdid shell getprop ro.build.version.release 2>&1).Trim()

        Write-Host ""
        Write-Host "  Device Info from ADB:"
        Write-Host "    Model   : $CYAN$adbModel$RESET"
        Write-Host "    Android : $CYAN$adbVersion$RESET"
        Write-Host "    UDID    : $CYAN$matchedUdid$RESET"

        if ($adbModel -and $adbModel -ne $configDeviceName) {
            Write-Host ""
            Write-Host "$WARN DeviceName mismatch — config='$configDeviceName', device='$adbModel'"
            Set-ConfigProperty "android.deviceName" $adbModel
        } else {
            Write-Host ""
            Write-Host "$PASS DeviceName '$configDeviceName' matches connected device"
        }

        if ($adbVersion) {
            $configVersion = Read-ConfigProperty "android.platformVersion"
            if ($adbVersion -ne $configVersion) {
                Write-Host "$WARN PlatformVersion mismatch — config='$configVersion', device='$adbVersion'"
                Set-ConfigProperty "android.platformVersion" $adbVersion
            } else {
                Write-Host "$PASS PlatformVersion '$configVersion' matches connected device"
            }
        }

        # Ensure deviceType = real
        if ($configDeviceType -ne "real") {
            Set-ConfigProperty "android.deviceType" "real"
        }
    }
} else {
    Write-Host "$WARN Skipping UDID/DeviceName check — no devices available"
}

# =============================================================================
# Summary
# =============================================================================
Write-Header "PRE-TEST CHECK SUMMARY"

Write-Host "  Appium Running : $(if ($appiumRunning) { "$PASS YES" } else { "$FAIL NO" })$RESET"
Write-Host "  ADB Available  : $(if ($adbAvailable) { "$PASS YES" } else { "$FAIL NO" })$RESET"
Write-Host "  Devices Found  : $(if ($connectedDevices.Count -gt 0) { "$PASS $($connectedDevices.Count)" } else { "$FAIL 0" })$RESET"
Write-Host ""

# Re-show final config values
$finalUdid    = Read-ConfigProperty "android.udid"
$finalName    = Read-ConfigProperty "android.deviceName"
$finalVersion = Read-ConfigProperty "android.platformVersion"
Write-Host "$CYAN  Final config.properties values:$RESET"
Write-Host "    android.udid            = $YELLOW$finalUdid$RESET"
Write-Host "    android.deviceName      = $YELLOW$finalName$RESET"
Write-Host "    android.platformVersion = $YELLOW$finalVersion$RESET"
Write-Host ""

if (-not $appiumRunning -or $connectedDevices.Count -eq 0) {
    Write-Host "$RED$BOLD  PRE-CHECK FAILED — Fix above issues before running tests$RESET"
    if (-not $RunTests) { exit 1 }
} else {
    Write-Host "$GREEN$BOLD  ALL CHECKS PASSED — Ready to run tests!$RESET"
}

# =============================================================================
# Optional: Run Tests
# =============================================================================
if ($RunTests) {
    Write-Header "RUNNING TESTS"
    if ($Tags -ne "") {
        Write-Host "$CYAN Running: mvn clean test -Dtest=TestRunner -Dcucumber.filter.tags=$Tags$RESET"
        mvn clean test "-Dtest=TestRunner" "-Dcucumber.filter.tags=$Tags"
    } else {
        Write-Host "$CYAN Running: mvn clean test -Dtest=TestRunner$RESET"
        mvn clean test "-Dtest=TestRunner"
    }
}
