# Appium BDD Cucumber Automation Framework

A comprehensive Appium automation framework using BDD Cucumber that supports **both Android and iOS** platforms in a single codebase with **Local and BrowserStack** execution capabilities.

## Tech Stack
- **Java 11**
- **Maven** - Build and dependency management
- **Appium** - Mobile automation
- **Cucumber BDD** - Behavior-driven development
- **TestNG** - Test execution and parallel testing
- **Extent Reports & Allure** - Rich HTML reporting
- **BrowserStack** - Cloud-based mobile testing
- **Log4j2** - Logging

## Framework Features

✅ Cross-platform support (Android & iOS)  
✅ Local & BrowserStack cloud execution  
✅ **Real device & Emulator/Simulator support**  
✅ **Automatic device detection via ADB**  
✅ **DeviceManager for smart device selection**  
✅ **Jenkins CI/CD integration with comprehensive pipeline**  
✅ CI/CD ready (Jenkins & GitHub Actions)  
✅ Page Object Model (POM) design pattern  
✅ Platform-specific locator management  
✅ ThreadLocal driver for parallel execution  
✅ Cucumber BDD with Gherkin syntax  
✅ Extent Reports & Allure with screenshots  
✅ Screenshot on test failure  
✅ Configurable via properties files  
✅ Maven profiles for easy switching  
✅ Environment variable support for credentials  
✅ ADB device information integration  
✅ Automatic device type detection and logging  
✅ Pre-execution device validation  
✅ Clean and maintainable code structure  

## 🚀 Quick Start - 5 Ways to Run Tests

### 1️⃣ IDE Right-Click (Development & Debugging) 🆕
```
1. Open: src/test/java/com/automation/runners/TestRunner.java
2. Right-click on TestRunner class
3. Select "Run TestRunner" / "Run Java" / "Run As TestNG Test"
```
✅ **Perfect for**: Development, debugging with breakpoints, quick feedback  
📖 **Documentation**: [IDE_EXECUTION_GUIDE.md](IDE_EXECUTION_GUIDE.md)

### 2️⃣ Easiest Way (Windows - Double Click)
```
1. Double-click: RUN_SMOKE_TESTS.bat
   OR
2. Double-click: run_tests.bat (Interactive Menu)
```

### 3️⃣ PowerShell Scripts (Recommended for Windows)
```powershell
# Run smoke tests (most common)
.\run_ecocash.ps1

# Quick test launcher
.\quick_test.ps1 smoke
.\quick_test.ps1 login

# Device management
.\manage_devices.ps1 list
.\manage_devices.ps1 clear   # Logout/clear app data

# Appium control
.\start_appium.ps1 start
.\start_appium.ps1 status
```

### 4️⃣ Traditional (Batch Files)
```batch
# Windows batch files
run_android_real.bat
run_android_emulator.bat
```

### 5️⃣ Maven CLI (Cross-Platform)
```bash
# Direct Maven
mvn clean test -Plocal-android-real "-Dcucumber.filter.tags=@smoke"
```

### 📖 Detailed Documentation

#### 🎯 Essential Reading
- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - One-page framework overview (START HERE!)
- **[FRAMEWORK_DESIGN.md](FRAMEWORK_DESIGN.md)** - Complete framework design & validation
- **[FRAMEWORK_REQUIREMENTS_VALIDATION.md](FRAMEWORK_REQUIREMENTS_VALIDATION.md)** - Requirements compliance report
- **[QUICKSTART.md](QUICKSTART.md)** - Quick start guide

#### 🔧 Technical Guides
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Technical architecture deep-dive
- **[IDE_EXECUTION_GUIDE.md](IDE_EXECUTION_GUIDE.md)** - 🆕 Right-click execution in IDE (VS Code/IntelliJ/Eclipse)
- **[DEVICE_MANAGER_GUIDE.md](DEVICE_MANAGER_GUIDE.md)** - Device management & ADB integration
- **[SHELL_INTEGRATION.md](SHELL_INTEGRATION.md)** - PowerShell & shell script usage
- **[JENKINSSETUP_GUIDE.md](JENKINS_SETUP_GUIDE.md)** - 🆕 Jenkins CI/CD integration setup
- **[REAL_DEVICE_GUIDE.md](REAL_DEVICE_GUIDE.md)** - Real device setup & troubleshooting
- **[ADB_HELPER_GUIDE.md](ADB_HELPER_GUIDE.md)** - ADB utilities reference

