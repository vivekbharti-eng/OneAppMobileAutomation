package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages the Appium server lifecycle for the test suite.
 * <ul>
 *   <li>Starts Appium automatically before tests if not already running.</li>
 *   <li>Auto-detects the first real Android device via ADB and sets its UDID
 *       + platform version in PropertyReader so DriverFactory picks it up.</li>
 *   <li>Stops the server after the suite finishes (only if it was started by this class).</li>
 * </ul>
 */
public class AppiumServerManager {

    private static final Logger logger = LogManager.getLogger(AppiumServerManager.class);

    private static final String ANSI_RESET  = "\u001B[0m";
    private static final String ANSI_GREEN  = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN   = "\u001B[36m";
    private static final String ANSI_BOLD   = "\u001B[1m";

    /** The OS process running Appium (null if an external server was already running). */
    private static Process appiumProcess = null;

    private AppiumServerManager() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Ensures the Appium server is running and auto-configures the real device.
     * Call this from {@code TestSuiteListener.onStart()}.
     */
    public static void start() {
        autoDetectAndConfigureDevice();
        ensureAppiumRunning();
    }

    /**
     * Stops the Appium server if it was started by this class.
     * Call this from {@code TestSuiteListener.onFinish()}.
     */
    public static void stop() {
        if (appiumProcess != null && appiumProcess.isAlive()) {
            appiumProcess.destroyForcibly();
            appiumProcess = null;
            System.out.println(ANSI_YELLOW + "⏹  Appium server stopped." + ANSI_RESET);
            logger.info("Appium server stopped by AppiumServerManager.");
        }
    }

    // -------------------------------------------------------------------------
    // Device auto-detection
    // -------------------------------------------------------------------------

    /**
     * Finds the first real (non-emulator) device connected via ADB and sets
     * the following System properties so DriverFactory uses them at runtime:
     * <ul>
     *   <li>{@code android.udid}            — device serial / UDID</li>
     *   <li>{@code android.platformVersion} — Android OS version</li>
     *   <li>{@code android.deviceName}      — device model name</li>
     *   <li>{@code android.deviceType}      — always "real"</li>
     * </ul>
     */
    private static void autoDetectAndConfigureDevice() {
        if (!AdbHelper.isAdbAvailable()) {
            logger.warn("ADB not available — cannot auto-detect device. Using config.properties values.");
            return;
        }

        List<String> devices = AdbHelper.getConnectedDevices();
        if (devices.isEmpty()) {
            System.out.println(ANSI_YELLOW
                    + "⚠  No ADB devices found. Ensure the device is connected and USB debugging is enabled."
                    + ANSI_RESET);
            logger.warn("No ADB devices found. Will proceed with config.properties UDID.");
            return;
        }

        // Prefer a real device (serial doesn't start with "emulator-")
        String udid = devices.stream()
                .filter(d -> !d.startsWith("emulator-"))
                .findFirst()
                .orElse(devices.getFirst());

        // Query device properties
        String androidVersion = getAdbProp(udid, "ro.build.version.release");
        String model          = getAdbProp(udid, "ro.product.model");
        if (model.isBlank()) { model = getAdbProp(udid, "ro.product.name"); }

        // Override runtime config so DriverFactory picks them up
        System.setProperty("android.udid",            udid);
        System.setProperty("android.platformVersion",  androidVersion.isBlank() ? "14.0" : androidVersion);
        System.setProperty("android.deviceName",       model.isBlank() ? "RealDevice" : model);
        System.setProperty("android.deviceType",       "real");

        // Also push into PropertyReader cache so getConfigProperty() returns the right values
        PropertyReader.overrideConfigProperty("android.udid",            System.getProperty("android.udid"));
        PropertyReader.overrideConfigProperty("android.platformVersion",  System.getProperty("android.platformVersion"));
        PropertyReader.overrideConfigProperty("android.deviceName",       System.getProperty("android.deviceName"));
        PropertyReader.overrideConfigProperty("android.deviceType",       "real");

        System.out.println(ANSI_GREEN + ANSI_BOLD + "📱 Real device detected:" + ANSI_RESET
                + ANSI_CYAN + "  " + model + "  |  UDID: " + udid
                + "  |  Android " + androidVersion + ANSI_RESET);
        logger.info("Auto-detected device — UDID: {} | Model: {} | Android: {}", udid, model, androidVersion);
    }

    private static String getAdbProp(String udid, String prop) {
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "-s", udid, "shell", "getprop", prop);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String result = new BufferedReader(new InputStreamReader(p.getInputStream()))
                    .lines()
                    .findFirst()
                    .orElse("")
                    .trim();
            p.waitFor(5, TimeUnit.SECONDS);
            return result;
        } catch (Exception e) {
            logger.warn("getprop {} failed: {}", prop, e.getMessage());
            return "";
        }
    }

    // -------------------------------------------------------------------------
    // Appium server lifecycle
    // -------------------------------------------------------------------------

    /**
     * If Appium is already reachable on 127.0.0.1:4723, does nothing.
     * Otherwise starts a new Appium process and waits up to 30 s for it to be ready.
     */
    private static void ensureAppiumRunning() {
        if (isAppiumReachable()) {
            System.out.println(ANSI_GREEN + "✓ Appium server already running on port 4723." + ANSI_RESET);
            logger.info("Appium already running — skipping start.");
            return;
        }

        System.out.println(ANSI_CYAN + ANSI_BOLD + "🚀 Starting Appium server..." + ANSI_RESET);
        logger.info("Appium not running — starting server.");

        try {
            // Detect appium executable (Windows: appium.cmd / appium; Unix: appium)
            String appiumCmd = System.getProperty("os.name").toLowerCase().contains("win")
                    ? "appium.cmd" : "appium";

            ProcessBuilder pb = new ProcessBuilder(
                    appiumCmd,
                    "--address", "127.0.0.1",
                    "--port", "4723",
                    "--log-level", "warn",
                    "--relaxed-security"
            );
            pb.redirectErrorStream(true);
            appiumProcess = pb.start();

            // Stream Appium output to a background thread so it doesn't block
            Thread logThread = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(appiumProcess.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        logger.debug("[Appium] {}", line);
                    }
                } catch (Exception ignored) {}
            }, "appium-log");
            logThread.setDaemon(true);
            logThread.start();

            // Wait up to 30 s for Appium to become reachable
            long deadline = System.currentTimeMillis() + 30_000;
            while (System.currentTimeMillis() < deadline) {
                if (isAppiumReachable()) {
                    System.out.println(ANSI_GREEN + ANSI_BOLD
                            + "✓ Appium server started successfully on http://127.0.0.1:4723"
                            + ANSI_RESET);
                    logger.info("Appium server is ready.");
                    return;
                }
                Thread.sleep(1000);
            }

            // Timed out
            logger.error("Appium server did not become ready within 30 seconds.");
            System.out.println(ANSI_YELLOW
                    + "⚠  Appium server did not respond within 30 s — tests may fail."
                    + ANSI_RESET);

        } catch (Exception e) {
            logger.error("Failed to start Appium server: {}", e.getMessage(), e);
            System.out.println(ANSI_YELLOW
                    + "⚠  Could not start Appium: " + e.getMessage()
                    + " — make sure 'appium' is on PATH." + ANSI_RESET);
        }
    }

    /** Returns true if Appium's /status endpoint returns HTTP 200. */
    private static boolean isAppiumReachable() {
        try {
            HttpURLConnection conn = (HttpURLConnection)
                    URI.create("http://127.0.0.1:4723/status").toURL().openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
