package com.automation.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe tracker for overall and per-feature test pass/fail counts.
 * Incremented by Hooks after each scenario; read by TestSuiteListener to send email.
 */
public class TestResultTracker {

    private static final AtomicInteger passed = new AtomicInteger(0);
    private static final AtomicInteger failed = new AtomicInteger(0);

    // featureName -> [passed, failed]
    private static final Map<String, int[]> featureStats = new LinkedHashMap<>();
    private static final Object featureLock = new Object();

    private TestResultTracker() {}

    public static void incrementPassed() { passed.incrementAndGet(); }

    public static void incrementFailed() { failed.incrementAndGet(); }

    public static int getPassed()  { return passed.get(); }
    public static int getFailed()  { return failed.get(); }
    public static int getTotal()   { return passed.get() + failed.get(); }

    /**
     * Track a scenario result under its feature name.
     * @param featureName display name of the feature (e.g. "Login", "SendMoney")
     * @param scenarioPassed true if the scenario passed
     */
    public static void trackFeatureResult(String featureName, boolean scenarioPassed) {
        synchronized (featureLock) {
            int[] counts = featureStats.computeIfAbsent(featureName, k -> new int[]{0, 0});
            if (scenarioPassed) { counts[0]++; } else { counts[1]++; }
        }
    }

    /** @return a snapshot of per-feature stats: featureName -> [passed, failed] */
    public static Map<String, int[]> getFeatureStats() {
        synchronized (featureLock) {
            return new LinkedHashMap<>(featureStats);
        }
    }

    /** Reset all counters (call from suite start if needed) */
    public static void reset() {
        passed.set(0);
        failed.set(0);
        synchronized (featureLock) { featureStats.clear(); }
    }
}
