package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class to read property files
 * Supports reading config.properties, platform-specific locator properties,
 * and per-feature test data files from src/test/resources/testdata/
 */
public class PropertyReader {
    
    private static final Logger logger = LogManager.getLogger(PropertyReader.class);
    private static Properties configProperties;
    private static Properties androidLocators;
    private static Properties iosLocators;
    
    private static final String CONFIG_FILE = "src/test/resources/config.properties";
    private static final String ANDROID_LOCATORS_FILE = "src/test/resources/locators/android_locators.properties";
    private static final String IOS_LOCATORS_FILE = "src/test/resources/locators/ios_locators.properties";
    private static final String TESTDATA_FILE = "src/test/resources/testdata/testdata.properties";
    private static Properties testDataProperties;
    
    /**
     * Load configuration properties
     */
    public static void loadConfigProperties() {
        try {
            FileInputStream fis = new FileInputStream(CONFIG_FILE);
            configProperties = new Properties();
            configProperties.load(fis);
            logger.info("Configuration properties loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load configuration properties: " + e.getMessage());
            throw new RuntimeException("Failed to load config.properties file", e);
        }
    }
    
    /**
     * Load Android locator properties
     */
    public static void loadAndroidLocators() {
        try {
            FileInputStream fis = new FileInputStream(ANDROID_LOCATORS_FILE);
            androidLocators = new Properties();
            androidLocators.load(fis);
            logger.info("Android locators loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load Android locators: " + e.getMessage());
            throw new RuntimeException("Failed to load android_locators.properties file", e);
        }
    }
    
    /**
     * Load iOS locator properties
     */
    public static void loadIosLocators() {
        try {
            FileInputStream fis = new FileInputStream(IOS_LOCATORS_FILE);
            iosLocators = new Properties();
            iosLocators.load(fis);
            logger.info("iOS locators loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load iOS locators: " + e.getMessage());
            throw new RuntimeException("Failed to load ios_locators.properties file", e);
        }
    }
    
    /**
     * Get a test data value from the single testdata.properties file.
     * Only credentials (country code, mobile number, OTP, PIN) are stored here.
     * All other test data (recipient, amount, currency) is defined inline in feature files.
     * Also checks environment variables for CI/CD override support.
     *
     * @param key Property key (e.g. "mobile.number", "otp", "pin")
     * @return Property value
     */
    public static String getTestDataProperty(String key) {
        // Check environment variable first
        String envValue = getEnvironmentVariable(key);
        if (envValue != null) {
            logger.info("Using environment variable for testdata key: " + key);
            return envValue;
        }

        if (testDataProperties == null) {
            try {
                FileInputStream fis = new FileInputStream(TESTDATA_FILE);
                testDataProperties = new Properties();
                testDataProperties.load(fis);
                logger.info("Loaded testdata file: " + TESTDATA_FILE);
            } catch (IOException e) {
                logger.error("Failed to load testdata file: " + TESTDATA_FILE + " - " + e.getMessage());
                throw new RuntimeException("Failed to load testdata.properties", e);
            }
        }

        String value = testDataProperties.getProperty(key);
        if (value == null) {
            logger.warn("Testdata key not found: " + key);
        }
        return value;
    }

    /**
     * Get configuration property value
     * @param key Property key
     * @return Property value
     */
    public static String getConfigProperty(String key) {
        if (configProperties == null) {
            loadConfigProperties();
        }
        
        // Check for environment variable first (for CI/CD support)
        String envValue = getEnvironmentVariable(key);
        if (envValue != null) {
            logger.info("Using environment variable for: " + key);
            return envValue;
        }
        
        String value = configProperties.getProperty(key);
        if (value == null) {
            logger.warn("Property key not found: " + key);
        }
        return value;
    }

    /**
     * Override / inject a config property at runtime (e.g. auto-detected device UDID).
     * Takes priority over file values for the lifetime of the JVM.
     *
     * @param key   Property key
     * @param value Property value
     */
    public static void overrideConfigProperty(String key, String value) {
        if (configProperties == null) {
            loadConfigProperties();
        }
        configProperties.setProperty(key, value);
        logger.info("Config property overridden at runtime: {} = {}", key, value);
    }
    
    /**
     * Get configuration property value with default
     * @param key Property key
     * @param defaultValue Default value if property not found
     * @return Property value or default value
     */
    public static String getProperty(String key, String defaultValue) {
        String value = getConfigProperty(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Get environment variable value
     * Supports both direct key and uppercase with underscores
     * @param key Property key
     * @return Environment variable value
     */
    private static String getEnvironmentVariable(String key) {
        // Try direct key
        String value = System.getenv(key);
        if (value != null) {
            return value;
        }
        
        // Try uppercase with underscores (e.g., browserstack.username -> BROWSERSTACK_USERNAME)
        String upperKey = key.toUpperCase().replace(".", "_");
        value = System.getenv(upperKey);
        if (value != null) {
            return value;
        }
        
        return null;
    }
    
    /**
     * Get Android locator value
     * @param key Locator key
     * @return Locator value
     */
    public static String getAndroidLocator(String key) {
        if (androidLocators == null) {
            loadAndroidLocators();
        }
        String value = androidLocators.getProperty(key);
        if (value == null) {
            logger.warn("Android locator key not found: " + key);
        }
        return value;
    }
    
    /**
     * Get iOS locator value
     * @param key Locator key
     * @return Locator value
     */
    public static String getIosLocator(String key) {
        if (iosLocators == null) {
            loadIosLocators();
        }
        String value = iosLocators.getProperty(key);
        if (value == null) {
            logger.warn("iOS locator key not found: " + key);
        }
        return value;
    }
    
    /**
     * Get platform from config
     * @return Current platform (android/ios)
     */
    public static String getPlatform() {
        return getConfigProperty("platform");
    }

    /**
     * Resolve execution target from Y/N switches.
     * Reads run.virtual.device, run.real.device, run.browserstack.
     * Only ONE should be Y at a time.
     * Falls back to legacy execution.type + android.deviceType if no switch is set.
     *
     * @return "virtual", "real", or "browserstack"
     */
    public static String getExecutionTarget() {
        String virtual     = getProperty("run.virtual.device",  "N");
        String real        = getProperty("run.real.device",     "N");
        String browserstack = getProperty("run.browserstack",   "N");

        if ("Y".equalsIgnoreCase(virtual))      return "virtual";
        if ("Y".equalsIgnoreCase(real))         return "real";
        if ("Y".equalsIgnoreCase(browserstack)) return "browserstack";

        // Legacy fallback
        String execType   = getProperty("execution.type",   "local");
        if ("browserstack".equalsIgnoreCase(execType)) return "browserstack";
        String deviceType = getProperty("android.deviceType", "real");
        return "emulator".equalsIgnoreCase(deviceType) ? "virtual" : "real";
    }
}
