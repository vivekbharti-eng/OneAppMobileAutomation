package com.automation.hooks;

import com.automation.drivers.DriverFactory;
import com.automation.drivers.DriverManager;
import com.automation.reports.CucumberReportGenerator;
import com.automation.reports.EmailReporter;
import com.automation.reports.ExtentReportManager;
import com.automation.utils.AdbHelper;
import com.automation.utils.PropertyReader;
import com.automation.utils.ScreenshotUtils;
import com.automation.utils.SuiteState;
import io.appium.java_client.AppiumDriver;
import com.automation.utils.TestResultTracker;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.SkipException;

import java.nio.file.Path;
import java.util.Map;

/**
 * Cucumber hooks for setup and teardown
 * Manages driver lifecycle and screenshot on failure
 */
public class Hooks {
    
    private static final Logger logger = LogManager.getLogger(Hooks.class);

    // Tracks whether the current scenario already had a step failure logged
    private static final ThreadLocal<Boolean> stepFailureLogged = ThreadLocal.withInitial(() -> false);

    // Tracks the current feature file URI so step definitions can detect which flow is running
    public static final ThreadLocal<String> currentFeatureUri = new ThreadLocal<>();

    // ANSI Color Codes for Console Output (Standard Cucumber Colors)
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";  // Passed
    private static final String ANSI_RED = "\u001B[31m";    // Failed
    private static final String ANSI_YELLOW = "\u001B[33m"; // Warning
    private static final String ANSI_CYAN = "\u001B[36m";   // Info
    private static final String ANSI_BOLD = "\u001B[1m";
    
    /**
     * Before hook - executed before each scenario
     * Initializes driver and loads properties
     */
    @Before
    public void setUp(Scenario scenario) {
        // If a previous scenario failed and fail.fast=true, skip all remaining scenarios
        if (SuiteState.isAborted()) {
            String msg = "[FAIL FAST] Suite aborted — skipping: " + scenario.getName();
            logger.warn(msg);
            System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
            throw new SkipException(msg);
        }
        try {
            logger.info("========================================");
            logger.info("Starting scenario: " + scenario.getName());
            logger.info("========================================");
            System.out.println(ANSI_CYAN + "\n▶ Scenario: " + ANSI_BOLD + scenario.getName() + ANSI_RESET);

            // Store feature URI so steps can detect which feature is running
            currentFeatureUri.set(scenario.getUri().toString());
            
            // Load configuration properties
            PropertyReader.loadConfigProperties();
            
            // Load platform-specific locators
            String platform = PropertyReader.getPlatform();
            if (platform.equalsIgnoreCase("android")) {
                PropertyReader.loadAndroidLocators();
            } else if (platform.equalsIgnoreCase("ios")) {
                PropertyReader.loadIosLocators();
            }
            
            // Get and log device information for Android local execution
            String executionTarget = PropertyReader.getExecutionTarget();
            if (platform.equalsIgnoreCase("android") && !executionTarget.equals("browserstack")) {
                if (AdbHelper.isAdbAvailable()) {
                    Map<String, String> deviceInfo = AdbHelper.getDeviceInfo(null);
                    if (!deviceInfo.isEmpty()) {
                        // Add screen resolution and battery info
                        deviceInfo.put("resolution", AdbHelper.getScreenResolution(null));
                        deviceInfo.put("battery", AdbHelper.getBatteryLevel(null));
                        
                        // Add device type (Real Device or Emulator)
                        String deviceId = deviceInfo.get("deviceId");
                        deviceInfo.put("deviceType", AdbHelper.getDeviceType(deviceId));
                        
                        AdbHelper.printDeviceInfo(null);
                        // Add device info to extent report
                        ExtentReportManager.addDeviceInfo(deviceInfo);
                    }
                }
            }
            
            // Kill any stale UiAutomator2 server on device to ensure a clean session
            try {
                String executionTarget2 = PropertyReader.getExecutionTarget();
                String udidForKill;
                if ("virtual".equalsIgnoreCase(executionTarget2)) {
                    udidForKill = PropertyReader.getConfigProperty("android.emulator.udid");
                    if (udidForKill == null || udidForKill.isBlank()) udidForKill = "emulator-5554";
                } else {
                    udidForKill = PropertyReader.getConfigProperty("android.real.udid");
                    if (udidForKill == null || udidForKill.isBlank()) udidForKill = "10BF7S243X0030Z";
                }
                Runtime.getRuntime().exec(new String[]{"adb", "-s", udidForKill, "shell", "am", "force-stop", "io.appium.uiautomator2.server"}).waitFor();
                Runtime.getRuntime().exec(new String[]{"adb", "-s", udidForKill, "shell", "am", "force-stop", "io.appium.uiautomator2.server.test"}).waitFor();
                Thread.sleep(1000);
                logger.info("Killed stale UiAutomator2 server processes on device: " + udidForKill);
            } catch (Exception ignored) {
                logger.debug("UiAutomator2 server cleanup skipped: " + ignored.getMessage());
            }

            // Initialize driver
            AppiumDriver driver = DriverFactory.initializeDriver();
            DriverManager.setDriver(driver);
            
            // Start Extent Report test
            ExtentReportManager.startTest(scenario.getName());
            
            logger.info("Driver initialized successfully for platform: " + platform);
            stepFailureLogged.set(false); // reset per scenario

        } catch (Exception e) {
            logger.error("Failed to set up test: " + e.getMessage());
            throw new RuntimeException("Test setup failed", e);
        }
    }