## Project Structure

```
OneAppAutomation/
├── src/
│   ├── test/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── automation/
│   │   │           ├── drivers/
│   │   │           │   ├── DriverFactory.java
│   │   │           │   └── DriverManager.java
│   │   │           ├── hooks/
│   │   │           │   └── Hooks.java
│   │   │           ├── pages/
│   │   │           │   ├── BasePage.java
│   │   │           │   ├── LoginPage.java
│   │   │           │   └── HomePage.java
│   │   │           ├── reports/
│   │   │           │   └── ExtentReportManager.java
│   │   │           ├── runners/
│   │   │           │   └── TestRunner.java
│   │   │           ├── stepdefinitions/
│   │   │           │   └── LoginSteps.java
│   │   │           └── utils/
│   │   │               ├── AdbHelper.java
│   │   │               ├── AdbInfoDemo.java
│   │   │               ├── BrowserStackCapabilityManager.java
│   │   │               ├── DeviceManager.java
│   │   │               ├── LocatorUtils.java
│   │   │               ├── PropertyReader.java
│   │   │               ├── ScreenshotUtils.java
│   │   │               └── WaitHelper.java
│   │   └── resources/
│   │       ├── features/
│   │       │   └── Login.feature
│   │       ├── locators/
│   │       │   ├── android_locators.properties
│   │       │   └── ios_locators.properties
│   │       ├── config.properties
│   │       └── log4j2.xml
├── target/
│   ├── cucumber-reports/
│   ├── logs/
│   ├── reports/
│   └── screenshots/
├── run_android_real.bat
├── run_android_real.sh
├── run_android_emulator.bat
├── run_android_emulator.sh
├── run_browserstack.bat
├── run_browserstack.sh
├── run_android.bat
├── run_android.sh
├── run_ios.sh
├── pom.xml
├── testng.xml
├── Jenkinsfile
├── README.md
├── QUICKSTART.md
├── DEVICE_MANAGER_GUIDE.md
├── REAL_DEVICE_GUIDE.md
└── ADB_HELPER_GUIDE.md
```

## Prerequisites

1. **Java JDK 11 or higher**
   ```bash
   java -version
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **Node.js and Appium**
   ```bash
   npm install -g appium
   appium --version
   ```

4. **Appium Drivers**
   ```bash
   # For Android
   appium driver install uiautomator2
   
   # For iOS
   appium driver install xcuitest
   ```

5. **Android Studio** (for Android testing)
   - Android SDK
   - Android Emulator or Real Device

6. **Xcode** (for iOS testing - Mac only)
   - iOS Simulator or Real Device

## Configuration

### 1. Update `config.properties`
Navigate to `src/test/resources/config.properties` and update:

```properties
# Select execution type: local or browserstack
execution.type=local

# Select platform: android or ios
platform=android

# Appium server URL (for local execution)
appium.server.url=http://127.0.0.1:4723

# Update Android configuration
android.deviceName=Your_Device_Name
android.platformVersion=14.0
android.app=path/to/your/android/app.apk
android.appPackage=com.your.app.package
android.appActivity=com.your.app.MainActivity

# Update iOS configuration (if testing iOS)
ios.deviceName=iPhone 15
ios.platformVersion=17.0
ios.app=path/to/your/ios/app.app
ios.bundleId=com.your.app.bundleId

# Update test credentials
username=your_username
password=your_password
```

### 2. BrowserStack Configuration

**Upload your app to BrowserStack:**
```bash
curl -u "username:access_key" \
-X POST "https://api-cloud.browserstack.com/app-automate/upload" \
-F "file=@/path/to/app.apk"
```

**Update config.properties with BrowserStack details:**
```properties
# BrowserStack credentials
browserstack.username=your_browserstack_username
browserstack.accessKey=your_browserstack_access_key

