# Framework Architecture & Flow

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         TEST EXECUTION                          │
│  (Maven / IDE / Shell Scripts / CI/CD)                         │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CONFIGURATION LAYER                           │
│  ┌──────────────────┐  ┌───────────────────┐                   │
│  │ config.properties│  │ Environment       │                   │
│  │ - Platform       │  │ Variables         │                   │
│  │ - Execution Type │  │ (CI/CD Secrets)   │                   │
│  │ - Device Type    │  │                   │                   │
│  └──────────────────┘  └───────────────────┘                   │
│                PropertyReader (Unified Access)                   │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DEVICE MANAGEMENT LAYER                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              DeviceManager (Auto Detection)              │  │
│  │  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │  │
│  │  │   Android   │  │     iOS      │  │  BrowserStack  │  │  │
│  │  │  - ADB Scan │  │  - UDID      │  │  - Cloud       │  │  │
│  │  │  - Filter   │  │  - Config    │  │    Devices     │  │  │
│  │  │  - Select   │  │  - Validate  │  │                │  │  │
│  │  └─────────────┘  └──────────────┘  └────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                          AdbHelper                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ - Device Detection  - Info Extraction  - App Management  │  │
│  └──────────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DRIVER FACTORY LAYER                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    DriverFactory                         │  │
│  │  ┌────────────┐  ┌────────────┐  ┌─────────────────┐   │  │
│  │  │   Local    │  │   Local    │  │   BrowserStack  │   │  │
│  │  │  Android   │  │    iOS     │  │   (Remote)      │   │  │
│  │  │  Driver    │  │   Driver   │  │   Driver        │   │  │
│  │  └────────────┘  └────────────┘  └─────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                    DriverManager (ThreadLocal)                   │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CUCUMBER BDD LAYER                           │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐      │
│  │   Hooks     │  │    Step      │  │   Test Runner    │      │
│  │ - Setup     │  │ Definitions  │  │   (TestNG)       │      │
│  │ - Teardown  │  │              │  │   - Parallel     │      │
│  │ - Device    │  │              │  │   - Allure       │      │
│  │   Info      │  │              │  │   - Tags         │      │
│  └─────────────┘  └──────────────┘  └──────────────────┘      │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PAGE OBJECT MODEL LAYER                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                      BasePage                            │  │
│  │  ┌────────────┐  ┌────────────┐  ┌──────────────────┐  │  │
│  │  │ LoginPage  │  │ HomePage   │  │  Other Pages     │  │  │
│  │  └────────────┘  └────────────┘  └──────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                      LocatorUtils                                │
│  ┌─────────────────────────┐  ┌────────────────────────────┐   │
│  │ android_locators.props  │  │ ios_locators.properties    │   │
│  └─────────────────────────┘  └────────────────────────────┘   │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                       REPORTING LAYER                            │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐      │
│  │   Extent    │  │    Allure    │  │     Log4j2       │      │
│  │   Reports   │  │   Reports    │  │     Logging      │      │
│  │ - Device    │  │ - History    │  │   - Console      │      │
│  │   Info      │  │ - Trends     │  │   - File         │      │
│  │ - Screen    │  │ - Screen     │  │                  │      │
│  │   shots     │  │   shots      │  │                  │      │
│  └─────────────┘  └──────────────┘  └──────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Execution Flow

### 1. Local Android Real Device Execution

