# ADB Helper Guide

## Overview

The AdbHelper utility class provides comprehensive ADB (Android Debug Bridge) integration for the automation framework. It automatically captures device information during test execution and provides methods for device management.

## Features

✅ Automatic device detection  
✅ Device information retrieval  
✅ App installation/uninstallation  
✅ App data management  
✅ Battery and screen info  
✅ Package verification  
✅ Seamless integration with reports  

## Automatic Integration

### During Test Execution

When running tests locally on Android, the framework automatically:

1. **Detects Connected Devices**
   - Verifies ADB availability
   - Lists all connected devices

2. **Captures Device Information**
   - Manufacturer, Brand, Model
   - Android Version & SDK Level
   - Build ID & Serial Number
   - Screen Resolution
   - Battery Level

3. **Logs Device Details**
   - Console output with formatted device info
   - Extent Report system information section

### Example Console Output

```
==================================================
DEVICE INFORMATION
==================================================
Device ID       : emulator-5554
Manufacturer    : Google
Brand           : google
Model           : Pixel 7
Device Name     : oriole
Android Version : 14.0
SDK Version     : 34
Build ID        : UD1A.230803.041
Serial Number   : emulator-5554
Screen Resolution: 1080x2400
Battery Level   : 95%
==================================================
```

## Manual Usage

### Check ADB Availability

```java
if (AdbHelper.isAdbAvailable()) {
    System.out.println("ADB is ready!");
}
```

### Get Connected Devices

```java
List<String> devices = AdbHelper.getConnectedDevices();
for (String deviceId : devices) {
    System.out.println("Device: " + deviceId);
}
```

### Get Device Information

```java
// Get info for first connected device
Map<String, String> deviceInfo = AdbHelper.getDeviceInfo(null);

// Get info for specific device
Map<String, String> deviceInfo = AdbHelper.getDeviceInfo("emulator-5554");

// Access individual properties
String manufacturer = deviceInfo.get("manufacturer");
String model = deviceInfo.get("model");
String androidVersion = deviceInfo.get("androidVersion");
```

### App Management

```java
String deviceId = "emulator-5554";
String packageName = "com.example.app";
String apkPath = "/path/to/app.apk";

// Check if app is installed
boolean installed = AdbHelper.isAppInstalled(deviceId, packageName);

// Install app
if (!installed) {
    AdbHelper.installApp(deviceId, apkPath);
}

// Clear app data
AdbHelper.clearAppData(deviceId, packageName);

// Uninstall app
AdbHelper.uninstallApp(deviceId, packageName);
```

### Device Properties

```java
String deviceId = "emulator-5554";

// Get screen resolution
String resolution = AdbHelper.getScreenResolution(deviceId);
System.out.println("Resolution: " + resolution); // Output: 1080x2400

// Get battery level
String battery = AdbHelper.getBatteryLevel(deviceId);
System.out.println("Battery: " + battery); // Output: 95%

// Get custom property
String manufacturer = AdbHelper.getDeviceProperty(deviceId, "ro.product.manufacturer");
```

### Package Management

```java
String deviceId = "emulator-5554";

// Get all installed packages
List<String> packages = AdbHelper.getInstalledPackages(deviceId);
System.out.println("Total packages: " + packages.size());

// Search for specific package
boolean hasChromeInstalled = packages.contains("com.android.chrome");
```

### Print Device Summary

```java
// Print formatted device information
AdbHelper.printDeviceInfo(null); // null = first device
AdbHelper.printDeviceInfo("emulator-5554"); // specific device
```

## Standalone Device Info Tool

Run the standalone tool to view device information:

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.automation.utils.AdbInfoDemo"

