package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Device Manager - Centralized device handling
 * Automatically detects and manages real devices and emulators
 */
public class DeviceManager {
    
    private static final Logger logger = LogManager.getLogger(DeviceManager.class);
    private static Map<String, String> selectedDevice = new HashMap<>();
    
    /**
     * Get device for execution based on configuration
     * Automatically detects real devices or uses configured device
     * @return Map with device details (udid, name, type)
     */
    public static Map<String, String> getDevice() {
        String platform = PropertyReader.getConfigProperty("platform");
        String executionTarget = PropertyReader.getExecutionTarget();
        String deviceType = PropertyReader.getConfigProperty(platform + ".deviceType");
        
        logger.info("Platform: " + platform + ", Execution target: " + executionTarget + ", Device Type: " + deviceType);

        // For BrowserStack, return configured details
        if ("browserstack".equals(executionTarget)) {
            logger.info("BrowserStack execution - using cloud devices");
            return getBrowserStackDevice(platform);
        }
        
        // For local execution
        if (platform.equalsIgnoreCase("android")) {
            return getAndroidDevice(deviceType);
        } else if (platform.equalsIgnoreCase("ios")) {
            return getIOSDevice(deviceType);
        }
        
        return new HashMap<>();
    }
    
    /**
     * Get Android device (real or emulator)
     * @param deviceType Type of device (real/emulator)
     * @return Device details
     */
    private static Map<String, String> getAndroidDevice(String deviceType) {
        Map<String, String> device = new HashMap<>();
        
        // Check if specific UDID is provided in config
        String configuredUdid = PropertyReader.getConfigProperty("android.udid");
        
        if (deviceType != null && deviceType.equalsIgnoreCase("real")) {
            logger.info("Real Android device execution requested");
            
            // Check ADB availability
            if (!AdbHelper.isAdbAvailable()) {
                throw new RuntimeException("ADB not available. Please ensure Android SDK is installed and in PATH.");
            }
            
            // Get connected devices
            List<String> devices = AdbHelper.getConnectedDevices();
            
            if (devices.isEmpty()) {
                throw new RuntimeException("No Android devices connected. Please connect a real device via USB and enable USB debugging.");
            }
            
            // Filter for real devices only
            String realDeviceId = null;
            for (String deviceId : devices) {
                if (AdbHelper.isRealDevice(deviceId)) {
                    realDeviceId = deviceId;
                    logger.info("Real device detected: " + deviceId);
                    break;
                }
            }
            
            if (realDeviceId == null) {
                logger.warn("No real device found among connected devices. Using first available device.");
                realDeviceId = devices.getFirst();
            }
            
            // Use configured UDID if provided and connected, otherwise use detected
            String selectedUdid = realDeviceId;
            if (configuredUdid != null && !configuredUdid.isEmpty()) {
                if (devices.contains(configuredUdid)) {
                    selectedUdid = configuredUdid;
                    logger.info("Using configured UDID: " + configuredUdid);
                } else {
                    logger.warn("Configured UDID not found in connected devices. Using detected device: " + realDeviceId);
                }
            }
            
            // Get device information
            Map<String, String> deviceInfo = AdbHelper.getDeviceInfo(selectedUdid);
            
            device.put("udid", selectedUdid);
            device.put("deviceName", deviceInfo.getOrDefault("model", "Android Device"));
            device.put("platformVersion", deviceInfo.getOrDefault("androidVersion", PropertyReader.getConfigProperty("android.platformVersion")));
            device.put("deviceType", "real");
            device.put("manufacturer", deviceInfo.getOrDefault("manufacturer", "Unknown"));
            device.put("model", deviceInfo.getOrDefault("model", "Unknown"));
            
            logger.info("Selected Real Device - UDID: " + selectedUdid + ", Model: " + device.get("model"));
            
        } else {
            // Emulator execution
            logger.info("Emulator execution");
            
            // Check if emulator is running
            if (AdbHelper.isAdbAvailable()) {
                List<String> devices = AdbHelper.getConnectedDevices();
                
                // Find emulator (starts with "emulator-")
                String emulatorId = null;
                for (String deviceId : devices) {
                    if (!AdbHelper.isRealDevice(deviceId)) {
                        emulatorId = deviceId;
                        logger.info("Emulator detected: " + deviceId);
                        break;
                    }
                }
                
                if (emulatorId != null) {
                    Map<String, String> deviceInfo = AdbHelper.getDeviceInfo(emulatorId);
                    device.put("udid", emulatorId);
                    device.put("deviceName", deviceInfo.getOrDefault("model", PropertyReader.getConfigProperty("android.deviceName")));
                    device.put("platformVersion", deviceInfo.getOrDefault("androidVersion", PropertyReader.getConfigProperty("android.platformVersion")));
                } else {
                    logger.warn("No emulator detected. Using configured device details.");
                    device.put("deviceName", PropertyReader.getConfigProperty("android.deviceName"));
                    device.put("platformVersion", PropertyReader.getConfigProperty("android.platformVersion"));
                }
            } else {
                // Fallback to configured values
                device.put("deviceName", PropertyReader.getConfigProperty("android.deviceName"));
                device.put("platformVersion", PropertyReader.getConfigProperty("android.platformVersion"));
            }
            
            device.put("deviceType", "emulator");
        }
        
        selectedDevice = device;
        return device;
    }
    
