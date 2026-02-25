# ✅ IDE Right-Click Execution - Complete Guide

## Overview

Your EcoCash Appium framework now supports **5 execution methods**, including **IDE right-click execution** of TestRunner.java.

---

## 🎯 All Supported Execution Methods

| Method | Command | Status |
|--------|---------|--------|
| **1. IDE Right-Click** | Right-click TestRunner.java → Run | ✅ **FIXED** |
| **2. Maven CLI** | `mvn clean test -Plocal-android-real` | ✅ Working |
| **3. PowerShell** | `.\quick_test.ps1 smoke` | ✅ Working |
| **4. Batch File** | Double-click `RUN_SMOKE_TESTS.bat` | ✅ Working |
| **5. CI/CD** | Jenkins/GitHub Actions | ✅ Working |

---

## 🔧 Recent Fix: IDE Right-Click Execution

### Problem (Before Fix)
```
Exception: No test found to run
java.lang.RuntimeException: No test found to run
```

**Root Cause**: TestRunner.java had incomplete glue path configuration
- Only scanned `com.automation.stepdefinitions`
- Missed `com.automation.hooks` package
- IDE test runner couldn't find Cucumber hooks

### Solution (After Fix)

**Updated TestRunner.java**:
```java
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.automation",  // ✅ Scans ALL subpackages
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "junit:target/cucumber-reports/cucumber.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        dryRun = false,
        tags = "@smoke"
)
public class TestRunner extends AbstractTestNGCucumberTests {
    
    @Override
    @DataProvider(parallel = false)  // ✅ Stable for IDE execution
    public Object[][] scenarios() {
        return super.scenarios();
    }
    
    @BeforeSuite
    public void beforeSuite() {
        ExtentReportManager.initReport();
    }
    
    @AfterSuite
    public void afterSuite() {
        ExtentReportManager.flushReport();
    }
}
```

**Key Changes**:
1. ✅ `glue = "com.automation"` - Scans all subpackages including hooks
2. ✅ `parallel = false` - More stable for IDE execution
3. ✅ `tags = "@smoke"` - Matches common execution scenario
4. ✅ `@BeforeSuite` and `@AfterSuite` - Initialize/cleanup reports

---

## 🖱️ How to Execute in IDE

### VS Code (With Java Extensions)

#### Option 1: Run Test Button
1. Open: [TestRunner.java](c:\Users\vivek.bharti\OneAppAutomation\src\test\java\com\automation\runners\TestRunner.java)
2. Look for green "▶ Run Test" button above class name
3. Click "Run Test"

#### Option 2: Right-Click Menu
1. Right-click on `TestRunner.java` in Explorer
2. Select "Run Java"

#### Option 3: Command Palette
1. Press `Ctrl+Shift+P`
2. Type "Java: Run"
3. Select TestRunner

#### Option 4: VS Code Task
1. Press `Ctrl+Shift+P`
2. Type "Tasks: Run Task"
3. Select "Run Smoke Tests (Android)"

### IntelliJ IDEA

#### Option 1: Right-Click
1. Right-click on `TestRunner.java`
2. Select "Run 'TestRunner'"

#### Option 2: Run Configuration
1. Right-click on `TestRunner.java`
2. Select "Modify Run Configuration"
3. Add VM options if needed: `-Dplatform=android -Dexecution.type=local`
4. Click "Run"

#### Option 3: Gutter Icon
1. Open `TestRunner.java`
2. Click green ▶ icon next to class name
3. Select "Run 'TestRunner'"

### Eclipse

#### Option 1: Right-Click
1. Right-click on `TestRunner.java`
2. Select "Run As" → "TestNG Test"

#### Option 2: Run Menu
1. Open `TestRunner.java`
2. Menu: Run → Run As → TestNG Test

#### Option 3: Run Configuration
1. Run → Run Configurations
2. Create new TestNG configuration
3. Set Test Class: `com.automation.runners.TestRunner`
4. Click "Run"

---

## ⚙️ IDE Configuration (Optional Enhancements)

### VS Code - settings.json
```json
{
    "java.test.config": {
        "name": "EcoCash Tests",
        "vmArgs": [
            "-Dplatform=android",
            "-Dexecution.type=local"
        ],
        "workingDirectory": "${workspaceFolder}"
    }
}
```

### IntelliJ - Run Configuration Template
```
Name: TestRunner
Main class: com.automation.runners.TestRunner
VM options: -Dplatform=android -Dexecution.type=local
Working directory: $MODULE_DIR$
Use classpath of module: OneAppAutomation
```