```
START
  │
  ├─── Read config.properties
  │    ├─ platform=android
  │    ├─ execution.type=local
  │    └─ android.deviceType=real
  │
  ├─── DeviceManager.getDevice()
  │    ├─ Check ADB availability
  │    ├─ Run: adb devices
  │    ├─ Filter real devices
  │    ├─ Select device (auto or by UDID)
  │    ├─ Extract device info via AdbHelper
  │    │  ├─ Manufacturer
  │    │  ├─ Model
  │    │  ├─ Android Version
  │    │  ├─ Screen Resolution
  │    │  └─ Battery Level
  │    └─ Return device map
  │
  ├─── DriverFactory.createLocalAndroidDriver()
  │    ├─ Use device info from DeviceManager
  │    ├─ Set capabilities:
  │    │  ├─ deviceName
  │    │  ├─ platformVersion
  │    │  ├─ udid
  │    │  ├─ app
  │    │  ├─ appPackage
  │    │  ├─ appActivity
  │    │  └─ systemPort (real device)
  │    └─ Initialize AndroidDriver
  │
  ├─── Store in DriverManager (ThreadLocal)
  │
  ├─── Hooks.@Before
  │    ├─ Get device info from AdbHelper
  │    ├─ Log device details
  │    └─ Add to ExtentReport
  │
  ├─── Execute Cucumber Scenarios
  │    ├─ Step Definitions
  │    ├─ Page Objects
  │    └─ Locators (platform-specific)
  │
  ├─── Hooks.@After
  │    ├─ Screenshot on failure
  │    └─ Quit driver
  │
  └─── Generate Reports
       ├─ Extent Report (with device info)
       ├─ Allure Report
       └─ Logs
END
```

### 2. BrowserStack Execution

```
START
  │
  ├─── Read config.properties
  │    ├─ platform=android
  │    └─ execution.type=browserstack
  │
  ├─── DeviceManager.getDevice()
  │    └─ Return BrowserStack device config
  │
  ├─── BrowserStackCapabilityManager
  │    ├─ Validate credentials (env vars/config)
  │    ├─ Set capabilities:
  │    │  ├─ device
  │    │  ├─ osVersion
  │    │  ├─ app (bs://appid)
  │    │  ├─ project
  │    │  ├─ build
  │    │  └─ browserstack options
  │    └─ Get BrowserStack Hub URL
  │
  ├─── DriverFactory.createBrowserStackDriver()
  │    └─ Initialize RemoteWebDriver
  │
  ├─── Execute Tests (same flow as local)
  │
  └─── Reports generated
END
```

---

## 📊 Decision Flow

### Platform Selection
```
┌─────────────────────────────┐
│ Read platform from config   │
└──────────┬──────────────────┘
           │
           ├── android ──► Android locators
           │              Android driver
           │              ADB integration
           │
           └── ios ──────► iOS locators
                          iOS driver
                          Xcode integration (real device)
```

### Execution Type Selection
```
┌─────────────────────────────┐
│ Read execution.type         │
└──────────┬──────────────────┘
           │
           ├── local ──────► DeviceManager
           │                 Local Appium
           │                 Device detection
           │
           └── browserstack ► BrowserStack Manager
                              Remote URL
                              Cloud devices
```

### Device Type Selection (Local Android)
```
┌─────────────────────────────┐
│ Read android.deviceType     │
└──────────┬──────────────────┘
           │
           ├── emulator ──► Use config deviceName
           │                Auto-detect running emulator
           │
           └── real ──────► Run: adb devices
                           Filter real devices
                           Auto-select or use UDID
                           Configure systemPort
```

---

## 🛠️ Component Interactions

### DeviceManager ↔ AdbHelper
```
DeviceManager                AdbHelper
     │                          │
     ├──getConnectedDevices()──►│
     │◄─────[List<String>]──────┤
     │                          │
     ├──isRealDevice(id)───────►│
     │◄─────[boolean]───────────┤
     │                          │
     ├──getDeviceInfo(id)──────►│
     │◄─────[Map<K,V>]──────────┤
```

### DriverFactory ↔ DeviceManager
```
DriverFactory              DeviceManager
     │                          │
     ├──getDevice()────────────►│
     │                          ├─Read config
     │                          ├─Check execution type
     │                          ├─Detect/select device
     │◄─[device Map]───────────┤
     │                          │
     ├─Use device details       │
     └─Create driver            │
```

