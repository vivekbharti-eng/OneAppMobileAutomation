# Device Manager Implementation Guide

## Overview

The **DeviceManager** is a centralized utility that automatically detects and manages both real devices and emulators/simulators for Android and iOS platforms. It eliminates hardcoded device configurations and enables seamless switching between different device types.

## Key Features

✅ **Automatic Device Detection** - Auto-detects connected Android real devices using ADB  
✅ **Smart Device Selection** - Intelligently picks the right device based on configuration  
✅ **Real vs Virtual Management** - Seamlessly handles emulators, simulators, and real devices  
✅ **BrowserStack Support** - Works with cloud-based device execution  
✅ **Validation** - Pre-execution device availability checks  
✅ **Configuration Driven** - No code changes needed to switch devices

---

## Architecture

```
DeviceManager
├── getDevice() → Auto-detects and returns device details
├── getAndroidDevice() → Handles Android emulators and real devices
├── getIOSDevice() → Handles iOS simulators and real devices
├── getBrowserStackDevice() → Returns BrowserStack device info
├── printDeviceInfo() → Displays selected device details
└── validateDeviceAvailability() → Pre-execution validation
```

---

## How It Works

### Android Device Detection Flow

1. **Reads Configuration**
   - Checks `execution.type` (local vs browserstack)
   - Checks `android.deviceType` (emulator vs real)
   - Checks `android.udid` (specific device UDID if configured)

2. **ADB Detection** (for real devices)
   - Runs `adb devices` to list connected devices
   - Filters real devices from emulators
   - Validates device availability

3. **Device Selection**
   - Uses configured UDID if available and connected
   - Otherwise selects first detected real device
   - Falls back to configured device name for emulators

4. **Device Info Extraction**
   - Retrieves manufacturer, model, Android version
   - Extracts screen resolution, battery level
   - Determines device type (Real Device vs Emulator)

### iOS Device Detection Flow

1. **Reads Configuration**
   - Checks `ios.deviceType` (simulator vs real)
   - Checks `ios.udid` (required for real devices)

2. **Real Device Validation**
   - Ensures UDID is configured
   - Verifies signing configuration (xcodeOrgId, WDA settings)

3. **Device Selection**
   - Uses configured device details
   - Sets appropriate capabilities for real device signing

---

## Configuration

### config.properties

#### Execution Type
```properties
# Options: local, browserstack
execution.type=local
```

#### Android Configuration

**For Emulator:**
```properties
platform=android
android.deviceType=emulator
android.deviceName=Pixel 7
android.platformVersion=14.0
```

**For Real Device (Auto-Detection):**
```properties
platform=android
android.deviceType=real
android.deviceName=Samsung Galaxy S24
android.platformVersion=14.0
# Optional: Specify UDID (leave blank for auto-detection)
android.udid=
```

**For Real Device (Specific UDID):**
```properties
platform=android
android.deviceType=real
android.udid=RF8W1234ABC
android.systemPort=8200
```

#### iOS Configuration

**For Simulator:**
```properties
platform=ios
ios.deviceType=simulator
ios.deviceName=iPhone 15
ios.platformVersion=17.0
```

**For Real Device:**
```properties
platform=ios
ios.deviceType=real
ios.deviceName=iPhone 15 Pro
ios.platformVersion=17.0
ios.udid=00008110-001234567890001E
ios.xcodeOrgId=ABCD123456
ios.xcodeSigningId=iPhone Developer
ios.updatedWDABundleId=com.yourcompany.WebDriverAgentRunner
```

---

## Usage in Code

### DriverFactory Integration

The DeviceManager is integrated into DriverFactory:

```java
// In DriverFactory.createLocalAndroidDriver()
Map<String, String> device = DeviceManager.getDevice();
DeviceManager.printDeviceInfo();

// Use detected device details
String deviceName = device.get("deviceName");
String platformVersion = device.get("platformVersion");
String udid = device.get("udid");

options.setDeviceName(deviceName);
options.setPlatformVersion(platformVersion);
if (udid != null && !udid.isEmpty()) {
    options.setUdid(udid);
}
```

### Pre-Execution Validation

```java
// Before starting tests
boolean isDeviceAvailable = DeviceManager.validateDeviceAvailability();
if (!isDeviceAvailable) {
    throw new RuntimeException("Device validation failed");
}
```

### Getting Selected Device Info

```java
// After device selection
Map<String, String> selectedDevice = DeviceManager.getSelectedDevice();
String deviceType = selectedDevice.get("deviceType"); // "real" or "emulator"
String udid = selectedDevice.get("udid");
String manufacturer = selectedDevice.get("manufacturer");
```

---

## Execution Methods

### 1. Maven Profiles

#### Android Emulator
```bash
mvn clean test -Plocal-android-emulator
```

#### Android Real Device (Auto-Detection)
```bash
mvn clean test -Plocal-android-real
```

#### iOS Simulator
```bash
mvn clean test -Plocal-ios-simulator
```

#### iOS Real Device
```bash
mvn clean test -Plocal-ios-real
```

#### BrowserStack
```bash
mvn clean test -Pbs-android
mvn clean test -Pbs-ios
```

### 2. Shell Scripts

#### Windows (Batch Scripts)

**Android Real Device:**
```cmd
run_android_real.bat
```

**Android Emulator:**
```cmd
run_android_emulator.bat
```

**BrowserStack:**
```cmd
run_browserstack.bat android
run_browserstack.bat ios
```

#### Linux/Mac (Shell Scripts)

**Android Real Device:**
```bash
./run_android_real.sh
```

**Android Emulator:**
```bash
./run_android_emulator.sh
```

**BrowserStack:**
```bash
./run_browserstack.sh android
./run_browserstack.sh ios
```

### 3. IDE Execution