### Eclipse - Run Configuration
```
Project: OneAppAutomation
Test class: com.automation.runners.TestRunner
VM arguments: -Dplatform=android -Dexecution.type=local
```

---

## 🎯 Configuration Priority (How Properties Are Read)

The framework reads configuration in this priority order:

```
1. System Properties (highest priority)
   ↓ -Dplatform=android
   
2. Maven Profiles
   ↓ -Plocal-android-real
   
3. Environment Variables
   ↓ $env:PLATFORM="android"
   
4. config.properties (lowest priority - default values)
   ↓ platform=android
```

**For IDE Execution**:
- Uses defaults from `config.properties`
- Can override via IDE Run Configuration (VM options)
- Perfect for development and debugging

---

## 📋 Pre-Requisites for IDE Execution

### VS Code
✅ **Install Extensions**:
1. Extension Pack for Java (Microsoft)
2. Test Runner for Java (Microsoft)
3. Debugger for Java (Microsoft)
4. Maven for Java (Microsoft)

**Verify Installation**:
```powershell
# Check Java
java -version

# Check Maven
mvn -version

# Check Appium
appium -v
```

### IntelliJ IDEA
✅ **Built-in Support**:
- TestNG plugin (bundled)
- Maven integration (bundled)
- Cucumber plugin (install from marketplace)

### Eclipse
✅ **Install Plugins**:
1. TestNG for Eclipse
2. Maven Integration (m2e)
3. Cucumber Eclipse Plugin

---

## 🚀 Quick Test - Verify IDE Execution

### Step 1: Ensure Prerequisites
```powershell
# Device connected?
.\manage_devices.ps1 list

# Appium running?
.\start_appium.ps1 status
```

### Step 2: Right-Click TestRunner.java
1. Navigate to: `src/test/java/com/automation/runners/TestRunner.java`
2. Right-click on file or class name
3. Select "Run TestRunner" (or equivalent)

### Step 3: Observe Execution
**Expected Output**:
```
[TestNG] Running: Command line suite

Scenario: Successful login with valid credentials
  Given the app is launched
  And I logout if already logged in
  When I enter country code "+263"
  And I enter mobile number "771222221"
  ...
  
===============================================
Command line suite
Total tests run: 1, Passes: 1, Failures: 0, Skips: 0
===============================================
```

### Step 4: Check Reports
- **Console Output**: Test execution logs
- **HTML Report**: `target/cucumber-reports/cucumber.html`
- **Extent Report**: `target/reports/ExtentReport_*.html`

---

## 🐛 Troubleshooting IDE Execution

### Issue 1: "No tests found to run"
**Cause**: TestNG not recognizing test class  
**Solution**: Ensure TestRunner extends `AbstractTestNGCucumberTests`

### Issue 2: "Cannot find feature files"
**Cause**: Incorrect working directory  
**Solution**: Set working directory to project root in Run Configuration

### Issue 3: "Cannot connect to Appium"
**Cause**: Appium server not running  
**Solution**:
```powershell
.\start_appium.ps1 start
```

### Issue 4: "Device not found"
**Cause**: No device connected  
**Solution**:
```powershell
.\manage_devices.ps1 list
adb devices
```

### Issue 5: "ClassNotFoundException"
**Cause**: Maven dependencies not resolved  
**Solution**:
```bash
mvn clean install
```
Then refresh Maven in IDE

### Issue 6: "Configuration not found"
**Cause**: config.properties not in classpath  
**Solution**: Ensure `src/test/resources/config.properties` exists

---

## 🎯 IDE vs Other Execution Methods

| Feature | IDE Right-Click | Maven CLI | PowerShell | CI/CD |
|---------|----------------|-----------|------------|-------|
| **Speed** | ⚡ Fastest | Medium | Medium | Slow |
| **Debugging** | ✅ Full breakpoints | ❌ No | ❌ No | ❌ No |
| **Reports** | ✅ Console + HTML | ✅ Full | ✅ Full | ✅ Full |
| **Configuration** | 🔧 Run Config | 🔧 Maven | 🔧 Script | 🔧 Pipeline |
| **Best For** | Development | Local Testing | Quick Tests | Automation |
| **Setup Time** | 5 seconds | 10 seconds | 5 seconds | 1 minute |

---

## 📊 Configuration Examples for IDE

### Development (Fast Feedback)
```properties
# config.properties for IDE dev work
execution.type=local
platform=android
android.deviceType=real
# Default tags
tags=@smoke
# Parallel off for debugging
parallel.execution=false
```

