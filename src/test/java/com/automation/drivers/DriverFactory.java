package com.automation.drivers;

import com.automation.utils.BrowserStackCapabilityManager;
import com.automation.utils.DeviceManager;
import com.automation.utils.PropertyReader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

/**
 * Factory class to create and configure AppiumDriver instances
 * Supports both Android and iOS on Local Appium and BrowserStack
 */
public class DriverFactory {
    
    private static final Logger logger = LogManager.getLogger(DriverFactory.class);
    
    /**
     * Initialize AppiumDriver based on platform and execution type configuration
     * @return AppiumDriver instance (AndroidDriver, IOSDriver, or RemoteWebDriver)
     */
    public static AppiumDriver initializeDriver() {
        String platform = PropertyReader.getPlatform();
        String executionType = PropertyReader.getConfigProperty("execution.type");
        
        logger.info("Initializing driver for platform: " + platform);
        logger.info("Execution type: " + executionType);
        
        AppiumDriver driver;
        
        try {
            // Determine execution type: local or browserstack
            if (executionType.equalsIgnoreCase("browserstack")) {
                driver = createBrowserStackDriver(platform);
            } else {
                // Local execution
                if (platform.equalsIgnoreCase("android")) {
                    driver = createLocalAndroidDriver();
                } else if (platform.equalsIgnoreCase("ios")) {
                    driver = createLocalIOSDriver();
                } else {
                    throw new RuntimeException("Invalid platform specified: " + platform);
                }
            }
            
            // Set implicit wait
            int implicitWait = Integer.parseInt(PropertyReader.getConfigProperty("implicit.wait"));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            
            logger.info("Driver initialized successfully");
            return driver;
            
        } catch (Exception e) {
            logger.error("Failed to initialize driver: " + e.getMessage());
            throw new RuntimeException("Driver initialization failed", e);
        }
    }
    
    /**
     * Create BrowserStack driver
     * @param platform Platform type (android/ios)
     * @return AppiumDriver instance for BrowserStack
     */
    private static AppiumDriver createBrowserStackDriver(String platform) throws MalformedURLException {
        logger.info("Creating BrowserStack driver for platform: " + platform);
        
        // Validate BrowserStack configuration
        if (!BrowserStackCapabilityManager.validateBrowserStackConfig()) {
            throw new RuntimeException("BrowserStack configuration validation failed");
        }
        
        // Get BrowserStack capabilities based on platform
        DesiredCapabilities capabilities;
        if (platform.equalsIgnoreCase("android")) {
            capabilities = BrowserStackCapabilityManager.getAndroidCapabilities();
        } else if (platform.equalsIgnoreCase("ios")) {
            capabilities = BrowserStackCapabilityManager.getIosCapabilities();
        } else {
            throw new RuntimeException("Invalid platform for BrowserStack: " + platform);
        }
        
        // Get BrowserStack Hub URL
        String browserStackUrl = BrowserStackCapabilityManager.getBrowserStackUrl();
        
        logger.info("Connecting to BrowserStack...");
        return new AppiumDriver(new URL(browserStackUrl), capabilities);
    }
    
    /**
     * Create and configure AndroidDriver for local execution
     * Uses DeviceManager for automatic device detection
     * @return AndroidDriver instance
     */
    private static AndroidDriver createLocalAndroidDriver() throws MalformedURLException {
        logger.info("Creating local Android driver");
        
        // Get device using DeviceManager (auto-detects real devices)
        Map<String, String> device = DeviceManager.getDevice();
        DeviceManager.printDeviceInfo();
        
        UiAutomator2Options options = new UiAutomator2Options();
        
        // Set device name and platform version from DeviceManager
        String deviceName = device.get("deviceName");
        String platformVersion = device.get("platformVersion");
        
        // Fall back to config if not detected
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = PropertyReader.getConfigProperty("android.deviceName");
        }
        if (platformVersion == null || platformVersion.isEmpty()) {
            platformVersion = PropertyReader.getConfigProperty("android.platformVersion");
        }
        
        options.setDeviceName(deviceName);
        options.setPlatformVersion(platformVersion);
        options.setAutomationName(PropertyReader.getConfigProperty("android.automationName"));
        options.setApp(PropertyReader.getConfigProperty("android.app"));
        options.setAppPackage(PropertyReader.getConfigProperty("android.appPackage"));
        options.setAppActivity(PropertyReader.getConfigProperty("android.appActivity"));
        
        // Set UDID if detected or configured
        String udid = device.get("udid");
        if (udid != null && !udid.isEmpty()) {
            options.setUdid(udid);
            logger.info("Using device with UDID: " + udid);
        }
        
        // Set common capabilities
        options.setNoReset(Boolean.parseBoolean(PropertyReader.getConfigProperty("android.noReset")));
        options.setFullReset(Boolean.parseBoolean(PropertyReader.getConfigProperty("android.fullReset")));
        options.setAutoGrantPermissions(Boolean.parseBoolean(PropertyReader.getConfigProperty("autoGrantPermissions")));
        options.setNewCommandTimeout(Duration.ofSeconds(
                Integer.parseInt(PropertyReader.getConfigProperty("newCommandTimeout"))));
        
