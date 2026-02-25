package com.automation.runners;

import com.automation.reports.ExtentReportManager;import com.automation.utils.ReportPathManager;import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

/**
 * TestNG Cucumber runner class
 * Configures Cucumber options and enables parallel execution
 */
@CucumberOptions(
        features = {
                "src/test/resources/features/login",      // 1. Login
                "src/test/resources/features/sendmoney",  // 2. SendMoney
                "src/test/resources/features/logout"      // 3. Logout
        },
        glue = "com.automation",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "junit:target/cucumber-reports/cucumber.xml",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = false,
        dryRun = false,
        tags = "@smoke and @positive"
)
public class TestRunner extends AbstractTestNGCucumberTests {
    
    // ANSI Color Codes for Console Output (Standard Cucumber Colors)
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";  // Passed
    private static final String ANSI_RED = "\u001B[31m";    // Failed
    private static final String ANSI_YELLOW = "\u001B[33m"; // Warning/Skipped
    private static final String ANSI_CYAN = "\u001B[36m";   // Info
    private static final String ANSI_BOLD = "\u001B[1m";
    
    /**
     * Enable parallel execution
     * Override this method to run scenarios in parallel
     * Set parallel to false for stable execution
     */
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
    
    /**
     * Before suite - initialize extent report
     */
    @BeforeSuite
    public void beforeSuite() {
        // Initialize report paths with timestamp
        ReportPathManager.initializeReportPaths();
        
        // Initialize Extent Report
        ExtentReportManager.initReport();
        
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
        System.out.println(ANSI_GREEN + ANSI_BOLD + "  MOBILE AUTOMATION TEST SUITE STARTED  " + ANSI_RESET);
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
    }
    
    /**
     * After suite - perform logout and flush extent report
     */
    @AfterSuite
    public void afterSuite() {
        try {
            // Attempt to logout if driver is still active
            com.automation.drivers.DriverManager driverManager = new com.automation.drivers.DriverManager();
            io.appium.java_client.AppiumDriver driver = com.automation.drivers.DriverManager.getDriver();
            
            if (driver != null) {
                System.out.println(ANSI_YELLOW + ANSI_BOLD + "=========================================" + ANSI_RESET);
                System.out.println(ANSI_YELLOW + ANSI_BOLD + "  PERFORMING SUITE CLEANUP - LOGOUT  " + ANSI_RESET);
                System.out.println(ANSI_YELLOW + ANSI_BOLD + "=========================================" + ANSI_RESET);
                
                try {
                    com.automation.pages.HomePage homePage = new com.automation.pages.HomePage();
                    homePage.performLogout();
                    System.out.println(ANSI_GREEN + "✓ Logout completed successfully" + ANSI_RESET);
                } catch (Exception e) {
                    System.out.println(ANSI_YELLOW + "⚠ Note: Logout during cleanup failed (this is expected if already logged out): " + e.getMessage() + ANSI_RESET);
                }
            }
        } catch (Exception e) {
            System.out.println(ANSI_YELLOW + "⚠ Suite cleanup: " + e.getMessage() + ANSI_RESET);
        }
        
        ExtentReportManager.flushReport();

        // Generate Masterthought Cucumber HTML report
        com.automation.reports.CucumberReportGenerator.generateReport();

        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
        System.out.println(ANSI_GREEN + ANSI_BOLD + "  TEST SUITE COMPLETED  " + ANSI_RESET);
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "Reports Generated:" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "  ✓ Extent Report:   " + ANSI_RESET + ReportPathManager.getExtentReportFile());
        System.out.println(ANSI_GREEN + "  ✓ Cucumber Report: " + ANSI_RESET + ReportPathManager.getCucumberReportDir() + "/cucumber-html-reports/overview-features.html");
        System.out.println(ANSI_GREEN + "  ✓ Allure Results:  " + ANSI_RESET + ReportPathManager.getAllureResultsDir());
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "To view Allure report, run:" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "  allure serve " + ReportPathManager.getAllureResultsDir() + ANSI_RESET);
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
    }
}
