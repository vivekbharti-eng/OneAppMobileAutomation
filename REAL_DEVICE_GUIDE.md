# Real Device Testing Guide

## Overview

This guide covers running automated tests on **real Android and iOS devices** instead of emulators/simulators. Real device testing provides more accurate results and helps identify device-specific issues.

## Benefits of Real Device Testing

✅ **Accurate Performance** - Real hardware behavior  
✅ **Sensor Testing** - Camera, GPS, accelerometer  
✅ **Network Conditions** - Actual carrier networks  
✅ **Hardware-Specific Issues** - Memory, CPU, heating  
✅ **OS Variations** - Manufacturer-specific customizations  
✅ **Battery Consumption** - Real battery drain testing  

---

## Android Real Device Setup

### Prerequisites

1. **Android Device**
   - Android 5.0+ (API Level 21+)
   - USB cable for connection
   - Charged battery (>20%)

2. **Enable Developer Options**
   - Go to: Settings → About Phone → Tap "Build Number" 7 times
   - Developer Options will appear in Settings

3. **Enable USB Debugging**
   - Go to: Settings → Developer Options
   - Enable "USB Debugging"
   - Enable "Install via USB" (if available)
   - Enable "USB Debugging (Security Settings)" (if available)

4. **Trust Computer**
   - Connect device via USB
   - On device, tap "Allow" when prompted to trust computer
   - Check "Always allow from this computer"

### Get Device UDID

```bash
# Connect device and run
adb devices

# Output example:
# List of devices attached
# R58M6049HMF    device
```

The device ID (e.g., `R58M6049HMF`) is your Android UDID.

### Configuration

**Update `config.properties`:**

```properties
# ==========================================
# ANDROID REAL DEVICE CONFIGURATION
# ==========================================
android.deviceType=real
android.udid=R58M6049HMF

# Device information (optional - will be detected)
android.deviceName=Samsung Galaxy S21
android.platformVersion=13.0

# App configuration
android.automationName=UiAutomator2
android.app=C:/path/to/your/app.apk
android.appPackage=com.yourapp.package
android.appActivity=com.yourapp.MainActivity

# Real device performance settings
android.skipDeviceInitialization=false
android.skipServerInstallation=false
android.systemPort=8200

# App behavior
android.noReset=false
android.fullReset=false
```

### Real Device Specific Settings

| Setting | Description | Default |
|---------|-------------|---------|
| `android.deviceType` | Set to `real` for real devices | `emulator` |
| `android.udid` | Device unique identifier from adb devices | - |
| `skipDeviceInitialization` | Skip device initialization (faster) | `false` |
| `skipServerInstallation` | Skip Appium server installation | `false` |
| `systemPort` | Appium system port (use different for parallel) | `8200` |

### Run Tests

```bash
# Run tests on real Android device
mvn clean test -Plocal-android

# With specific tags
mvn clean test -Plocal-android -Dcucumber.filter.tags="@smoke"

# Using script
./run_android.sh local @smoke
```

### Verify Device Connection

```bash
# Check device is connected
adb devices

# Get device information
mvn exec:java -Dexec.mainClass="com.automation.utils.AdbInfoDemo"

# View device logs
adb logcat | grep -i appium
```

---

## iOS Real Device Setup

### Prerequisites

1. **Mac Computer** (Required for iOS real device testing)
2. **Xcode** (Latest stable version)
3. **iOS Device** (iOS 12+)
4. **Apple Developer Account** (Free or paid)
5. **USB Cable** (Lightning/USB-C)

### Step 1: Trust Computer

1. Connect iOS device to Mac via USB
2. On iOS device, tap "Trust This Computer"
3. Enter device passcode

### Step 2: Get Device UDID

**Method 1: Using Terminal**
```bash
xcrun xctrace list devices

# Output example:
# iPhone (14.5) (00008020-001234567890123A)
```

**Method 2: Using Xcode**
1. Open Xcode
2. Go to: Window → Devices and Simulators
3. Select your device
4. Copy the "Identifier"

**Method 3: Using System Information**
```bash
system_profiler SPUSBDataType | grep "Serial Number"
```

### Step 3: Get Team ID (Organization ID)

**Method 1: Xcode**
1. Open Xcode → Preferences → Accounts
2. Select your Apple ID
3. Click on team name
4. Copy "Team ID"

**Method 2: Apple Developer Portal**
1. Go to https://developer.apple.com/account
2. Login with Apple ID
3. Go to Membership
4. Copy "Team ID"

### Step 4: WebDriverAgent (WDA) Setup

WDA needs to be built and signed for real devices:

```bash
# Navigate to WDA directory
cd /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/Library/Xcode/Agents/

# Build WDA (Appium will handle this automatically, but you can test manually)
xcodebuild -project WebDriverAgent.xcodeproj \
           -scheme WebDriverAgentRunner \
           -destination 'id=YOUR_UDID' \
           test
```