### Page Objects ↔ LocatorUtils
```
LoginPage                  LocatorUtils
    │                          │
    ├──getLocator("key")──────►│
    │                          ├─Read platform
    │                          ├─Load platform file
    │◄─[By locator]───────────┤
    │                          │
    ├─Perform action           │
```

---

## 🚦 State Management

### Driver Lifecycle
```
Test Start
    │
    ├─ DriverFactory.initializeDriver()
    │  └─► Driver created
    │
    ├─ DriverManager.setDriver(driver)
    │  └─► Stored in ThreadLocal
    │
    ├─ Test Execution
    │  └─► DriverManager.getDriver() (in pages/steps)
    │
    ├─ Test End
    │  └─► DriverManager.quitDriver()
    │     └─► Driver quit & removed from ThreadLocal
```

### Device Info Lifecycle
```
Hooks.@Before
    │
    ├─ DeviceManager.getDevice()
    │  └─► Device detected/configured
    │
    ├─ AdbHelper.getDeviceInfo()
    │  └─► Detailed info extracted
    │
    ├─ ExtentReportManager.addDeviceInfo()
    │  └─► Info added to report
    │
Test Scenarios
    │
    └─► Device info available in logs/reports
```

---

## 🔌 Integration Points

### CI/CD Integration Flow
```
Jenkins/GitHub Actions
    │
    ├─ Set environment variables
    │  ├─ PLATFORM
    │  ├─ EXECUTION_TYPE
    │  ├─ BROWSERSTACK_USERNAME
    │  └─ BROWSERSTACK_ACCESS_KEY
    │
    ├─ Execute Maven command
    │  mvn clean test -P<profile>
    │
    ├─ PropertyReader reads env vars
    │  └─► Overrides config.properties
    │
    ├─ Tests execute
    │
    └─ Publish reports
       ├─ Cucumber JSON
       ├─ Extent HTML
       └─ Allure artifacts
```

---

## 💡 Key Design Patterns

### 1. Factory Pattern
- **DriverFactory** - Creates appropriate driver based on platform and execution type
- **Capability builders** - Creates capabilities for different environments

### 2. Singleton Pattern
- **DriverManager** - Ensures single driver instance per thread
- **ExtentReportManager** - Single report instance

### 3. Page Object Model
- **BasePage** - Base class with common methods
- **Specific Pages** - Extend BasePage with page-specific actions

### 4. Strategy Pattern
- **DeviceManager** - Different strategies for device selection
- **LocatorUtils** - Different locator strategies per platform

### 5. Builder Pattern
- **BrowserStack capabilities** - Builds complex capability objects
- **Appium options** - Builds UiAutomator2Options, XCUITestOptions

---

## 📈 Scalability Features

### Parallel Execution
```
TestNG Thread Pool
    ├─ Thread 1
    │  └─► ThreadLocal Driver 1 → Device 1
    │
    ├─ Thread 2
    │  └─► ThreadLocal Driver 2 → Device 2
    │
    └─ Thread 3
       └─► ThreadLocal Driver 3 → Device 3
```

### Multi-Device Execution
```
DeviceManager
    ├─ Device 1 (UDID specified in TestNG)
    ├─ Device 2 (UDID specified in TestNG)
    └─ Device 3 (Auto-detected)
```

---

## 🎯 Configuration-Driven Philosophy

Everything is **configuration-driven** - no code changes needed:

```
Switch Platform        → Change: platform=ios
Switch Execution       → Change: execution.type=browserstack
Switch Device Type     → Change: android.deviceType=real
Switch Specific Device → Change: android.udid=DEVICE_ID
Switch Environment     → Set environment variables
```

---

**This architecture ensures:**
- ✅ Scalability - Easy to add new platforms/devices
- ✅ Maintainability - Clear separation of concerns
- ✅ Testability - Each component can be tested independently
- ✅ Flexibility - Configuration-driven behavior
- ✅ Reliability - Robust error handling and validation
