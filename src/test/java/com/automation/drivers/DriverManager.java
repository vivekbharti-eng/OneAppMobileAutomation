package com.automation.drivers;

import io.appium.java_client.AppiumDriver;

/**
 * Thread-safe driver manager using ThreadLocal
 * Ensures each thread gets its own driver instance for parallel execution
 */
public class DriverManager {
    
    private static ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    
    /**
     * Get driver instance for current thread
     * @return AppiumDriver instance
     */
    public static AppiumDriver getDriver() {
        return driver.get();
    }
    
    /**
     * Set driver instance for current thread
     * @param driverInstance AppiumDriver instance
     */
    public static void setDriver(AppiumDriver driverInstance) {
        driver.set(driverInstance);
    }
    
    /**
     * Quit driver and remove from thread
     */
    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}
