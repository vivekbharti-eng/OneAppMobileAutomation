package com.automation.runners;

import com.automation.reports.ExtentReportManager;
import com.automation.utils.PropertyReader;
import com.automation.utils.ReportPathManager;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

/**
 * Runner for Login feature — executes FIRST in the suite
 * Holds @BeforeSuite to initialise reports before any test runs
 */
@CucumberOptions(
        features = "src/test/resources/features/login/Login.feature",
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
public class LoginTestRunner extends AbstractTestNGCucumberTests {

    private static final Logger logger = LogManager.getLogger(LoginTestRunner.class);
    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_BOLD   = "\u001B[1m";

    @BeforeSuite
    public void beforeSuite() {
        ReportPathManager.initializeReportPaths();
        ExtentReportManager.initReport();
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
        System.out.println(ANSI_GREEN + ANSI_BOLD + "  MOBILE AUTOMATION TEST SUITE STARTED  " + ANSI_RESET);
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);

        // Uninstall app once at suite start so Login flow does a fresh install.
        // SendMoney and Logout then reuse the installed app (noReset=true keeps state).
        String target = PropertyReader.getExecutionTarget();
        if (!"browserstack".equalsIgnoreCase(target)) {
            String appPackage = PropertyReader.getProperty("android.appPackage", "com.sasai.sasaipay");
            String udid = "virtual".equalsIgnoreCase(target)
                    ? PropertyReader.getProperty("android.emulator.udid", "emulator-5554")
                    : PropertyReader.getProperty("android.real.udid", "");
            try {
                String[] cmd = udid.isEmpty()
                        ? new String[]{"adb", "uninstall", appPackage}
                        : new String[]{"adb", "-s", udid, "uninstall", appPackage};
                Process proc = Runtime.getRuntime().exec(cmd);
                int exit = proc.waitFor();
                logger.info("[BeforeSuite] ADB uninstall '{}' on '{}' → exit {}", appPackage, udid, exit);
                System.out.println(ANSI_CYAN + "[Suite Setup] App uninstalled for fresh install → " + appPackage + ANSI_RESET);
            } catch (Exception e) {
                logger.warn("[BeforeSuite] ADB uninstall skipped: " + e.getMessage());
            }
        }
    }

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
