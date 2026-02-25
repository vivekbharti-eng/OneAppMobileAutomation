package com.automation.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe tracker for overall test pass/fail counts across all scenarios.
 * Incremented by Hooks after each scenario; read by TestSuiteListener to send email.
 */
public class TestResultTracker {

    private static final AtomicInteger passed = new AtomicInteger(0);
    private static final AtomicInteger failed = new AtomicInteger(0);

    private TestResultTracker() {}

    public static void incrementPassed() { passed.incrementAndGet(); }

    public static void incrementFailed() { failed.incrementAndGet(); }

    public static int getPassed()  { return passed.get(); }
    public static int getFailed()  { return failed.get(); }
    public static int getTotal()   { return passed.get() + failed.get(); }

    /** Reset counters (call from suite start if needed) */
    public static void reset() { passed.set(0); failed.set(0); }
}
