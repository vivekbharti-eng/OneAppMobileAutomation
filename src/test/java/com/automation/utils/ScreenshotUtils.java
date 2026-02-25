package com.automation.utils;

import com.automation.drivers.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing screenshots.
 * Failed screenshots are saved to:
 *   test-output/screenshots/{runTimestamp}/{featureName}/{scenarioName}_{HHmmss}.png
 */
public class ScreenshotUtils {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);

    /**
     * Capture a failure screenshot and save it under the feature-specific sub-folder.
     *
     * @param scenarioName Name of the failing scenario (used as filename base)
     * @param featureName  Feature folder name, e.g. "login", "sendmoney", "logout"
     * @return Absolute path to the saved screenshot, or null on error
     */
    public static String captureFailureScreenshot(String scenarioName, String featureName) {
        try {
            // Sanitise names for use in file/folder paths
            String cleanScenario = scenarioName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String cleanFeature  = featureName.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();

            // Build path:  test-output/screenshots/{runTimestamp}/{feature}/
            String dir = ReportPathManager.getScreenshotDir(cleanFeature);

            // Unique filename with time-of-failure suffix
            String timeSuffix = new SimpleDateFormat("HHmmss").format(new Date());
            String fileName   = cleanScenario + "_" + timeSuffix + ".png";
            File   destFile   = new File(dir, fileName);

            // Take screenshot
            AppiumDriver driver = DriverManager.getDriver();
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, destFile);

            logger.info("Failure screenshot saved: " + destFile.getPath());
            return destFile.getPath();

        } catch (IOException e) {
            logger.error("Failed to save screenshot: " + e.getMessage());
            return null;
        }
    }

    /**
     * Capture screenshot and return as Base64 (for embedding in Extent Report).
     *
     * @return Base64-encoded PNG string, or null on error
     */
    public static String getBase64Screenshot() {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            logger.error("Failed to capture base64 screenshot: " + e.getMessage());
            return null;
        }
    }
}