    /**
     * AfterStep hook — captures the error message of the first failing step
     * and logs it into the Extent report so every step failure is visible.
     */
    @AfterStep
    public void captureStepError(Scenario scenario) {
        if (scenario.isFailed() && !Boolean.TRUE.equals(stepFailureLogged.get())) {
            stepFailureLogged.set(true);
            String errMsg = "Step failed";
            try {
                // Cucumber 7.18 does not expose getError() on io.cucumber.java.Scenario.
                // Extract via reflection on the underlying delegate object.
                java.lang.reflect.Method m = scenario.getClass().getMethod("getError");
                Object result = m.invoke(scenario);
                if (result instanceof Throwable t) {
                    String msg = t.getMessage();
                    if (msg != null && msg.length() > 500) {
                        msg = msg.substring(0, 500) + "...(truncated)";
                    }
                    errMsg = t.getClass().getSimpleName() + (msg != null ? ": " + msg : "");
                }
            } catch (NoSuchMethodException e1) {
                // Method not found — try to get info from driver page title
                try {
                    AppiumDriver drv = DriverManager.getDriver();
                    if (drv != null) {
                        errMsg = "Step failed — screenshot captured for diagnosis";
                    }
                } catch (Exception ignored2) {}
            } catch (Exception ignored) {}
            logger.error("Step failed — scenario: '{}' | error: {}", scenario.getName(), errMsg);
            ExtentReportManager.logFail("<b>Step Error:</b> " + errMsg);
        }
    }
    
