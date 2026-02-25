package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ADB Helper utility class
 * Executes ADB commands to get device information
 */
public class AdbHelper {
    
    private static final Logger logger = LogManager.getLogger(AdbHelper.class);
    
    /**
     * Execute ADB command
     * @param command ADB command to execute
     * @return Command output
     */
    private static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command.split(" "));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            process.waitFor();
            reader.close();
            
        } catch (Exception e) {
            logger.error("Failed to execute ADB command: " + command + " - " + e.getMessage());
        }
        
        return output.toString().trim();
    }
    
    /**
     * Check if ADB is available
     * @return true if ADB is available, false otherwise
     */
    public static boolean isAdbAvailable() {
        try {
            String output = executeCommand("adb version");
            return output.contains("Android Debug Bridge");
        } catch (Exception e) {
            logger.error("ADB not available: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get list of connected devices
     * @return List of device IDs
     */
    public static List<String> getConnectedDevices() {
        List<String> devices = new ArrayList<>();
        
        try {
            String output = executeCommand("adb devices");
            String[] lines = output.split("\n");
            
            for (String line : lines) {
                if (line.contains("device") && !line.contains("List of devices")) {
                    String deviceId = line.split("\t")[0].trim();
                    if (!deviceId.isEmpty()) {
                        devices.add(deviceId);
                    }
                }
            }
            
            logger.info("Found " + devices.size() + " connected device(s)");
            
        } catch (Exception e) {
            logger.error("Failed to get connected devices: " + e.getMessage());
        }
        
        return devices;
    }
    
    /**
     * Get device information
     * @param deviceId Device ID (optional, uses first device if null)
     * @return Map of device properties
     */
    public static Map<String, String> getDeviceInfo(String deviceId) {
        Map<String, String> deviceInfo = new HashMap<>();
        
        try {
            // If no device ID provided, get first connected device
            if (deviceId == null || deviceId.isEmpty()) {
                List<String> devices = getConnectedDevices();
                if (devices.isEmpty()) {
                    logger.warn("No connected devices found");
                    return deviceInfo;
                }
                deviceId = devices.getFirst();
            }
            
            // Get device properties
            String adbPrefix = "adb -s " + deviceId + " shell getprop ";
            
            deviceInfo.put("deviceId", deviceId);
            deviceInfo.put("manufacturer", executeCommand(adbPrefix + "ro.product.manufacturer"));
            deviceInfo.put("model", executeCommand(adbPrefix + "ro.product.model"));
            deviceInfo.put("brand", executeCommand(adbPrefix + "ro.product.brand"));
            deviceInfo.put("device", executeCommand(adbPrefix + "ro.product.device"));
            deviceInfo.put("androidVersion", executeCommand(adbPrefix + "ro.build.version.release"));
            deviceInfo.put("sdkVersion", executeCommand(adbPrefix + "ro.build.version.sdk"));
            deviceInfo.put("buildId", executeCommand(adbPrefix + "ro.build.id"));
            deviceInfo.put("serialNumber", executeCommand("adb -s " + deviceId + " get-serialno"));
            
            logger.info("Device Info Retrieved: " + deviceInfo.get("manufacturer") + " " + 
                       deviceInfo.get("model") + " (Android " + deviceInfo.get("androidVersion") + ")");
            
        } catch (Exception e) {
            logger.error("Failed to get device information: " + e.getMessage());
        }
        
        return deviceInfo;
    }
    
    /**
     * Get device property
     * @param deviceId Device ID
     * @param property Property name
     * @return Property value
     */
    public static String getDeviceProperty(String deviceId, String property) {
        if (deviceId == null || deviceId.isEmpty()) {
            List<String> devices = getConnectedDevices();
            if (!devices.isEmpty()) {
                deviceId = devices.getFirst();
            }
        }
        
        String command = "adb -s " + deviceId + " shell getprop " + property;
        return executeCommand(command);
    }
    
    /**
     * Get installed package list
     * @param deviceId Device ID
     * @return List of installed packages
     */
    public static List<String> getInstalledPackages(String deviceId) {
        List<String> packages = new ArrayList<>();
        
        if (deviceId == null || deviceId.isEmpty()) {
            List<String> devices = getConnectedDevices();
            if (!devices.isEmpty()) {
                deviceId = devices.getFirst();
            }
        }
        
        try {
            String output = executeCommand("adb -s " + deviceId + " shell pm list packages");
            String[] lines = output.split("\n");
            
            for (String line : lines) {
                if (line.startsWith("package:")) {
                    packages.add(line.replace("package:", "").trim());
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to get installed packages: " + e.getMessage());
        }
        
        return packages;
    }
    
    /**
     * Check if app is installed
     * @param deviceId Device ID
     * @param packageName Package name
     * @return true if installed, false otherwise
     */
    public static boolean isAppInstalled(String deviceId, String packageName) {
        List<String> packages = getInstalledPackages(deviceId);
        return packages.contains(packageName);
    }
    
    /**
     * Install app
     * @param deviceId Device ID
     * @param apkPath Path to APK file
     * @return true if successful, false otherwise
     */
    public static boolean installApp(String deviceId, String apkPath) {
        try {
            String command = deviceId != null && !deviceId.isEmpty() 
                ? "adb -s " + deviceId + " install " + apkPath
                : "adb install " + apkPath;
                
            String output = executeCommand(command);
            boolean success = output.contains("Success");
            
            if (success) {
                logger.info("App installed successfully: " + apkPath);
            } else {
                logger.error("Failed to install app: " + output);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to install app: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Uninstall app
     * @param deviceId Device ID
     * @param packageName Package name
     * @return true if successful, false otherwise
     */
    public static boolean uninstallApp(String deviceId, String packageName) {
        try {
            String command = deviceId != null && !deviceId.isEmpty()
                ? "adb -s " + deviceId + " uninstall " + packageName
                : "adb uninstall " + packageName;
                
            String output = executeCommand(command);
            boolean success = output.contains("Success");
            
            if (success) {
                logger.info("App uninstalled successfully: " + packageName);
            } else {
                logger.error("Failed to uninstall app: " + output);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to uninstall app: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Clear app data
     * @param deviceId Device ID
     * @param packageName Package name
     * @return true if successful, false otherwise
     */
    public static boolean clearAppData(String deviceId, String packageName) {
        try {
            String command = deviceId != null && !deviceId.isEmpty()
                ? "adb -s " + deviceId + " shell pm clear " + packageName
                : "adb shell pm clear " + packageName;
                
            String output = executeCommand(command);
            boolean success = output.contains("Success");
            
            if (success) {
                logger.info("App data cleared successfully: " + packageName);
            } else {
                logger.error("Failed to clear app data: " + output);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to clear app data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get device screen resolution
     * @param deviceId Device ID
     * @return Screen resolution as "widthxheight"
     */
    public static String getScreenResolution(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            List<String> devices = getConnectedDevices();
            if (!devices.isEmpty()) {
                deviceId = devices.getFirst();
            }
        }
        
        try {
            String command = "adb -s " + deviceId + " shell wm size";
            String output = executeCommand(command);
            
            if (output.contains("Physical size:")) {
                return output.split(":")[1].trim();
            }
            
        } catch (Exception e) {
            logger.error("Failed to get screen resolution: " + e.getMessage());
        }
        
        return "Unknown";
    }
    
    /**
     * Get battery level
     * @param deviceId Device ID
     * @return Battery level percentage
     */
    public static String getBatteryLevel(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            List<String> devices = getConnectedDevices();
            if (!devices.isEmpty()) {
                deviceId = devices.getFirst();
            }
        }
        
        try {
            String command = "adb -s " + deviceId + " shell dumpsys battery | grep level";
            String output = executeCommand(command);
            
            if (output.contains("level:")) {
                return output.split(":")[1].trim() + "%";
            }
            
        } catch (Exception e) {
            logger.error("Failed to get battery level: " + e.getMessage());
        }
        
        return "Unknown";
    }
    
    /**
     * Print device information summary
     * @param deviceId Device ID
     */
    public static void printDeviceInfo(String deviceId) {
        Map<String, String> info = getDeviceInfo(deviceId);
        
        if (info.isEmpty()) {
            logger.warn("No device information available");
            return;
        }
        
        String resolution = getScreenResolution(deviceId);
        String battery = getBatteryLevel(deviceId);
        
        logger.info("\n" +
                "=".repeat(50) + "\n" +
                "DEVICE INFORMATION\n" +
                "=".repeat(50) + "\n" +
                "Device ID       : " + info.get("deviceId") + "\n" +
                "Device Type     : " + getDeviceType(deviceId) + "\n" +
                "Manufacturer    : " + info.get("manufacturer") + "\n" +
                "Brand           : " + info.get("brand") + "\n" +
                "Model           : " + info.get("model") + "\n" +
                "Device Name     : " + info.get("device") + "\n" +
                "Android Version : " + info.get("androidVersion") + "\n" +
                "SDK Version     : " + info.get("sdkVersion") + "\n" +
                "Build ID        : " + info.get("buildId") + "\n" +
                "Serial Number   : " + info.get("serialNumber") + "\n" +
                "Screen Resolution: " + resolution + "\n" +
                "Battery Level   : " + battery + "\n" +
                "=".repeat(50));
    }
    
    /**
     * Start ADB server
     */
    public static void startAdbServer() {
        logger.info("Starting ADB server...");
        executeCommand("adb start-server");
    }
    
    /**
     * Kill ADB server
     */
    public static void killAdbServer() {
        logger.info("Killing ADB server...");
        executeCommand("adb kill-server");
    }
    
    /**
     * Check if device is a real device or emulator
     * @param deviceId Device ID
     * @return true if real device, false if emulator
     */
    public static boolean isRealDevice(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            List<String> devices = getConnectedDevices();
            if (!devices.isEmpty()) {
                deviceId = devices.getFirst();
            } else {
                return false;
            }
        }
        
        try {
            // Emulators typically start with "emulator-"
            if (deviceId.startsWith("emulator-")) {
                return false;
            }
            
            // Check device characteristics
            String characteristics = getDeviceProperty(deviceId, "ro.build.characteristics");
            if (characteristics != null && characteristics.contains("emulator")) {
                return false;
            }
            
            // Check product name
            String product = getDeviceProperty(deviceId, "ro.product.name");
            if (product != null && (product.contains("sdk") || product.contains("emulator"))) {
                return false;
            }
            
            logger.info("Device " + deviceId + " detected as real device");
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to determine device type: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get device type string (Real Device or Emulator)
     * @param deviceId Device ID
     * @return "Real Device" or "Emulator"
     */
    public static String getDeviceType(String deviceId) {
        return isRealDevice(deviceId) ? "Real Device" : "Emulator";
    }
}