# BrowserStack app IDs (from upload response)
browserstack.android.app=bs://your_android_app_id
browserstack.ios.app=bs://your_ios_app_id

# BrowserStack device configuration
browserstack.android.device=Google Pixel 7
browserstack.android.osVersion=13.0
browserstack.ios.device=iPhone 14
browserstack.ios.osVersion=16
```

**Or use environment variables (recommended for CI/CD):**
```bash
export BROWSERSTACK_USERNAME=your_username
export BROWSERSTACK_ACCESS_KEY=your_access_key
```

### 3. Update Locators
Update platform-specific locators in:
- `src/test/resources/locators/android_locators.properties`
- `src/test/resources/locators/ios_locators.properties`

### 4. Real Device Configuration (Optional)

**For Android Real Device:**
```properties
android.deviceType=real
android.udid=YOUR_DEVICE_UDID  # Get from: adb devices
```

**For iOS Real Device (Mac only):**
```properties
ios.deviceType=real
ios.udid=YOUR_DEVICE_UDID  # Get from: xcrun xctrace list devices
ios.xcodeOrgId=YOUR_TEAM_ID  # From Apple Developer Account
ios.updatedWDABundleId=com.yourcompany.WebDriverAgentRunner
```

**See [REAL_DEVICE_GUIDE.md](REAL_DEVICE_GUIDE.md) for complete setup instructions.**

## Running Tests

### Quick Start with Shell Scripts

**Windows:**
```bash
# Android Real Device (Auto-detects connected device)
run_android_real.bat

# Android Emulator
run_android_emulator.bat

# BrowserStack
run_browserstack.bat android
run_browserstack.bat ios
```

**Linux/Mac:**
```bash
# Android Real Device (Auto-detects connected device)
./run_android_real.sh

# Android Emulator
./run_android_emulator.sh

# BrowserStack
./run_browserstack.sh android
./run_browserstack.sh ios
```

### Method 1: Using Maven Profiles

**Local Android Emulator:**
```bash
mvn clean test -Plocal-android-emulator
```

**Local Android Real Device:**
```bash
mvn clean test -Plocal-android-real
```

**Local iOS Simulator:**
```bash
mvn clean test -Plocal-ios-simulator
```

**Local iOS Real Device:**
```bash
mvn clean test -Plocal-ios-real
```

**BrowserStack Android:**
```bash
mvn clean test -Pbs-android
```

**BrowserStack iOS:**
```bash
mvn clean test -Pbs-ios
```

**Run specific tags:**
```bash
mvn clean test -Plocal-android-real -Dcucumber.filter.tags="@smoke"
```

### Method 2: Using VS Code Tasks

Use Command Palette (Ctrl+Shift+P) → "Tasks: Run Task"
- Run All Tests (Android)
- Run Smoke Tests (Android)
- Run All Tests (iOS)
- Run Smoke Tests (iOS)

### Method 3: Using TestNG XML

```bash
mvn clean test -DsuiteXmlFile=testng.xml
```

### Method 4: From IDE (VS Code)

1. Install **Test Runner for Java** extension
2. Open `TestRunner.java`
3. Right-click and select "Run Test"

---

## DeviceManager - Automatic Device Detection

The framework includes a **DeviceManager** utility that automatically detects and manages devices:

### Key Features

✅ **Auto-Detection** - Automatically detects connected Android real devices  
✅ **Smart Selection** - Intelligently selects the right device based on configuration  
✅ **Device Info Capture** - Extracts manufacturer, model, Android version, UDID  
✅ **Pre-Validation** - Validates device availability before test execution  
✅ **Logging** - Comprehensive device information in console and reports

### How It Works

1. **Configuration Check** - Reads `deviceType` from config.properties
2. **ADB Detection** - Runs `adb devices` to list connected devices
3. **Device Filtering** - Filters real devices from emulators
4. **Info Extraction** - Captures device details using ADB commands
5. **Capability Setup** - Automatically configures Appium capabilities

### Usage Example

**Connect Android device and run:**
```bash
# DeviceManager automatically detects connected device
mvn clean test -Plocal-android-real
```

**Console Output:**
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

**For detailed DeviceManager documentation → [DEVICE_MANAGER_GUIDE.md](DEVICE_MANAGER_GUIDE.md)**
## Device Type Switching

### Emulator/Simulator (Default)
```properties
android.deviceType=emulator
ios.deviceType=simulator
```

### Real Device
```properties
android.deviceType=real
android.udid=R58M6049HMF  # From: adb devices