    /**
     * After hook - executed after each scenario
     * Captures screenshot on failure and quits driver
     */
    @After
    public void tearDown(Scenario scenario) {
        try {
            logger.info("========================================");
            logger.info("Scenario status: " + scenario.getStatus());
            logger.info("========================================");
            
            // Display colored status in console
            if (scenario.isFailed()) {
                System.out.println(ANSI_RED + ANSI_BOLD + "✗ FAILED: " + scenario.getName() + ANSI_RESET);
            } else {
                System.out.println(ANSI_GREEN + ANSI_BOLD + "✓ PASSED: " + scenario.getName() + ANSI_RESET);
            }
            
            // Capture screenshot if scenario failed
            if (scenario.isFailed()) {
                logger.error("Scenario failed: " + scenario.getName());

                // ── FAIL FAST: signal abort FIRST (before any operations that may throw) ──
                String failFast = PropertyReader.getProperty("fail.fast", "false");
                if ("true".equalsIgnoreCase(failFast)) {
                    SuiteState.abort();
                    logger.error("FAIL FAST: scenario '{}' failed — aborting suite", scenario.getName());
                    System.out.println(ANSI_RED + ANSI_BOLD +
                        "[FAIL FAST] Suite stopped after first failure" + ANSI_RESET);
                }

                // Derive feature name from scenario URI  e.g. ".../features/login/Login.feature" → "login"
                String featureName = "unknown";
                try {
                    String uri = scenario.getUri().toString();
                    // grab the last directory segment before the .feature file
                    String[] parts = uri.replace("\\", "/").split("/");
                    if (parts.length >= 2) {
                        featureName = parts[parts.length - 2]; // parent folder = feature name
                    }
                } catch (Exception ex) {
                    logger.warn("Could not determine feature name from URI: " + ex.getMessage());
                }

                // Capture screenshot → test-output/screenshots/{runTimestamp}/{feature}/
                String screenshotPath = null;
                try {
                    screenshotPath = ScreenshotUtils.captureFailureScreenshot(scenario.getName(), featureName);
                } catch (Exception ex) {
                    logger.warn("Screenshot capture failed (driver may be dead): " + ex.getMessage());
                }

                // Attach screenshot to Cucumber report
                if (screenshotPath != null) {
                    try {
                        byte[] screenshot = java.nio.file.Files.readAllBytes(Path.of(screenshotPath));
                        scenario.attach(screenshot, "image/png", "Failure Screenshot");
                    } catch (Exception ex) {
                        logger.warn("Could not attach screenshot to Cucumber report: " + ex.getMessage());
                    }
                }

                // Add screenshot to Extent Report
                try {
                    String base64Screenshot = ScreenshotUtils.getBase64Screenshot();
                    if (base64Screenshot != null) {
                        ExtentReportManager.attachScreenshot(base64Screenshot);
                    }
                } catch (Exception ex) {
                    logger.warn("Could not attach base64 screenshot: " + ex.getMessage());
                }

                ExtentReportManager.logFail("<b>Scenario Failed:</b> " + scenario.getName());
                if (screenshotPath != null) {
                    ExtentReportManager.logFail("<b>Screenshot:</b> " + screenshotPath);
                    System.out.println(ANSI_RED + "✗ Screenshot: " + screenshotPath + ANSI_RESET);
                }

                // Capture page source for debugging
                try {
                    AppiumDriver driver = DriverManager.getDriver();
                    if (driver != null) {
                        String pageSource = driver.getPageSource();
                        logger.debug("Page source at failure:\n" + pageSource);
                    }
                } catch (Exception e) {
                    logger.warn("Could not capture page source: " + e.getMessage());
                }

                // Generate reports after abort (if fail-fast enabled)
                if ("true".equalsIgnoreCase(failFast)) {
                    // 1. Flush Extent Report to disk
                    try { ExtentReportManager.flushReport(); } catch (Exception ex) {
                        logger.warn("Extent report flush failed: " + ex.getMessage());
                    }

                    // 2. Generate Cucumber HTML report
                    try { CucumberReportGenerator.generateReport(); } catch (Exception ex) {
                        logger.warn("Cucumber report generation failed: " + ex.getMessage());
                    }

                    // 3. Send email
                    try {
                        int passed = TestResultTracker.getPassed();
                        int failed = TestResultTracker.getFailed();
                        EmailReporter.sendReport(ExtentReportManager.getReportPath(), passed, failed);
                    } catch (Exception ex) {
                        logger.warn("Email send failed: " + ex.getMessage());
                    }
                }
            } else {
                ExtentReportManager.logPass("Scenario passed: " + scenario.getName());
            }

            // Derive feature name from scenario URI for per-feature tracking
            String trackedFeature = "Unknown";
            try {
                String uri = scenario.getUri().toString().replace("\\", "/");
                String[] parts = uri.split("/");
                if (parts.length >= 2) {
                    // Use folder name (e.g. "login") capitalised as display name
                    String folder = parts[parts.length - 2];
                    trackedFeature = Character.toUpperCase(folder.charAt(0)) + folder.substring(1);
                }
            } catch (Exception ex) {
                logger.warn("Could not derive feature name for tracking: " + ex.getMessage());
            }

            // Track overall pass/fail counts for email summary
            if (scenario.isFailed()) {
                TestResultTracker.incrementFailed();
                TestResultTracker.trackFeatureResult(trackedFeature, false);
            } else {
                TestResultTracker.incrementPassed();
                TestResultTracker.trackFeatureResult(trackedFeature, true);
            }

            // Flush extent report
            ExtentReportManager.endTest();
            
        } catch (Exception e) {
            logger.error("Error during teardown: " + e.getMessage());
        } finally {
            // Quit driver
            DriverManager.quitDriver();
            logger.info("Driver quit successfully");
            // Brief cooldown so Appium cleans up the session before the next scenario starts
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        }
    }
}