### Configuration

**Update `config.properties`:**

```properties
# ==========================================
# iOS REAL DEVICE CONFIGURATION
# ==========================================
ios.deviceType=real
ios.udid=00008020-001234567890123A

# Device information (optional)
ios.deviceName=iPhone 13
ios.platformVersion=16.0

# App configuration
ios.automationName=XCUITest
ios.app=C:/path/to/your/app.ipa
ios.bundleId=com.yourapp.bundleid

# REQUIRED: Real device signing (from Apple Developer Account)
ios.xcodeOrgId=Z4XQ6A9B8C
ios.xcodeSigningId=iPhone Developer
ios.updatedWDABundleId=com.yourcompany.WebDriverAgentRunner

# WDA settings
ios.useNewWDA=false
ios.wdaLocalPort=8100

# App behavior
ios.noReset=false
ios.fullReset=false
```

### Real Device Specific Settings

| Setting | Description | Required |
|---------|-------------|----------|
| `ios.deviceType` | Set to `real` | Yes |
| `ios.udid` | Device UDID from Xcode/Terminal | Yes |
| `xcodeOrgId` | Team ID from Apple Developer Account | Yes |
| `xcodeSigningId` | Usually "iPhone Developer" | Yes |
| `updatedWDABundleId` | Custom WDA bundle ID | Yes |
| `useNewWDA` | Use new WDA session each time | No |
| `wdaLocalPort` | WDA port (different for parallel) | No |

### Run Tests

```bash
# Run tests on real iOS device
mvn clean test -Plocal-ios

# With specific tags
mvn clean test -Plocal-ios -Dcucumber.filter.tags="@smoke"

# Using script
./run_ios.sh local @smoke
```

### Verify Device Connection

```bash
# List devices
xcrun xctrace list devices

# Check device info
ideviceinfo -u YOUR_UDID

# View device logs
idevicesyslog -u YOUR_UDID
```

---

## Parallel Execution on Multiple Real Devices

### Android - Multiple Devices

**Configuration for Device 1:**
```properties
android.udid=R58M6049HMF
android.systemPort=8200
```

**Configuration for Device 2:**
```properties
android.udid=R58M6050ABC
android.systemPort=8201
```

**Start Appium servers:**
```bash
# Terminal 1 - Device 1
appium -p 4723 --session-override

# Terminal 2 - Device 2
appium -p 4724 --session-override
```

**TestNG XML for parallel:**
```xml
<suite name="Multi-Device Suite" parallel="tests" thread-count="2">
    <test name="Device-1">
        <parameter name="udid" value="R58M6049HMF"/>
        <parameter name="systemPort" value="8200"/>
        <parameter name="appiumPort" value="4723"/>
        <classes>
            <class name="com.automation.runners.TestRunner"/>
        </classes>
    </test>
    
    <test name="Device-2">
        <parameter name="udid" value="R58M6050ABC"/>
        <parameter name="systemPort" value="8201"/>
        <parameter name="appiumPort" value="4724"/>
        <classes>
            <class name="com.automation.runners.TestRunner"/>
        </classes>
    </test>
</suite>
```

### iOS - Multiple Devices

Similar approach with different WDA ports:
```properties
# Device 1
ios.udid=00008020-001234567890123A
ios.wdaLocalPort=8100

# Device 2
ios.udid=00008020-001234567890123B
ios.wdaLocalPort=8101
```

---

## Troubleshooting

### Android Issues

**Issue: Device not detected**
```bash
# Check USB debugging is enabled
adb devices

# Restart ADB
adb kill-server
adb start-server

# Check drivers (Windows)
# Install Google USB Driver from Android SDK Manager
```

**Issue: Unauthorized device**
```bash
# Revoke USB debugging authorizations on device
# Settings → Developer Options → Revoke USB debugging authorizations
# Reconnect device and authorize again
```

**Issue: App installation failed**
```bash
# Enable install via USB
# Settings → Developer Options → Install via USB (Enable)

# Manually install to verify
adb install path/to/app.apk
```

**Issue: Permission denied**
```bash
# Grant storage permissions manually
adb shell pm grant com.your.package android.permission.WRITE_EXTERNAL_STORAGE
adb shell pm grant com.your.package android.permission.READ_EXTERNAL_STORAGE
```

### iOS Issues

**Issue: WebDriverAgent fails to build**
```bash
# Open WDA in Xcode
open /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/Library/Xcode/Agents/WebDriverAgent.xcodeproj

# In Xcode:
# 1. Select WebDriverAgentRunner target
# 2. Go to Signing & Capabilities
# 3. Select your Team
# 4. Change Bundle Identifier
# 5. Build for Testing
```

**Issue: Signing errors**
- Verify Apple Developer Account is active
- Check Team ID is correct
- Ensure provisioning profiles are valid
- Try changing `updatedWDABundleId` to unique value

