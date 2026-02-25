package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Utility class to manage BrowserStack capabilities
 * Handles BrowserStack-specific configurations for cloud execution
 */
public class BrowserStackCapabilityManager {
    
    private static final Logger logger = LogManager.getLogger(BrowserStackCapabilityManager.class);
    
    /**
     * Get BrowserStack capabilities for Android
     * @return DesiredCapabilities for Android on BrowserStack
     */
    public static DesiredCapabilities getAndroidCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        // BrowserStack credentials
        String username = PropertyReader.getConfigProperty("browserstack.username");
        String accessKey = PropertyReader.getConfigProperty("browserstack.accessKey");
        
        // Platform capabilities
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("device", PropertyReader.getConfigProperty("browserstack.android.device"));
        capabilities.setCapability("os_version", PropertyReader.getConfigProperty("browserstack.android.osVersion"));
        
        // App capabilities
        capabilities.setCapability("app", PropertyReader.getConfigProperty("browserstack.android.app"));
        
        // BrowserStack specific capabilities
        capabilities.setCapability("project", PropertyReader.getConfigProperty("browserstack.project"));
        capabilities.setCapability("build", getBuildName());
        capabilities.setCapability("name", PropertyReader.getConfigProperty("browserstack.sessionName") + " - Android");
        
        // Additional BrowserStack features
        capabilities.setCapability("browserstack.debug", 
            PropertyReader.getConfigProperty("browserstack.debug"));
        capabilities.setCapability("browserstack.networkLogs", 
            PropertyReader.getConfigProperty("browserstack.networkLogs"));
        capabilities.setCapability("browserstack.video", 
            PropertyReader.getConfigProperty("browserstack.video"));
        capabilities.setCapability("browserstack.local", 
            PropertyReader.getConfigProperty("browserstack.local"));
        capabilities.setCapability("browserstack.acceptSslCerts", 
            PropertyReader.getConfigProperty("browserstack.acceptSslCerts"));
        capabilities.setCapability("browserstack.timezone", 
            PropertyReader.getConfigProperty("browserstack.timezone"));
        
        // Automation capabilities
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("autoGrantPermissions", true);
        capabilities.setCapability("autoAcceptAlerts", true);
        
        logger.info("BrowserStack Android capabilities configured");
        return capabilities;
    }
    
    /**
     * Get BrowserStack capabilities for iOS
     * @return DesiredCapabilities for iOS on BrowserStack
     */
    public static DesiredCapabilities getIosCapabilities() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        
        // BrowserStack credentials
        String username = PropertyReader.getConfigProperty("browserstack.username");
        String accessKey = PropertyReader.getConfigProperty("browserstack.accessKey");
        
        // Platform capabilities
        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("device", PropertyReader.getConfigProperty("browserstack.ios.device"));
        capabilities.setCapability("os_version", PropertyReader.getConfigProperty("browserstack.ios.osVersion"));
        
        // App capabilities
        capabilities.setCapability("app", PropertyReader.getConfigProperty("browserstack.ios.app"));
        
        // BrowserStack specific capabilities
        capabilities.setCapability("project", PropertyReader.getConfigProperty("browserstack.project"));
        capabilities.setCapability("build", getBuildName());
        capabilities.setCapability("name", PropertyReader.getConfigProperty("browserstack.sessionName") + " - iOS");
        
        // Additional BrowserStack features
        capabilities.setCapability("browserstack.debug", 
            PropertyReader.getConfigProperty("browserstack.debug"));
        capabilities.setCapability("browserstack.networkLogs", 
            PropertyReader.getConfigProperty("browserstack.networkLogs"));
        capabilities.setCapability("browserstack.video", 
            PropertyReader.getConfigProperty("browserstack.video"));
        capabilities.setCapability("browserstack.local", 
            PropertyReader.getConfigProperty("browserstack.local"));
        capabilities.setCapability("browserstack.acceptSslCerts", 
            PropertyReader.getConfigProperty("browserstack.acceptSslCerts"));
        capabilities.setCapability("browserstack.timezone", 
            PropertyReader.getConfigProperty("browserstack.timezone"));
        
        // Automation capabilities
        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("autoAcceptAlerts", true);
        
        logger.info("BrowserStack iOS capabilities configured");
        return capabilities;
    }
    
    /**
     * Get BrowserStack Hub URL
     * @return BrowserStack Hub URL with credentials
     */
    public static String getBrowserStackUrl() {
        String username = PropertyReader.getConfigProperty("browserstack.username");
        String accessKey = PropertyReader.getConfigProperty("browserstack.accessKey");
        String hubUrl = PropertyReader.getConfigProperty("browserstack.url");
        
        if (username == null || accessKey == null) {
            logger.error("BrowserStack credentials not found. Please set browserstack.username and browserstack.accessKey");
            throw new RuntimeException("BrowserStack credentials not configured");
        }
        
        // Format: https://username:accessKey@hub-cloud.browserstack.com/wd/hub
        String url = hubUrl.replace("https://", "https://" + username + ":" + accessKey + "@");
        logger.info("BrowserStack URL configured");
        return url;
    }
    
    /**
     * Get build name with timestamp
     * @return Build name
     */
    private static String getBuildName() {
        String buildName = PropertyReader.getConfigProperty("browserstack.build");
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
        return buildName + "_" + timestamp;
    }
    
    /**
     * Validate BrowserStack configuration
     * @return true if configuration is valid
     */
    public static boolean validateBrowserStackConfig() {
        try {
            String username = PropertyReader.getConfigProperty("browserstack.username");
            String accessKey = PropertyReader.getConfigProperty("browserstack.accessKey");
            String platform = PropertyReader.getPlatform();
            
            if (username == null || username.isEmpty() || username.equals("your_username")) {
                logger.error("BrowserStack username not configured");
                return false;
            }
            
            if (accessKey == null || accessKey.isEmpty() || accessKey.equals("your_access_key")) {
                logger.error("BrowserStack access key not configured");
                return false;
            }
            
            String appId = platform.equalsIgnoreCase("android") 
                ? PropertyReader.getConfigProperty("browserstack.android.app")
                : PropertyReader.getConfigProperty("browserstack.ios.app");
                
            if (appId == null || appId.isEmpty() || appId.startsWith("bs://your_")) {
                logger.error("BrowserStack app ID not configured for platform: " + platform);
                return false;
            }
            
            logger.info("BrowserStack configuration validated successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("BrowserStack configuration validation failed: " + e.getMessage());
            return false;
        }
    }
}
