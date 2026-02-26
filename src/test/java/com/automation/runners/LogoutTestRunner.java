package com.automation.runners;

import com.automation.reports.ExtentReportManager;
import com.automation.reports.CucumberReportGenerator;
import com.automation.utils.ReportPathManager;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;

/**
 * Runner for Logout feature — executes THIRD (last) in the suite
 * Holds @AfterSuite to flush reports and perform suite cleanup
 */
@CucumberOptions(
        features = "src/test/resources/features/logout/Logout.feature",
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
public class LogoutTestRunner extends AbstractTestNGCucumberTests {

    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BOLD   = "\u001B[1m";

    @AfterSuite
    public void afterSuite() {
        ExtentReportManager.flushReport();
        CucumberReportGenerator.generateReport();
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
        System.out.println(ANSI_GREEN + ANSI_BOLD + "  TEST SUITE COMPLETED  " + ANSI_RESET);
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "Reports Generated:" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "  \u2713 Extent Report:   " + ANSI_RESET + ReportPathManager.getExtentReportFile());
        System.out.println(ANSI_GREEN + "  \u2713 Cucumber Report: " + ANSI_RESET + ReportPathManager.getCucumberReportDir() + "/cucumber-html-reports/overview-features.html");
        System.out.println(ANSI_GREEN + "  \u2713 Allure Results:  " + ANSI_RESET + ReportPathManager.getAllureResultsDir());
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "To view Allure report, run:" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "  allure serve " + ReportPathManager.getAllureResultsDir() + ANSI_RESET);
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
    }

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