ios.deviceType=real
ios.udid=00008020-001234567890123A  # From: xcrun xctrace list devices
ios.xcodeOrgId=Z4XQ6A9B8C  # From Apple Developer Account
```

**Complete real device setup → [REAL_DEVICE_GUIDE.md](REAL_DEVICE_GUIDE.md)**
## Switching Between Android and iOS

### Option 1: Update config.properties
Change the platform property:
```properties
platform=ios
```

### Option 2: Use Maven Profile
```bash
mvn clean test -Pios
```

### Option 3: System Property
```bash
mvn clean test -Dplatform=ios
```

## Reports

### Extent Report
- Location: `target/reports/ExtentReport_<timestamp>.html`
- Open in browser for rich HTML report with screenshots

### Cucumber Reports
- HTML: `target/cucumber-reports/cucumber.html`
- JSON: `target/cucumber-reports/cucumber.json`
- XML: `target/cucumber-reports/cucumber.xml`

### Logs
- Location: `target/logs/automation.log`

### Screenshots
- Location: `target/screenshots/`
- Captured automatically on test failure

## Parallel Execution

The framework supports parallel execution using TestNG:

1. Update `testng.xml` to increase thread count:
```xml
<suite name="Appium Automation Suite" parallel="tests" thread-count="4">
```

2. Or modify `TestRunner.java`:
```java
@DataProvider(parallel = true)
public Object[][] scenarios() {
    return super.scenarios();
}
```

## Writing New Tests

### 1. Create Feature File
Create a new `.feature` file in `src/test/resources/features/`:

```gherkin
@smoke
Feature: User Profile
  Scenario: View user profile
    Given I login with valid credentials
    When I navigate to profile page
    Then I should see user details
```

### 2. Create Step Definitions
Create step definition class in `src/test/java/com/automation/stepdefinitions/`:

```java
public class ProfileSteps {
    @When("I navigate to profile page")
    public void iNavigateToProfilePage() {
        // Implementation
    }
}
```

### 3. Create Page Object
Create page class in `src/test/java/com/automation/pages/`:

```java
public class ProfilePage extends BasePage {
    // Page methods
}
```

### 4. Add Locators
Add locators in:
- `android_locators.properties`
- `ios_locators.properties`

## CI/CD Integration

### GitHub Actions

The framework includes a GitHub Actions workflow for automated testing.

**Setup:**
1. Add secrets to your GitHub repository:
   - `BROWSERSTACK_USERNAME`
   - `BROWSERSTACK_ACCESS_KEY`
   - `SLACK_WEBHOOK` (optional)

2. The workflow automatically runs on:
   - Push to main/develop branches
   - Pull requests
   - Manual trigger with custom parameters

**Manual Trigger:**
Go to Actions → Appium Mobile Automation Tests → Run workflow

**Workflow file:** `.github/workflows/test.yml`

### Jenkins

**Setup:**
1. Create credentials in Jenkins:
   - ID: `browserstack-username`
   - ID: `browserstack-accesskey`

2. Create a new Pipeline job pointing to `Jenkinsfile`

3. Configure parameters:
   - Platform (android/ios)
   - Execution Type (local/browserstack)
   - Test Tags

**Run Pipeline:**
Select parameter values and click "Build"

### Available Maven Profiles for CI/CD

```bash
# Local execution profiles
mvn clean test -Plocal-android
mvn clean test -Plocal-ios

# BrowserStack execution profiles
mvn clean test -Pbs-android
mvn clean test -Pbs-ios

