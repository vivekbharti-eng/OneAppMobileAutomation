package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Sample class to demonstrate ADB Helper usage
 * Can be run standalone to get device information
 */
public class AdbInfoDemo {
    
    private static final Logger logger = LogManager.getLogger(AdbInfoDemo.class);
    
    public static void main(String[] args) {
        logger.info("ADB Device Information Tool");
        logger.info("===========================\n");
        
        // Check if ADB is available
        if (!AdbHelper.isAdbAvailable()) {
            logger.error("ADB is not available. Please ensure:");
            logger.error("1. Android SDK is installed");
            logger.error("2. ADB is in your system PATH");
            logger.error("3. USB debugging is enabled on your device");
            return;
        }
        
        logger.info("✓ ADB is available\n");
        
        // Get connected devices
        List<String> devices = AdbHelper.getConnectedDevices();
        
        if (devices.isEmpty()) {
            logger.warn("No devices connected!");
            logger.info("\nTo connect a device:");
            logger.info("1. Connect Android device via USB");
            logger.info("2. Enable USB debugging in Developer Options");
            logger.info("3. Authorize the computer on the device");
            logger.info("4. Run 'adb devices' to verify");
            return;
        }
        
        logger.info("Connected Devices: " + devices.size());
        
        // Display information for each device
        for (String deviceId : devices) {
            logger.info("\n" + "=".repeat(60));
            logger.info("Device: " + deviceId);
            logger.info("=".repeat(60));
            
            // Get full device information
            Map<String, String> info = AdbHelper.getDeviceInfo(deviceId);
            
            if (!info.isEmpty()) {
                System.out.println("\n📱 Device Details:");
                System.out.println("   Device Type      : " + AdbHelper.getDeviceType(deviceId));
                System.out.println("   Manufacturer     : " + info.get("manufacturer"));
                System.out.println("   Brand            : " + info.get("brand"));
                System.out.println("   Model            : " + info.get("model"));
                System.out.println("   Device Name      : " + info.get("device"));
                System.out.println("   Serial Number    : " + info.get("serialNumber"));
                
                System.out.println("\n🤖 Android Information:");
                System.out.println("   Android Version  : " + info.get("androidVersion"));
                System.out.println("   SDK Version      : " + info.get("sdkVersion"));
                System.out.println("   Build ID         : " + info.get("buildId"));
                
                System.out.println("\n📊 Device Status:");
                System.out.println("   Screen Resolution: " + AdbHelper.getScreenResolution(deviceId));
                System.out.println("   Battery Level    : " + AdbHelper.getBatteryLevel(deviceId));
                
                // Get installed packages count
                List<String> packages = AdbHelper.getInstalledPackages(deviceId);
                System.out.println("   Installed Apps   : " + packages.size() + " packages");
                
                // Check specific app (example)
                String testPackage = "com.android.chrome";
                boolean chromeInstalled = AdbHelper.isAppInstalled(deviceId, testPackage);
                System.out.println("   Chrome Installed : " + (chromeInstalled ? "Yes" : "No"));
                
                // Additional info for real devices
                if (AdbHelper.isRealDevice(deviceId)) {
                    System.out.println("\n🔧 Real Device Info:");
                    System.out.println("   Consider using UDID in config.properties");
                    System.out.println("   android.deviceType=real");
                    System.out.println("   android.udid=" + deviceId);
                }
            }
        }
        
        logger.info("\n" + "=".repeat(60));
        logger.info("ADB Information Retrieval Complete");
        logger.info("=".repeat(60));
    }
}