**Issue: Device not trusted**
```bash
# On iOS device
# Settings → General → Device Management
# Trust your developer certificate
```

**Issue: WDA port conflict**
```bash
# Check what's using port 8100
lsof -i :8100

# Kill process
kill -9 <PID>

# Or use different port in config
ios.wdaLocalPort=8101
```

---

## Best Practices

### For All Real Devices

1. **Keep Device Charged** - >20% battery recommended
2. **Disable Auto-Lock** - Prevent screen from sleeping
3. **Stable USB Connection** - Use good quality cables
4. **Close Background Apps** - Free up memory
5. **Disable Notifications** - Avoid test interruptions
6. **Keep Screen On** - Developer options → Stay awake

### Android Specific

1. **Disable animations** for faster tests:
   ```bash
   adb shell settings put global window_animation_scale 0
   adb shell settings put global transition_animation_scale 0
   adb shell settings put global animator_duration_scale 0
   ```

2. **Check device temperature** - Avoid overheating

3. **Clear app data** between test runs

### iOS Specific

1. **Keep device unlocked** during test execution

2. **Disable iCloud sync** for test accounts

3. **Use dedicated test device** - Don't use personal device

4. **Reset Location & Privacy** before testing:
   - Settings → General → Reset → Reset Location & Privacy

---

## Comparison: Emulator vs Real Device

| Aspect | Emulator/Simulator | Real Device |
|--------|-------------------|-------------|
| Cost | Free | Device cost |
| Setup | Quick | More complex |
| Performance | Good | Excellent |
| Sensors | Limited | Full support |
| Network | Simulated | Real network |
| Hardware | Generic | Actual hardware |
| CI/CD | Easy to automate | Requires device farm |
| Parallel | Easy (unlimited) | Limited by devices |
| Maintenance | Low | Medium |

---

## When to Use Real Devices

✅ **Final validation** before release  
✅ **Hardware feature testing** (camera, GPS, sensors)  
✅ **Performance testing** under real conditions  
✅ **Network testing** with carrier networks  
✅ **OS-specific issues** (manufacturer customizations)  
✅ **Battery consumption** testing  
✅ **Production-like environment**  

## When to Use Emulators

✅ **Development and debugging**  
✅ **Fast feedback** during development  
✅ **CI/CD pipelines** (automated)  
✅ **Multiple OS versions** testing  
✅ **Parallel execution** at scale  
✅ **Cost-effective** testing  

---

## Hybrid Approach (Recommended)

**Local Development:**
- Emulators for quick feedback
- Real device for final verification

**CI/CD Pipeline:**
- Emulators for PR validation
- BrowserStack real devices for nightly runs

**Release Testing:**
- Real devices only
- Multiple brands/models
- Different OS versions

---

## Cloud Real Devices (BrowserStack)

For teams without physical devices, use BrowserStack:

```properties
execution.type=browserstack
platform=android  # or ios

# BrowserStack automatically uses real devices
```

Benefits:
- Access to 1000+ real devices
- No physical device management
- Parallel execution
- Integrated with CI/CD

See main README for BrowserStack setup.

---

## Checklist

### Before Starting Tests

**Android:**
- [ ] USB debugging enabled
- [ ] Device connected and authorized
- [ ] UDID configured in config.properties
- [ ] App APK available
- [ ] Battery >20%
- [ ] Screen timeout disabled

**iOS:**
- [ ] Device trusted on Mac
- [ ] UDID configured
- [ ] Team ID configured
- [ ] WDA bundle ID configured
- [ ] Device unlocked
- [ ] App IPA signed correctly

### During Testing

- [ ] Monitor device temperature
- [ ] Watch for unexpected popups
- [ ] Check device logs for errors
- [ ] Verify app permissions

### After Testing

- [ ] Review test logs
- [ ] Check screenshots
- [ ] Analyze reports
- [ ] Document device-specific issues

---

## Additional Resources

**Android:**
- [Android Developer - USB Debugging](https://developer.android.com/studio/debug/dev-options)
- [ADB Documentation](https://developer.android.com/tools/adb)

**iOS:**
- [Apple - Device Management](https://support.apple.com/guide/deployment/intro-to-mdm-depc0aadd3fe/web)
- [WebDriverAgent](https://github.com/appium/WebDriverAgent)

**Appium:**
- [Real Device Testing](https://appium.io/docs/en/writing-running-appium/running-tests/)
- [Desired Capabilities](https://appium.io/docs/en/writing-running-appium/caps/)

---

**For framework-specific questions, see:**
- [README.md](README.md) - Main framework documentation
- [QUICKSTART.md](QUICKSTART.md) - Quick setup guide
- [ADB_HELPER_GUIDE.md](ADB_HELPER_GUIDE.md) - ADB utilities

**Happy Testing on Real Devices! 📱**