        // Real device specific settings
        String deviceType = device.get("deviceType");
        if ("real".equalsIgnoreCase(deviceType)) {
            String skipDeviceInit = PropertyReader.getConfigProperty("android.skipDeviceInitialization");
            if (skipDeviceInit != null && !skipDeviceInit.isEmpty()) {
                options.setCapability("skipDeviceInitialization", Boolean.parseBoolean(skipDeviceInit));
            }
            
            String skipServerInstall = PropertyReader.getConfigProperty("android.skipServerInstallation");
            if (skipServerInstall != null && !skipServerInstall.isEmpty()) {
                options.setCapability("skipServerInstallation", Boolean.parseBoolean(skipServerInstall));
            }
            
            String systemPort = PropertyReader.getConfigProperty("android.systemPort");
            if (systemPort != null && !systemPort.isEmpty()) {
                options.setSystemPort(Integer.parseInt(systemPort));
            }
            
            logger.info("Real device capabilities configured");
        }
        
        logger.info("Android capabilities configured");
        
        // Get Appium server URL
        String appiumUrl = PropertyReader.getConfigProperty("appium.server.url");
        
        return new AndroidDriver(new URL(appiumUrl), options);
    }
    
    /**
     * Create and configure IOSDriver for local execution
     * Uses DeviceManager for device information
     * @return IOSDriver instance
     */
    private static IOSDriver createLocalIOSDriver() throws MalformedURLException {
        logger.info("Creating local iOS driver");
        
        // Get device using DeviceManager
        Map<String, String> device = DeviceManager.getDevice();
        DeviceManager.printDeviceInfo();
        
        XCUITestOptions options = new XCUITestOptions();
        
        // Set device name and platform version from DeviceManager
        String deviceName = device.get("deviceName");
        String platformVersion = device.get("platformVersion");
        
        // Fall back to config if not available
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = PropertyReader.getConfigProperty("ios.deviceName");
        }
        if (platformVersion == null || platformVersion.isEmpty()) {
            platformVersion = PropertyReader.getConfigProperty("ios.platformVersion");
        }
        
        options.setDeviceName(deviceName);
        options.setPlatformVersion(platformVersion);
        options.setAutomationName(PropertyReader.getConfigProperty("ios.automationName"));
        options.setApp(PropertyReader.getConfigProperty("ios.app"));
        options.setBundleId(PropertyReader.getConfigProperty("ios.bundleId"));
        
        // Set UDID if available
        String udid = device.get("udid");
        String deviceType = device.get("deviceType");
        
        if ("real".equalsIgnoreCase(deviceType) && udid != null && !udid.isEmpty()) {
            options.setUdid(udid);
            logger.info("Using real iOS device with UDID: " + udid);
            
            // Real device requires signing configuration
            String xcodeOrgId = PropertyReader.getConfigProperty("ios.xcodeOrgId");
            String xcodeSigningId = PropertyReader.getConfigProperty("ios.xcodeSigningId");
            
            if (xcodeOrgId != null && !xcodeOrgId.isEmpty()) {
                options.setCapability("xcodeOrgId", xcodeOrgId);
                logger.info("Xcode Organization ID configured");
            } else {
                logger.warn("Real device detected but xcodeOrgId not configured. This may cause issues.");
            }
            
            if (xcodeSigningId != null && !xcodeSigningId.isEmpty()) {
                options.setCapability("xcodeSigningId", xcodeSigningId);
            }
            
            // WDA Bundle ID for real device
            String wdaBundleId = PropertyReader.getConfigProperty("ios.updatedWDABundleId");
            if (wdaBundleId != null && !wdaBundleId.isEmpty()) {
                options.setUpdatedWdaBundleId(wdaBundleId);
            }
            
            // Use new WDA
            String useNewWDA = PropertyReader.getConfigProperty("ios.useNewWDA");
            if (useNewWDA != null && !useNewWDA.isEmpty()) {
                options.setCapability("useNewWDA", Boolean.parseBoolean(useNewWDA));
            }
            
            // WDA Local Port
            String wdaLocalPort = PropertyReader.getConfigProperty("ios.wdaLocalPort");
            if (wdaLocalPort != null && !wdaLocalPort.isEmpty()) {
                options.setWdaLocalPort(Integer.parseInt(wdaLocalPort));
            }
            
            logger.info("Real device capabilities configured");
        }
        
        // Set common capabilities
        options.setNoReset(Boolean.parseBoolean(PropertyReader.getConfigProperty("ios.noReset")));
        options.setFullReset(Boolean.parseBoolean(PropertyReader.getConfigProperty("ios.fullReset")));
        options.setAutoAcceptAlerts(Boolean.parseBoolean(PropertyReader.getConfigProperty("autoAcceptAlerts")));
        options.setNewCommandTimeout(Duration.ofSeconds(
                Integer.parseInt(PropertyReader.getConfigProperty("newCommandTimeout"))));
        
        logger.info("iOS capabilities configured");
        
        // Get Appium server URL
        String appiumUrl = PropertyReader.getConfigProperty("appium.server.url");
        
        return new IOSDriver(new URL(appiumUrl), options);
    }
}