# Combined with tags
mvn clean test -Pbs-android -Dcucumber.filter.tags="@smoke"
```

### Environment Variables Support

The framework supports environment variables for sensitive data:

```bash
# BrowserStack credentials
export BROWSERSTACK_USERNAME=your_username
export BROWSERSTACK_ACCESS_KEY=your_access_key

# Run tests
mvn clean test -Pbs-android
```

## Best Practices

1. **Use LocatorUtils** for platform-independent locators
2. **Extend BasePage** for all page objects
3. **Use WaitHelper** instead of Thread.sleep()
4. **Add logs** using Logger for debugging
5. **Use ExtentReportManager** for reporting
6. **Follow naming conventions** for locator keys
7. **Keep feature files simple** and readable
8. **One assertion per Then step** in scenarios
9. **Use tags** to organize tests (@smoke, @regression)
10. **Handle exceptions** gracefully with proper logging

## ADB Device Information

The framework includes ADB integration to automatically capture and display device information for Android local execution.

### Automatic Device Info Capture

When running tests locally on Android, the framework automatically:
- Detects connected devices
- Retrieves device details (manufacturer, model, Android version)
- Displays device info in console logs
- Adds device info to Extent Reports

### Manual ADB Usage

**Check connected devices:**
```java
List<String> devices = AdbHelper.getConnectedDevices();
```

**Get device information:**
```java
Map<String, String> deviceInfo = AdbHelper.getDeviceInfo(null); // null = first device
```

**Check if app is installed:**
```java
boolean installed = AdbHelper.isAppInstalled(deviceId, "com.example.app");
```

**Install/Uninstall app:**
```java
AdbHelper.installApp(deviceId, "/path/to/app.apk");
AdbHelper.uninstallApp(deviceId, "com.example.app");
```

**Clear app data:**
```java
AdbHelper.clearAppData(deviceId, "com.example.app");
```

**Get device properties:**
```java
String resolution = AdbHelper.getScreenResolution(deviceId);
String battery = AdbHelper.getBatteryLevel(deviceId);
```

**Run standalone tool:**
```bash
mvn exec:java -Dexec.mainClass="com.automation.utils.AdbInfoDemo"
```

### Available Device Information

The framework captures:
- Device ID / Serial Number
- Manufacturer & Brand
- Model Name
- Android Version & SDK Level
- Build ID
- Screen Resolution
- Battery Level
- Installed Apps Count

**For detailed ADB Helper documentation, see [ADB_HELPER_GUIDE.md](ADB_HELPER_GUIDE.md)**

## Documentation

- **[README.md](README.md)** - Main framework documentation (you are here)
- **[QUICKSTART.md](QUICKSTART.md)** - Quick setup and run guide
- **[DEVICE_MANAGER_GUIDE.md](DEVICE_MANAGER_GUIDE.md)** - ⭐ Automatic device detection and management
- **[REAL_DEVICE_GUIDE.md](REAL_DEVICE_GUIDE.md)** - Complete real device testing setup
- **[ADB_HELPER_GUIDE.md](ADB_HELPER_GUIDE.md)** - ADB utility documentation
- **[Jenkinsfile](Jenkinsfile)** - Jenkins CI/CD pipeline
- **[.github/workflows/test.yml](.github/workflows/test.yml)** - GitHub Actions workflow

## Troubleshooting

### Issue: Appium server not running
```bash
# Start Appium server
appium
```

### Issue: Element not found
- Verify locators in property files
- Check if element is visible/present
- Increase wait timeout in config.properties

### Issue: Driver initialization failed
- Verify Appium server is running
- Check device/emulator is connected
- Verify app path in config.properties

### Issue: Tests not running in parallel
- Check TestNG configuration
- Verify ThreadLocal driver usage
- Review thread-count in testng.xml

## Contact & Support

For issues and questions:
- Check logs in `target/logs/automation.log`
- Review Extent Report for detailed test results
- Enable debug logging in `log4j2.xml`

## License

This framework is created for automation testing purposes.

---

**Happy Testing! 🚀**
