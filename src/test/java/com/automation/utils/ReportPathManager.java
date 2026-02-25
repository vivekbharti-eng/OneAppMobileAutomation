package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Report Path Manager
 * Manages report directory paths with timestamps for Allure and Extent reports
 */
public class ReportPathManager {
    
    private static final Logger logger = LogManager.getLogger(ReportPathManager.class);
    private static String timestamp;
    private static String baseReportDir = "target/reports";
    
    // Report directories
    private static String allureResultsDir;
    private static String allureReportDir;
    private static String extentReportDir;
    private static String extentReportFile;
    private static String cucumberReportDir;
    private static String screenshotBaseDir;
    
    /**
     * Initialize report paths with timestamp
     */
    public static void initializeReportPaths() {
        // Generate timestamp once per test run
        timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        
        // Create base report directory structure
        allureResultsDir = baseReportDir + "/allure/" + timestamp + "/allure-results";
        allureReportDir = baseReportDir + "/allure/" + timestamp + "/allure-report";
        extentReportDir = baseReportDir + "/extent/" + timestamp;
        extentReportFile = extentReportDir + "/ExtentReport_" + timestamp + ".html";
        cucumberReportDir = "test-output/cucumber-reports/" + timestamp;
        screenshotBaseDir  = "test-output/screenshots/" + timestamp;

        // Create directories
        createDirectory(allureResultsDir);
        createDirectory(extentReportDir);
        createDirectory(cucumberReportDir);
        createDirectory(screenshotBaseDir);
        
        // Update allure.properties dynamically
        updateAllureProperties();
        
        logger.info("Report paths initialized with timestamp: " + timestamp);
        logger.info("Allure Results: " + allureResultsDir);
        logger.info("Allure Report: " + allureReportDir);
        logger.info("Extent Report: " + extentReportFile);
    }
    
    /**
     * Create directory if it doesn't exist
     * @param dirPath Directory path
     */
    private static void createDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                logger.info("Created directory: " + dirPath);
            } else {
                logger.error("Failed to create directory: " + dirPath);
            }
        }
    }
    
    /**
     * Update allure.properties file with new results directory
     */
    private static void updateAllureProperties() {
        try {
            String allurePropertiesPath = "src/test/resources/allure.properties";
            FileWriter writer = new FileWriter(allurePropertiesPath);
            writer.write("# Allure Report Configuration - Auto-generated\n");
            writer.write("allure.results.directory=" + allureResultsDir + "\n");
            writer.write("allure.link.issue.pattern=https://github.com/your-org/your-repo/issues/{}\n");
            writer.write("allure.link.tms.pattern=https://github.com/your-org/your-repo/issues/{}\n");
            writer.close();
            
            // Also set system property for runtime
            System.setProperty("allure.results.directory", allureResultsDir);
            
            logger.info("Updated allure.properties with results directory: " + allureResultsDir);
        } catch (IOException e) {
            logger.error("Failed to update allure.properties: " + e.getMessage());
        }
    }
    
    /**
     * Get Allure results directory path
     * @return Allure results directory path
     */
    public static String getAllureResultsDir() {
        if (allureResultsDir == null) {
            initializeReportPaths();
        }
        return allureResultsDir;
    }
    
    /**
     * Get Allure report directory path
     * @return Allure report directory path
     */
    public static String getAllureReportDir() {
        if (allureReportDir == null) {
            initializeReportPaths();
        }
        return allureReportDir;
    }
    
    /**
     * Get Extent report directory path
     * @return Extent report directory path
     */
    public static String getExtentReportDir() {
        if (extentReportDir == null) {
            initializeReportPaths();
        }
        return extentReportDir;
    }
    
    /**
     * Get Extent report file path
     * @return Extent report file path
     */
    public static String getExtentReportFile() {
        if (extentReportFile == null) {
            initializeReportPaths();
        }
        return extentReportFile;
    }

    /**
     * Get Cucumber HTML report directory path (test-output/cucumber-reports/{timestamp})
     * @return Cucumber report directory path
     */
    public static String getCucumberReportDir() {
        if (cucumberReportDir == null) {
            initializeReportPaths();
        }
        return cucumberReportDir;
    }

    /**
     * Get screenshot directory for a specific feature within this run.
     * Path: test-output/screenshots/{timestamp}/{featureName}/
     * @param featureName e.g. "login", "sendmoney", "logout"
     * @return Directory path string
     */
    public static String getScreenshotDir(String featureName) {
        if (screenshotBaseDir == null) {
            initializeReportPaths();
        }
        String dir = screenshotBaseDir + "/" + featureName;
        createDirectory(dir);
        return dir;
    }
    
    /**
     * Get current timestamp
     * @return Timestamp string
     */
    public static String getTimestamp() {
        if (timestamp == null) {
            initializeReportPaths();
        }
        return timestamp;
    }
    
    /**
     * Get base report directory
     * @return Base report directory path
     */
    public static String getBaseReportDir() {
        return baseReportDir;
    }
}
