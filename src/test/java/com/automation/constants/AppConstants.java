package com.automation.constants;

/**
 * Application constants
 * Contains constant values used across the framework
 */
public class AppConstants {
    
    // Timeout constants (in seconds)
    public static final int IMPLICIT_WAIT = 10;
    public static final int EXPLICIT_WAIT = 20;
    public static final int PAGE_LOAD_TIMEOUT = 30;
    
    // Platform constants
    public static final String PLATFORM_ANDROID = "android";
    public static final String PLATFORM_IOS = "ios";
    
    // Execution type constants
    public static final String EXECUTION_LOCAL = "local";
    public static final String EXECUTION_BROWSERSTACK = "browserstack";
    
    // Device type constants
    public static final String DEVICE_TYPE_EMULATOR = "emulator";
    public static final String DEVICE_TYPE_SIMULATOR = "simulator";
    public static final String DEVICE_TYPE_REAL = "real";
    
    // Locator types
    public static final String LOCATOR_ID = "id";
    public static final String LOCATOR_XPATH = "xpath";
    public static final String LOCATOR_NAME = "name";
    public static final String LOCATOR_CLASS = "class";
    public static final String LOCATOR_ACCESSIBILITY_ID = "accessibility_id";
    
    // Report paths
    public static final String EXTENT_REPORT_PATH = "target/reports/";
    public static final String SCREENSHOT_PATH = "target/screenshots/";
    public static final String LOG_PATH = "target/logs/";
    public static final String CUCUMBER_REPORT_PATH = "target/cucumber-reports/";
    
    // File paths
    public static final String CONFIG_FILE = "src/test/resources/config.properties";
    public static final String ANDROID_LOCATORS_FILE = "src/test/resources/locators/android_locators.properties";
    public static final String IOS_LOCATORS_FILE = "src/test/resources/locators/ios_locators.properties";
    
    // Test data
    public static final String DEFAULT_USERNAME = "testuser@example.com";
    public static final String DEFAULT_PASSWORD = "Test@123";
    
    // Messages
    public static final String LOGIN_SUCCESS_MSG = "Login successful";
    public static final String LOGIN_FAILED_MSG = "Invalid credentials";
    public static final String LOGOUT_SUCCESS_MSG = "Logout successful";
    
    // Error messages
    public static final String DRIVER_INIT_ERROR = "Failed to initialize driver";
    public static final String ELEMENT_NOT_FOUND_ERROR = "Element not found";
    public static final String TIMEOUT_ERROR = "Timeout waiting for element";
    
    private AppConstants() {
        // Private constructor to prevent instantiation
        throw new IllegalStateException("Constants class");
    }
}
