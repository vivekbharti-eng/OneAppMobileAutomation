package com.automation.listeners;

import com.automation.reports.EmailReporter;
import com.automation.reports.ExtentReportManager;
import com.automation.utils.AppiumServerManager;
import com.automation.utils.TestResultTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * TestNG Suite listener — flushes Extent Report and sends email when the full suite finishes.
 * Register in testng.xml:
 *   &lt;listeners&gt;&lt;listener class-name="com.automation.listeners.TestSuiteListener"/&gt;&lt;/listeners&gt;
 */
public class TestSuiteListener implements ISuiteListener {

    private static final Logger logger = LogManager.getLogger(TestSuiteListener.class);

    @Override
    public void onStart(ISuite suite) {
        TestResultTracker.reset();
        logger.info("Suite started: " + suite.getName());

        // Auto-detect real device via ADB and start Appium server
        AppiumServerManager.start();
    }

    @Override
    public void onFinish(ISuite suite) {
        logger.info("Suite finished: " + suite.getName());

        // Flush the Extent Report so the HTML is written to disk
        ExtentReportManager.flushReport();

        int passed = TestResultTracker.getPassed();
        int failed = TestResultTracker.getFailed();

        logger.info("Suite Summary — Passed: %d | Failed: %d | Total: %d".formatted(
            passed, failed, TestResultTracker.getTotal()));

        // Send email with the report attached
        String reportPath = ExtentReportManager.getReportPath();
        EmailReporter.sendReport(reportPath, passed, failed);

        // Stop Appium server if it was started by this run
        AppiumServerManager.stop();
    }
}