Update `testng.xml` or pass system properties:
```xml
<parameter name="platform" value="android"/>
<parameter name="execution.type" value="local"/>
```

---

## Real Device Setup

### Android Real Device

1. **Enable Developer Options**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times

2. **Enable USB Debugging**
   - Settings → Developer Options → Enable USB Debugging

3. **Connect Device via USB**
   ```bash
   adb devices
   ```
   Output:
   ```
   RF8W1234ABC    device
   ```

4. **Update Configuration**
   ```properties
   android.deviceType=real
   android.udid=RF8W1234ABC  # Or leave blank for auto-detection
   ```

5. **Run Tests**
   ```bash
   mvn clean test -Plocal-android-real
   ```

### iOS Real Device

1. **Get Device UDID**
   ```bash
   # Via Command Line
   xcrun xctrace list devices
   
   # Or via Xcode
   # Window → Devices and Simulators → Select Device → Identifier
   ```

2. **Configure App Signing**
   - Open Xcode → Preferences → Accounts
   - Add Apple ID → Note Team ID
   - Set `ios.xcodeOrgId=TEAM_ID`

3. **Update Configuration**
   ```properties
   ios.deviceType=real
   ios.udid=00008110-001234567890001E
   ios.xcodeOrgId=ABCD123456
   ios.updatedWDABundleId=com.yourcompany.WebDriverAgentRunner
   ```

4. **Run Tests**
   ```bash
   mvn clean test -Plocal-ios-real
   ```

---

## Device Info in Reports

DeviceManager automatically captures and logs device information:

### Console Output
```
==================================================
SELECTED DEVICE INFORMATION
==================================================
Device Type     : Real Device
Device Name     : SM-S918B
Platform Version: 14
UDID            : RF8W1234ABC
Manufacturer    : samsung
Model           : SM-S918B
==================================================
```

### Extent Report
System info includes:
- Device Type (Real Device / Emulator)
- Device ID (UDID)
- Manufacturer
- Model
- Android Version
- Screen Resolution
- Battery Level

---

## Troubleshooting

### Issue: No devices detected

**Android:**
```bash
# Check ADB connection
adb devices

# Restart ADB server
adb kill-server
adb start-server

# Check device authorization
adb devices
# If showing "unauthorized", check device screen for authorization prompt
```

**iOS:**
```bash
# Check connected devices
xcrun xctrace list devices

# Verify Xcode installation
xcode-select --print-path

# Trust device
# iOS Settings → General → VPN & Device Management → Trust Computer
```

### Issue: Device detected but test fails to start

**Android:**
- Ensure app is compatible with device OS version
- Check if app requires specific permissions
- Verify `appPackage` and `appActivity` are correct

**iOS:**
- Verify signing configuration (xcodeOrgId)
- Check WDA bundle ID configuration
- Ensure device is unlocked during test execution
- Trust the developer certificate on device

### Issue: Multiple devices connected, wrong device selected

**Solution 1: Specify UDID**
```properties
android.udid=SPECIFIC_DEVICE_UDID
```

**Solution 2: Disconnect other devices**
```bash
adb disconnect
# Keep only target device connected
```

---

## Advanced Scenarios

### Running on Multiple Devices in Parallel

**testng.xml:**
```xml
<suite name="Parallel Device Suite" parallel="tests" thread-count="2">
    <test name="Device1">
        <parameter name="android.udid" value="DEVICE1_UDID"/>
        <classes>
            <class name="com.automation.runners.TestRunner"/>
        </classes>
    </test>
    <test name="Device2">
        <parameter name="android.udid" value="DEVICE2_UDID"/>
        <classes>
            <class name="com.automation.runners.TestRunner"/>
        </classes>
    </test>
</suite>
```

### Programmatic Device Selection

```java
// Override device selection in test
System.setProperty("android.udid", "SPECIFIC_DEVICE");
Map<String, String> device = DeviceManager.getDevice();
```

### CI/CD Environment Variables

```bash
# Jenkins / GitHub Actions
export PLATFORM=android
export EXECUTION_TYPE=local
export ANDROID_DEVICETYPE=real
export ANDROID_UDID=RF8W1234ABC

mvn clean test
```

---

## Best Practices

1. ✅ **Use Auto-Detection for Single Device** - Leave UDID blank when only one device is connected
2. ✅ **Specify UDID for Multiple Devices** - Configure explicit UDID when multiple devices are connected
3. ✅ **Validate Before Execution** - Use `DeviceManager.validateDeviceAvailability()`
4. ✅ **Check Device Info** - Review console logs to confirm correct device selection
5. ✅ **Use Maven Profiles** - Leverage profiles for quick execution mode switching
6. ✅ **Keep Devices Unlocked** - Ensure devices are unlocked during test execution
7. ✅ **Configure Real Device Settings** - Set systemPort (Android) and signing (iOS) properly

---

## Summary

| Feature | Emulator/Simulator | Real Device | BrowserStack |
|---------|-------------------|-------------|--------------|
| Auto-Detection | ✅ | ✅ | ➖ |
| UDID Required | ➖ | Optional* | ➖ |
| ADB Integration | ✅ | ✅ | ➖ |
| Device Info Capture | ✅ | ✅ | Limited |
| Parallel Execution | ✅ | ✅ (with UDID) | ✅ |
| Configuration Switch | ✅ One property | ✅ One property | ✅ One property |

*Auto-detected if only one device connected

---

## Need Help?

See also:
- [QUICKSTART.md](QUICKSTART.md) - Quick setup guide
- [REAL_DEVICE_GUIDE.md](REAL_DEVICE_GUIDE.md) - Real device configuration
- [ADB_HELPER_GUIDE.md](ADB_HELPER_GUIDE.md) - ADB command reference
- [README.md](README.md) - Complete framework documentation
