# Quick Start Guide

## Setup Instructions

### 1. Install Prerequisites

**Java:**
- Download and install JDK 11 or higher from Oracle or AdoptOpenJDK
- Set JAVA_HOME environment variable

**Maven:**
- Download from https://maven.apache.org/download.cgi
- Add to PATH environment variable

**Node.js and Appium:**
```bash
# Install Node.js from https://nodejs.org/

# Install Appium globally
npm install -g appium

# Install Appium drivers
appium driver install uiautomator2
appium driver install xcuitest
```

### 2. Connect Device/Emulator

**For Android Emulator:**
```bash
# Check connected devices
adb devices

# If no devices, start an emulator from Android Studio
```

**For Android Real Device:**
```bash
# Enable USB debugging on device
# Connect via USB

# Check device is detected
adb devices

# Get device UDID (the alphanumeric string)
# Example output: R58M6049HMF    device

# View device information
mvn exec:java -Dexec.mainClass="com.automation.utils.AdbInfoDemo"
```

**Update config.properties for real device:**
```properties
android.deviceType=real
android.udid=R58M6049HMF
```

**For iOS (Mac only):**
```bash
# For Simulator
xcrun simctl list devices
xcrun simctl boot "iPhone 15"

# For Real Device
# Connect device via USB
# Get UDID
xcrun xctrace list devices

# Trust device in Xcode
# Xcode → Window → Devices and Simulators
```

**Update config.properties for iOS real device:**
```properties
ios.deviceType=real
ios.udid=00008020-001234567890123A
ios.xcodeOrgId=YOUR_TEAM_ID
ios.updatedWDABundleId=com.yourcompany.WebDriverAgentRunner
```

**See [REAL_DEVICE_GUIDE.md](../REAL_DEVICE_GUIDE.md) for detailed real device setup.**

### 3. Start Appium Server

```bash
# Start Appium in a separate terminal
appium

# Or with log level
appium --log-level info
```

### 4. Configure Your App

1. Update `src/test/resources/config.properties`:
   - Set platform (android/ios)
   - Set device name
   - Set app path
   - Set app package/bundle ID
   - Update credentials

2. Update locators in:
   - `src/test/resources/locators/android_locators.properties`
   - `src/test/resources/locators/ios_locators.properties`

### 5. Run Tests

#### LOCAL EXECUTION

**Option 1: Command Line**
```bash
# Navigate to project directory
cd OneAppAutomation

# Run tests on local Android
mvn clean test -Plocal-android

# Run tests on local iOS
mvn clean test -Plocal-ios
```

**Option 2: VS Code**
- Install "Test Runner for Java" extension
- Open TestRunner.java
- Click "Run Test" button

**Option 3: With Specific Tags**
```bash
# Smoke tests on Android
mvn clean test -Plocal-android -Dcucumber.filter.tags="@smoke"

# Regression tests on iOS
mvn clean test -Plocal-ios -Dcucumber.filter.tags="@regression"
```

#### BROWSERSTACK EXECUTION

**Step 1: Upload App to BrowserStack**
```bash
# For Android
curl -u "USERNAME:ACCESS_KEY" \
-X POST "https://api-cloud.browserstack.com/app-automate/upload" \
-F "file=@/path/to/app.apk"

# For iOS
curl -u "USERNAME:ACCESS_KEY" \
-X POST "https://api-cloud.browserstack.com/app-automate/upload" \
-F "file=@/path/to/app.ipa"
```

**Step 2: Update config.properties**
```properties
execution.type=browserstack
browserstack.username=your_username
browserstack.accessKey=your_access_key
browserstack.android.app=bs://app_id_from_upload
browserstack.ios.app=bs://app_id_from_upload
```

**Step 3: Run Tests**
```bash
# Android on BrowserStack
mvn clean test -Pbs-android

# iOS on BrowserStack
mvn clean test -Pbs-ios

# With specific tags
mvn clean test -Pbs-android -Dcucumber.filter.tags="@smoke"
```

**Using Environment Variables (Recommended for CI/CD):**
```bash
# Set credentials
export BROWSERSTACK_USERNAME=your_username
export BROWSERSTACK_ACCESS_KEY=your_access_key

# Run tests
mvn clean test -Pbs-android
```

### 6. View Reports

After test execution:
- **Extent Report**: `target/reports/ExtentReport_<timestamp>.html`
- **Cucumber HTML**: `target/cucumber-reports/cucumber.html`
- **Logs**: `target/logs/automation.log`
- **Screenshots**: `target/screenshots/`

## Common Commands

```bash
# Clean and compile
mvn clean compile

# Run specific test
mvn test -Dcucumber.filter.tags="@smoke"

# Run with custom property
mvn test -Dplatform=ios

# Skip tests
mvn clean install -DskipTests

# Run with debug
mvn test -X
```

## Troubleshooting

### Appium won't start
```bash
# Kill existing Appium process
pkill -f appium

# Or on Windows
taskkill /F /IM node.exe

# Restart Appium
appium
```

### Device not detected (Android)
```bash
# Check ADB
adb devices

# Restart ADB
adb kill-server
adb start-server
```

### Maven build fails
```bash
# Update dependencies
mvn clean install -U

# Clear local repository
mvn dependency:purge-local-repository
```

### Tests fail with timeout
- Increase timeouts in `config.properties`
- Check if app is installed correctly
- Verify locators are correct

## Next Steps

1. Customize locators for your app
2. Add more feature files
3. Create additional page objects
4. Configure CI/CD pipeline
5. Add more test scenarios

## Need Help?

- Check README.md for detailed documentation
- Review logs in target/logs/
- Check Extent Report for detailed test results
