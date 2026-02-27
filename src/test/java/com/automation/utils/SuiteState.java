package com.automation.utils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shared suite-level state.
 * Used to propagate fail-fast flag across TestNG test classes (runners)
 * without relying on System.exit() which may be ignored by surefire.
 */
public class SuiteState {

    private static final AtomicBoolean suiteAborted = new AtomicBoolean(false);

    /** Mark the suite as aborted (called on first failure when fail.fast=true). */
    public static void abort() {
        suiteAborted.set(true);
    }

    /** @return true if the suite has been aborted due to a failure. */
    public static boolean isAborted() {
        return suiteAborted.get();
    }

    /** Reset — called at suite start. */
    public static void reset() {
        suiteAborted.set(false);
    }
}