    /**
     * Get iOS device (real or simulator)
     * @param deviceType Type of device (real/simulator)
     * @return Device details
     */
    private static Map<String, String> getIOSDevice(String deviceType) {
        Map<String, String> device = new HashMap<>();
        
        if (deviceType != null && deviceType.equalsIgnoreCase("real")) {
            logger.info("Real iOS device execution requested");
            
            // For real iOS device, UDID must be configured
            String udid = PropertyReader.getConfigProperty("ios.udid");
            if (udid == null || udid.isEmpty()) {
                throw new RuntimeException("Real iOS device selected but UDID not configured. Please set ios.udid in config.properties");
            }
            
            device.put("udid", udid);
            device.put("deviceName", PropertyReader.getConfigProperty("ios.deviceName"));
            device.put("platformVersion", PropertyReader.getConfigProperty("ios.platformVersion"));
            device.put("deviceType", "real");
            
            // Verify required signing configuration
            String xcodeOrgId = PropertyReader.getConfigProperty("ios.xcodeOrgId");
            if (xcodeOrgId == null || xcodeOrgId.isEmpty()) {
                logger.warn("Real iOS device requires xcodeOrgId to be configured for signing.");
            }
            
            logger.info("Selected Real iOS Device - UDID: " + udid);
            
        } else {
            // Simulator execution
            logger.info("iOS Simulator execution");
            
            device.put("deviceName", PropertyReader.getConfigProperty("ios.deviceName"));
            device.put("platformVersion", PropertyReader.getConfigProperty("ios.platformVersion"));
            device.put("deviceType", "simulator");
        }
        
        selectedDevice = device;
        return device;
    }
    
    /**
     * Get BrowserStack device details
     * @param platform Platform (android/ios)
     * @return Device details
     */
    private static Map<String, String> getBrowserStackDevice(String platform) {
        Map<String, String> device = new HashMap<>();
        
        device.put("deviceType", "browserstack");
        device.put("platform", platform);
        
        if (platform.equalsIgnoreCase("android")) {
            device.put("deviceName", PropertyReader.getConfigProperty("browserstack.android.device"));
            device.put("platformVersion", PropertyReader.getConfigProperty("browserstack.android.osVersion"));
        } else {
            device.put("deviceName", PropertyReader.getConfigProperty("browserstack.ios.device"));
            device.put("platformVersion", PropertyReader.getConfigProperty("browserstack.ios.osVersion"));
        }
        
        logger.info("BrowserStack Device - " + device.get("deviceName") + " (" + platform + " " + device.get("platformVersion") + ")");
        
        return device;
    }
    
    /**
     * Get currently selected device details
     * @return Selected device map
     */
    public static Map<String, String> getSelectedDevice() {
        return selectedDevice;
    }
    
    /**
     * Print device information
     */
    public static void printDeviceInfo() {
        if (selectedDevice.isEmpty()) {
            logger.warn("No device selected yet");
            return;
        }
        
        logger.info("\n" +
                "=".repeat(50) + "\n" +
                "SELECTED DEVICE INFORMATION\n" +
                "=".repeat(50) + "\n" +
                "Device Type     : " + selectedDevice.getOrDefault("deviceType", "N/A") + "\n" +
                "Device Name     : " + selectedDevice.getOrDefault("deviceName", "N/A") + "\n" +
                "Platform Version: " + selectedDevice.getOrDefault("platformVersion", "N/A") + "\n" +
                "UDID            : " + selectedDevice.getOrDefault("udid", "N/A") + "\n" +
                "Manufacturer    : " + selectedDevice.getOrDefault("manufacturer", "N/A") + "\n" +
                "Model           : " + selectedDevice.getOrDefault("model", "N/A") + "\n" +
                "=".repeat(50));
    }
    
    /**
     * Validate device availability before test execution
     * @return true if device is available and ready
     */
    public static boolean validateDeviceAvailability() {
        try {
            String platform = PropertyReader.getConfigProperty("platform");
            String executionType = PropertyReader.getConfigProperty("execution.type");
            String deviceType = PropertyReader.getConfigProperty(platform + ".deviceType");
            
            // BrowserStack validation
            if (executionType.equalsIgnoreCase("browserstack")) {
                return BrowserStackCapabilityManager.validateBrowserStackConfig();
            }
            
            // Android validation
            if (platform.equalsIgnoreCase("android")) {
                if (deviceType != null && deviceType.equalsIgnoreCase("real")) {
                    if (!AdbHelper.isAdbAvailable()) {
                        logger.error("ADB not available");
                        return false;
                    }
                    
                    List<String> devices = AdbHelper.getConnectedDevices();
                    if (devices.isEmpty()) {
                        logger.error("No Android devices connected");
                        return false;
                    }
                    
                    logger.info("Android real device validation passed");
                    return true;
                }
            }
            
            // iOS validation
            if (platform.equalsIgnoreCase("ios")) {
                if (deviceType != null && deviceType.equalsIgnoreCase("real")) {
                    String udid = PropertyReader.getConfigProperty("ios.udid");
                    if (udid == null || udid.isEmpty()) {
                        logger.error("iOS real device UDID not configured");
                        return false;
                    }
                    logger.info("iOS real device validation passed");
                    return true;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Device validation failed: " + e.getMessage());
            return false;
        }
    }
}