### VM Options in IDE Run Config
```bash
# Quick smoke test
-Dcucumber.filter.tags=@smoke

# Specific scenario
-Dcucumber.filter.tags="@login and @positive"

# Override platform
-Dplatform=ios -Dexecution.type=simulator
```

---

## 🔄 Workflow: Development with IDE

### Typical Dev Cycle
```
1. Write/modify test scenario in .feature file
   ↓
2. Implement/update step definitions
   ↓
3. Right-click TestRunner.java → Debug
   ↓
4. Set breakpoints in step definitions
   ↓
5. Debug execution, inspect variables
   ↓
6. Fix issues
   ↓
7. Right-click TestRunner.java → Run
   ↓
8. Verify test passes
   ↓
9. Commit code
```

### Benefits of IDE Execution
✅ **Fast Feedback** - No Maven overhead  
✅ **Debugging** - Breakpoints, step-through, variable inspection  
✅ **Convenience** - Right-click and go  
✅ **Incremental** - Test single scenarios quickly  
✅ **Development** - Perfect for TDD/BDD development  

---

## 🎓 Best Practices for IDE Execution

### 1. Use Default Safe Configuration
```properties
# config.properties - IDE-friendly defaults
execution.type=local
android.deviceType=real
parallel.execution=false
implicit.wait=10
explicit.wait=20
```

### 2. Keep Appium Running
```powershell
# Start once, use many times
.\start_appium.ps1 start

# Check status anytime
.\start_appium.ps1 status
```

### 3. Clear App Data Between Runs
```powershell
# Fresh state for each test
.\manage_devices.ps1 clear
```

### 4. Use Tags for Focused Testing
```java
// TestRunner.java - Change tags as needed
tags = "@smoke"           // Quick smoke tests
tags = "@login"           // Only login tests
tags = "@wip"             // Work in progress
tags = "@login and @positive"  // Specific combination
```

### 5. Monitor Console Output
- Watch for Appium connection logs
- Check device detection messages
- Monitor test execution flow
- Capture any error messages

---

## 📝 IDE Execution Checklist

Before right-clicking TestRunner.java:

- [ ] Device connected (`.\manage_devices.ps1 list`)
- [ ] Appium server running (`.\start_appium.ps1 status`)
- [ ] App installed on device (or APK path correct)
- [ ] config.properties has correct settings
- [ ] Maven dependencies resolved (`mvn clean install`)
- [ ] Feature files exist in `src/test/resources/features/`
- [ ] Step definitions exist in `src/test/java/.../stepdefinitions/`
- [ ] Hooks exist in `src/test/java/.../hooks/`

---

## 🎉 Summary

### What Was Fixed
✅ **TestRunner.java glue path** - Now scans all packages  
✅ **Parallel execution** - Disabled for stable IDE runs  
✅ **Tags configuration** - Set to @smoke for quick tests  

### Now You Can
✅ Right-click TestRunner.java and run tests in any IDE  
✅ Debug tests with breakpoints  
✅ Get fast feedback during development  
✅ Use same codebase for IDE, CLI, PowerShell, and CI/CD  

### Zero Code Changes
✅ Same TestRunner.java works for:
- IDE right-click execution
- Maven command line
- PowerShell scripts
- CI/CD pipelines
- BrowserStack cloud

---

## 🚀 Try It Now!

### Quick 3-Step Test

**Step 1**: Ensure setup
```powershell
.\manage_devices.ps1 list
.\start_appium.ps1 status
```

**Step 2**: Right-click TestRunner.java
- Navigate to: `src/test/java/com/automation/runners/TestRunner.java`
- Right-click → Run TestRunner

**Step 3**: Watch tests execute!
- Monitor console output
- Check reports in `target/reports/`
- Celebrate! 🎉

---

## 📚 Related Documentation

- [FRAMEWORK_DESIGN.md](FRAMEWORK_DESIGN.md) - Complete framework documentation
- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - One-page overview
- [SHELL_INTEGRATION.md](SHELL_INTEGRATION.md) - PowerShell execution
- [DEVICE_MANAGER_GUIDE.md](DEVICE_MANAGER_GUIDE.md) - Device management

---

**Status**: ✅ **IDE Right-Click Execution WORKING**  
**Last Updated**: February 10, 2026  
**Tested On**: VS Code with Java extensions  
**Framework Version**: 1.0-SNAPSHOT
