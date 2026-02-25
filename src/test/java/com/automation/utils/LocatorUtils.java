package com.automation.utils;

import io.appium.java_client.AppiumBy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

/**
 * Utility class to fetch locators based on platform
 * Automatically detects locator type (id or xpath) and returns appropriate By object
 */
public class LocatorUtils {
    
    private static final Logger logger = LogManager.getLogger(LocatorUtils.class);
    
    /**
     * Get locator based on current platform
     * @param locatorKey Key from locator properties file
     * @return By object for the locator
     */
    public static By getLocator(String locatorKey) {
        String platform = PropertyReader.getPlatform();
        String locatorValue;
        
        if (platform.equalsIgnoreCase("android")) {
            locatorValue = PropertyReader.getAndroidLocator(locatorKey);
            logger.debug("Android locator for key '" + locatorKey + "': " + locatorValue);
        } else if (platform.equalsIgnoreCase("ios")) {
            locatorValue = PropertyReader.getIosLocator(locatorKey);
            logger.debug("iOS locator for key '" + locatorKey + "': " + locatorValue);
        } else {
            throw new RuntimeException("Invalid platform: " + platform);
        }
        
        if (locatorValue == null) {
            throw new RuntimeException("Locator not found for key: " + locatorKey);
        }
        
        return getByObject(locatorKey, locatorValue);
    }
    
    /**
     * Convert locator string to By object based on locator type
     * @param locatorKey Locator key
     * @param locatorValue Locator value
     * @return By object
     */
    private static By getByObject(String locatorKey, String locatorValue) {
        By locator;
        
        // Determine locator type from the key
        if (locatorKey.endsWith(".id")) {
            locator = By.id(locatorValue);
            logger.debug("Using ID locator: " + locatorValue);
        } else if (locatorKey.endsWith(".xpath")) {
            locator = By.xpath(locatorValue);
            logger.debug("Using XPath locator: " + locatorValue);
        } else if (locatorKey.endsWith(".name")) {
            locator = By.name(locatorValue);
            logger.debug("Using Name locator: " + locatorValue);
        } else if (locatorKey.endsWith(".class")) {
            locator = By.className(locatorValue);
            logger.debug("Using ClassName locator: " + locatorValue);
        } else if (locatorKey.endsWith(".uiautomator")) {
            locator = AppiumBy.androidUIAutomator(locatorValue);
            logger.debug("Using AndroidUIAutomator locator: " + locatorValue);
        } else {
            // Default to ID if no type specified
            locator = By.id(locatorValue);
            logger.debug("Using default ID locator: " + locatorValue);
        }
        
        return locator;
    }
}
