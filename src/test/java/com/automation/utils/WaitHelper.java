package com.automation.utils;

import com.automation.drivers.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Utility class for explicit waits
 * Provides various wait conditions for elements
 */
public class WaitHelper {
    
    private static final Logger logger = LogManager.getLogger(WaitHelper.class);
    private static final int DEFAULT_TIMEOUT = 20;
    
    /**
     * Wait for element to be visible
     * @param locator Element locator
     * @return WebElement
     */
    public static WebElement waitForElementToBeVisible(By locator) {
        return waitForElementToBeVisible(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for element to be visible with custom timeout
     * @param locator Element locator
     * @param timeoutInSeconds Timeout in seconds
     * @return WebElement
     */
    public static WebElement waitForElementToBeVisible(By locator, int timeoutInSeconds) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            logger.debug("Waiting for element to be visible: " + locator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            logger.error("Element not visible within " + timeoutInSeconds + " seconds: " + locator);
            throw e;
        }
    }
    
    /**
     * Wait for element to be clickable
     * @param locator Element locator
     * @return WebElement
     */
    public static WebElement waitForElementToBeClickable(By locator) {
        return waitForElementToBeClickable(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for element to be clickable with custom timeout
     * @param locator Element locator
     * @param timeoutInSeconds Timeout in seconds
     * @return WebElement
     */
    public static WebElement waitForElementToBeClickable(By locator, int timeoutInSeconds) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            logger.debug("Waiting for element to be clickable: " + locator);
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (Exception e) {
            logger.error("Element not clickable within " + timeoutInSeconds + " seconds: " + locator);
            throw e;
        }
    }
    
    /**
     * Wait for element to be present in DOM
     * @param locator Element locator
     * @return WebElement
     */
    public static WebElement waitForElementToBePresent(By locator) {
        return waitForElementToBePresent(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for element to be present in DOM with custom timeout
     * @param locator Element locator
     * @param timeoutInSeconds Timeout in seconds
     * @return WebElement
     */
    public static WebElement waitForElementToBePresent(By locator, int timeoutInSeconds) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            logger.debug("Waiting for element to be present: " + locator);
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            logger.error("Element not present within " + timeoutInSeconds + " seconds: " + locator);
            throw e;
        }
    }
    
    /**
     * Wait for element to be invisible
     * @param locator Element locator
     * @return boolean
     */
    public static boolean waitForElementToBeInvisible(By locator) {
        return waitForElementToBeInvisible(locator, DEFAULT_TIMEOUT);
    }
    
    /**
     * Wait for element to be invisible with custom timeout
     * @param locator Element locator
     * @param timeoutInSeconds Timeout in seconds
     * @return boolean
     */
    public static boolean waitForElementToBeInvisible(By locator, int timeoutInSeconds) {
        try {
            AppiumDriver driver = DriverManager.getDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            logger.debug("Waiting for element to be invisible: " + locator);
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (Exception e) {
            logger.error("Element still visible after " + timeoutInSeconds + " seconds: " + locator);
            throw e;
        }
    }
    
    /**
     * Hard wait (Thread.sleep) - use sparingly
     * @param milliseconds Time to wait in milliseconds
     */
    public static void hardWait(long milliseconds) {
        try {
            logger.debug("Hard wait for " + milliseconds + " milliseconds");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.error("Hard wait interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
