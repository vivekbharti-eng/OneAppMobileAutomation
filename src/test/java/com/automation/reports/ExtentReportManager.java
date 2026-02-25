package com.automation.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.automation.utils.PropertyReader;
import com.automation.utils.ReportPathManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Extent Report Manager
 * Manages ExtentReports lifecycle and test reporting
 */
public class ExtentReportManager {
    
    private static final Logger logger = LogManager.getLogger(ExtentReportManager.class);
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static String reportPath;
    
    /**
     * Initialize Extent Report
     */
    public static void initReport() {
        if (extent == null) {
            // Get report path from ReportPathManager
            reportPath = ReportPathManager.getExtentReportFile();
            logger.info("Extent Report Directory: " + ReportPathManager.getExtentReportDir());
            
            // Initialize ExtentSparkReporter
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            
            // Configure report
            sparkReporter.config().setDocumentTitle("Appium Automation Report");
            sparkReporter.config().setReportName("Mobile Automation Test Results");
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
            
            // Initialize ExtentReports
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            
            // Set system information
            extent.setSystemInfo("Platform", PropertyReader.getPlatform());
            extent.setSystemInfo("Execution Type", PropertyReader.getConfigProperty("execution.type"));
            extent.setSystemInfo("Environment", "QA");
            extent.setSystemInfo("Tester", System.getProperty("user.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            
            logger.info("Extent Report initialized: " + reportPath);
        }
    }
    
    /**
     * Start a new test
     * @param testName Test name
     */
    public static void startTest(String testName) {
        ExtentTest extentTest = extent.createTest(testName);
        test.set(extentTest);
        logger.info("Started test: " + testName);
    }
    
    /**
     * End current test
     */
    public static void endTest() {
        test.remove();
    }
    
    /**
     * Log info message
     * @param message Message to log
     */
    public static void logInfo(String message) {
        if (test.get() != null) {
            // Standard Cucumber color: Cyan for info
            test.get().log(Status.INFO, "<span style='color:#17a2b8;'>ℹ</span> " + message);
        }
    }
    
    /**
     * Log pass message
     * @param message Message to log
     */
    public static void logPass(String message) {
        if (test.get() != null) {
            // Standard Cucumber color: Green for passed
            test.get().log(Status.PASS, "<span style='color:#28a745; font-weight:bold;'>✓</span> " + message);
        }
    }
    
    /**
     * Log fail message
     * @param message Message to log
     */
    public static void logFail(String message) {
        if (test.get() != null) {
            // Standard Cucumber color: Red for failed
            test.get().log(Status.FAIL, "<span style='color:#dc3545; font-weight:bold;'>✗</span> " + message);
        }
    }
    
    /**
     * Log warning message
     * @param message Message to log
     */
    public static void logWarning(String message) {
        if (test.get() != null) {
            // Standard Cucumber color: Orange/Yellow for warning
            test.get().log(Status.WARNING, "<span style='color:#ffc107; font-weight:bold;'>⚠</span> " + message);
        }
    }
    
    /**
     * Log skip message
     * @param message Message to log
     */
    public static void logSkip(String message) {
        if (test.get() != null) {
            // Standard Cucumber color: Gray for skipped
            test.get().log(Status.SKIP, "<span style='color:#6c757d;'>○</span> " + message);
        }
    }
    
    /**
     * Attach screenshot to report
     * @param base64Screenshot Base64 encoded screenshot
     */
    public static void attachScreenshot(String base64Screenshot) {
        if (test.get() != null && base64Screenshot != null) {
            try {
                test.get().fail("Screenshot on failure",
                        MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
            } catch (Exception e) {
                logger.error("Failed to attach screenshot to report: " + e.getMessage());
            }
        }
    }
    
    /**
     * Flush report
     */
    public static void flushReport() {
        if (extent != null) {
            extent.flush();
            logger.info("Extent Report flushed: " + reportPath);
        }
    }

    /**
     * Returns the absolute path to the generated Extent HTML report file.
     * Used by EmailReporter to attach the report.
     */
    public static String getReportPath() {
        return reportPath;
    }
    
    /**
     * Get current test instance
     * @return ExtentTest instance
     */
    public static ExtentTest getTest() {
        return test.get();
    }
    
    /**
     * Add device information to report
     * @param deviceInfo Map of device information
     */
    public static void addDeviceInfo(Map<String, String> deviceInfo) {
        if (extent != null && deviceInfo != null && !deviceInfo.isEmpty()) {
            extent.setSystemInfo("Device Type", deviceInfo.getOrDefault("deviceType", "N/A"));
            extent.setSystemInfo("Device ID", deviceInfo.getOrDefault("deviceId", "N/A"));
            extent.setSystemInfo("Device Manufacturer", deviceInfo.getOrDefault("manufacturer", "N/A"));
            extent.setSystemInfo("Device Model", deviceInfo.getOrDefault("model", "N/A"));
            extent.setSystemInfo("Android Version", deviceInfo.getOrDefault("androidVersion", "N/A"));
            extent.setSystemInfo("SDK Version", deviceInfo.getOrDefault("sdkVersion", "N/A"));
            extent.setSystemInfo("Screen Resolution", deviceInfo.getOrDefault("resolution", "N/A"));
            extent.setSystemInfo("Battery Level", deviceInfo.getOrDefault("battery", "N/A"));
            logger.info("Device information added to extent report");
        }
    }
}