# Or compile and run
mvn clean compile
java -cp target/test-classes:target/dependency/* com.automation.utils.AdbInfoDemo
```

## Integration in Page Objects

You can use ADB Helper in page objects or step definitions:

```java
public class LoginPage extends BasePage {
    
    public void verifyAppInstalled(String packageName) {
        String deviceId = AdbHelper.getConnectedDevices().get(0);
        
        if (!AdbHelper.isAppInstalled(deviceId, packageName)) {
            logger.error("App not installed: " + packageName);
            throw new RuntimeException("App not installed");
        }
    }
    
    public void clearAppDataBeforeTest(String packageName) {
        String deviceId = AdbHelper.getConnectedDevices().get(0);
        AdbHelper.clearAppData(deviceId, packageName);
        logger.info("App data cleared for: " + packageName);
    }
}
```

## Extent Report Integration

Device information is automatically added to Extent Reports:

- Device ID
- Manufacturer
- Model
- Android Version
- SDK Version
- Screen Resolution
- Battery Level

View this information in the "System Info" section of the Extent Report.

## Best Practices

1. **Null Device ID**
   - Pass `null` to use the first connected device
   - Useful when only one device is connected

2. **Error Handling**
   - ADB commands may fail if device is disconnected
   - Always check `isAdbAvailable()` before operations

3. **Performance**
   - Device info is captured once before test execution
   - Avoid excessive ADB calls during test execution

4. **Multi-Device Testing**
   - Use specific device IDs when multiple devices are connected
   - Loop through `getConnectedDevices()` for parallel execution

## Troubleshooting

### ADB Not Found

```bash
# Add Android SDK platform-tools to PATH
export PATH=$PATH:~/Android/Sdk/platform-tools

# On Windows
set PATH=%PATH%;C:\Users\YourUser\AppData\Local\Android\Sdk\platform-tools
```

### No Devices Listed

```bash
# Start ADB server
adb start-server

# Restart ADB
adb kill-server
adb start-server

# Check USB debugging is enabled on device
```

### Device Unauthorized

1. Disconnect and reconnect USB cable
2. Revoke USB debugging authorizations on device
3. Reconnect and authorize computer

### Empty Device Information

- Ensure device has proper ADB access
- Some emulators may have limited property access
- Try using a real device or different emulator image

## Examples

### Pre-Test Setup

```java
@Before
public void setupDevice() {
    String deviceId = AdbHelper.getConnectedDevices().get(0);
    String packageName = "com.example.app";
    
    // Clear app data before each test
    if (AdbHelper.isAppInstalled(deviceId, packageName)) {
        AdbHelper.clearAppData(deviceId, packageName);
    }
}
```

### Device Validation

```java
@Before
public void validateDevice() {
    Map<String, String> deviceInfo = AdbHelper.getDeviceInfo(null);
    String androidVersion = deviceInfo.get("androidVersion");
    
    int sdkVersion = Integer.parseInt(deviceInfo.get("sdkVersion"));
    if (sdkVersion < 28) {
        throw new RuntimeException("Android SDK 28+ required, found: " + sdkVersion);
    }
}
```

### Battery Check

```java
public void checkBatteryLevel() {
    String battery = AdbHelper.getBatteryLevel(null);
    int level = Integer.parseInt(battery.replace("%", ""));
    
    if (level < 20) {
        logger.warn("Low battery: " + level + "%. Test may be affected.");
    }
}
```

## API Reference

| Method | Description | Returns |
|--------|-------------|---------|
| `isAdbAvailable()` | Check if ADB is available | boolean |
| `getConnectedDevices()` | Get list of connected device IDs | List<String> |
| `getDeviceInfo(deviceId)` | Get device information map | Map<String, String> |
| `getDeviceProperty(deviceId, property)` | Get specific device property | String |
| `getInstalledPackages(deviceId)` | Get all installed packages | List<String> |
| `isAppInstalled(deviceId, packageName)` | Check if app is installed | boolean |
| `installApp(deviceId, apkPath)` | Install APK | boolean |
| `uninstallApp(deviceId, packageName)` | Uninstall app | boolean |
| `clearAppData(deviceId, packageName)` | Clear app data | boolean |
| `getScreenResolution(deviceId)` | Get screen resolution | String |
| `getBatteryLevel(deviceId)` | Get battery level | String |
| `printDeviceInfo(deviceId)` | Print formatted device info | void |
| `startAdbServer()` | Start ADB server | void |
| `killAdbServer()` | Kill ADB server | void |

## Related Files

- **AdbHelper.java** - Main utility class
- **AdbInfoDemo.java** - Standalone demo tool
- **Hooks.java** - Integration in test lifecycle
- **ExtentReportManager.java** - Report integration

---

**Note:** ADB Helper is currently only applicable for Android local execution. iOS and BrowserStack executions do not use ADB.
