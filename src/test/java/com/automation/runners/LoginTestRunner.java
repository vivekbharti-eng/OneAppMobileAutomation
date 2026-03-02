package com.automation.runners;

import com.automation.reports.ExtentReportManager;
import com.automation.utils.PropertyReader;
import com.automation.utils.ReportPathManager;
import com.automation.utils.SuiteState;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.SkipException;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
        SuiteState.reset(); // clear any leftover abort flag from a previous run

        // ── Device readiness check ──────────────────────────────────────────
        String target = PropertyReader.getExecutionTarget();
        if ("virtual".equalsIgnoreCase(target)) {
            String udid = PropertyReader.getProperty("android.emulator.udid", "emulator-5554");
            ensureEmulatorReady(udid);
        } else if ("real".equalsIgnoreCase(target)) {
            String udid = PropertyReader.getProperty("android.real.udid", "");
            if (!udid.isEmpty()) {
                boolean found = isDeviceConnected(udid);
                if (!found) {
                    throw new SkipException("[DEVICE CHECK] Real device '" + udid + "' not connected via ADB. Connect the device and retry.");
                }
                logger.info("[DEVICE CHECK] Real device '{}' is connected.", udid);
            }
        }
        // ────────────────────────────────────────────────────────────────────

        ReportPathManager.initializeReportPaths();
        ExtentReportManager.initReport();
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);
        System.out.println(ANSI_GREEN + ANSI_BOLD + "  MOBILE AUTOMATION TEST SUITE STARTED  " + ANSI_RESET);
        System.out.println(ANSI_CYAN + ANSI_BOLD + "=========================================" + ANSI_RESET);

        // Uninstall app once at suite start so Login flow does a fresh install.
        // SendMoney and Logout then reuse the installed app (noReset=true keeps state).
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

    // ── Helpers ─────────────────────────────────────────────────────────────

    /** Returns true if the given UDID appears in 'adb devices' output. */
    private boolean isDeviceConnected(String udid) {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"adb", "devices"});
            try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(udid) && line.contains("device")) {
                        return true;
                    }
                }
            }
            proc.waitFor();
        } catch (Exception e) {
            logger.warn("[DEVICE CHECK] adb devices check failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Waits up to 90 seconds for the emulator to appear in 'adb devices'.
     * If not found after waiting, attempts to launch it via 'emulator -avd <name>'.
     * Throws SkipException (stops suite) if device is still absent after all retries.
     */
    private void ensureEmulatorReady(String udid) {
        final int waitSeconds = 90;
        final int pollInterval = 5;

        // First quick check
        if (isDeviceConnected(udid)) {
            logger.info("[DEVICE CHECK] Emulator '{}' is already running.", udid);
            return;
        }

        // Try launching the emulator
        String avdName = PropertyReader.getProperty("android.emulator.deviceName", "Pixel_8_API_35");
        logger.warn("[DEVICE CHECK] Emulator '{}' not found — attempting to launch AVD '{}'...", udid, avdName);
        System.out.println(ANSI_CYAN + "[Suite Setup] Emulator not running — starting AVD '" + avdName + "'..." + ANSI_RESET);
        try {
            // Start emulator in background
            new ProcessBuilder("emulator", "-avd", avdName, "-no-snapshot-load")
                    .redirectErrorStream(true)
                    .start();
        } catch (Exception e) {
            logger.warn("[DEVICE CHECK] Could not launch emulator: {}", e.getMessage());
        }

        // Wait for device to appear
        for (int elapsed = 0; elapsed < waitSeconds; elapsed += pollInterval) {
            try { Thread.sleep(pollInterval * 1000L); } catch (InterruptedException ignored) {}
            if (isDeviceConnected(udid)) {
                // Wait a few more seconds for ADB to be fully ready
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                logger.info("[DEVICE CHECK] Emulator '{}' is now ready.", udid);
                System.out.println(ANSI_GREEN + "[Suite Setup] Emulator '" + udid + "' is ready." + ANSI_RESET);
                return;
            }
            logger.info("[DEVICE CHECK] Waiting for emulator '{}' ... {}s elapsed", udid, elapsed + pollInterval);
        }

        // Still not found after waiting
        throw new SkipException("[DEVICE CHECK] Emulator '" + udid + "' not available after " + waitSeconds + "s. Start the emulator and retry.");
    }

    // ────────────────────────────────────────────────────────────────────────

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
