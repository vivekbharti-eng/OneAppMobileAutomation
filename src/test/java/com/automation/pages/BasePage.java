package com.automation.pages;

import com.automation.drivers.DriverManager;
import com.automation.utils.WaitHelper;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * Base page class with common methods
 * All page objects should extend this class
 */
public class BasePage {
    
    protected static final Logger logger = LogManager.getLogger(BasePage.class);
    protected AppiumDriver driver;
    
    /**
     * Constructor to initialize driver
     */
    public BasePage() {
        this.driver = DriverManager.getDriver();
    }
    
    /**
     * Click on element
     * @param locator Element locator
     */
    protected void click(By locator) {
        try {
            WebElement element = WaitHelper.waitForElementToBeClickable(locator);
            element.click();
            logger.info("Clicked on element: " + locator);
        } catch (Exception e) {
            logger.error("Failed to click on element: " + locator);
            throw e;
        }
    }
    
    /**
     * Send keys to element with clearing
     * @param locator Element locator
     * @param text Text to send
     */
    protected void sendKeys(By locator, String text) {
        try {
            WebElement element = WaitHelper.waitForElementToBeVisible(locator);
            
            // Get current text to log it
            String currentText = "";
            try {
                currentText = element.getText();
                if (currentText != null && !currentText.isEmpty()) {
                    logger.info("Field currently contains: '" + currentText + "'");
                }
            } catch (Exception e) {
                logger.debug("Could not get current text");
            }
            
            // Click element first to ensure it has focus
            element.click();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Select all text and delete using BACKSPACE (more reliable on mobile)
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            element.sendKeys(Keys.BACK_SPACE);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Try standard clear as well
            try {
                element.clear();
                Thread.sleep(100);
            } catch (Exception e) {
                logger.debug("Clear() method did not work, continuing...");
            }
            
            // One more clear attempt with DELETE key
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            element.sendKeys(Keys.DELETE);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Now send the actual text
            element.sendKeys(text);
            logger.info("Entered text '" + text + "' in element: " + locator);
            
            // Verify what was actually entered
            try {
                Thread.sleep(300);
                String finalText = element.getText();
                if (finalText != null && !finalText.isEmpty()) {
                    logger.info("Field now contains: '" + finalText + "'");
                }
            } catch (Exception e) {
                // Ignore verification errors
            }
        } catch (Exception e) {
            logger.error("Failed to send keys to element: " + locator);
            throw e;
        }
    }
    
    /**
     * Get text from element
     * @param locator Element locator
     * @return Element text
     */
    protected String getText(By locator) {
        try {
            WebElement element = WaitHelper.waitForElementToBeVisible(locator);
            String text = element.getText();
            logger.info("Retrieved text '" + text + "' from element: " + locator);
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element: " + locator);
            throw e;
        }
    }
    
    /**
     * Check if element is displayed
     * @param locator Element locator
     * @return true if displayed, false otherwise
     */
    protected boolean isElementDisplayed(By locator) {
        return isElementDisplayed(locator, 5);
    }
    
    /**
     * Check if element is displayed with custom timeout
     * @param locator Element locator
     * @param timeout Wait timeout in seconds
     * @return true if displayed, false otherwise
     */
    protected boolean isElementDisplayed(By locator, int timeout) {
        try {
            WebElement element = WaitHelper.waitForElementToBeVisible(locator, timeout);
            boolean displayed = element.isDisplayed();
            logger.info("Element " + locator + " displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Element not displayed: " + locator);
            return false;
        }
    }
    
    /**
     * Check if element is enabled
     * @param locator Element locator
     * @return true if enabled, false otherwise
     */
    protected boolean isElementEnabled(By locator) {
        try {
            WebElement element = WaitHelper.waitForElementToBeVisible(locator);
            boolean enabled = element.isEnabled();
            logger.info("Element " + locator + " enabled: " + enabled);
            return enabled;
        } catch (Exception e) {
            logger.error("Failed to check if element is enabled: " + locator);
            throw e;
        }
    }
    
    /**
     * Wait for element to be visible
     * @param locator Element locator
     * @return WebElement
     */
    protected WebElement waitForElement(By locator) {
        logger.debug("Waiting for element: " + locator);
        return WaitHelper.waitForElementToBeVisible(locator);
    }
    
    /**
     * Wait for element to be visible with custom timeout
     * @param locator Element locator
     * @param timeoutInSeconds Timeout in seconds
     * @return WebElement
     */
    protected WebElement waitForElement(By locator, int timeoutInSeconds) {
        logger.debug("Waiting for element: " + locator + " with timeout: " + timeoutInSeconds);
        return WaitHelper.waitForElementToBeVisible(locator, timeoutInSeconds);
    }
    
    /**
     * Get attribute value from element
     * @param locator Element locator
     * @param attributeName Attribute name
     * @return Attribute value
     */
    protected String getAttribute(By locator, String attributeName) {
        try {
            WebElement element = WaitHelper.waitForElementToBeVisible(locator);
            String value = element.getAttribute(attributeName);
            logger.info("Retrieved attribute '" + attributeName + "' = '" + value + "' from element: " + locator);
            return value;
        } catch (Exception e) {
            logger.error("Failed to get attribute from element: " + locator);
            throw e;
        }
    }
    
    /**
     * Scroll to element (platform independent)
     * @param locator Element locator
     */
    protected void scrollToElement(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            // For Appium, we can use scrollIntoView via JavaScript
            driver.executeScript("arguments[0].scrollIntoView(true);", element);
            logger.info("Scrolled to element: " + locator);
        } catch (Exception e) {
            logger.error("Failed to scroll to element: " + locator);
            throw e;
        }
    }
    
    /**
     * Scroll down on the screen using gesture
     * Useful for scrolling in menus, lists, etc.
     */
    protected void scrollDown() {
        try {
            // Get screen dimensions
            int screenHeight = driver.manage().window().getSize().getHeight();
            int screenWidth = driver.manage().window().getSize().getWidth();
            
            // Calculate scroll start and end points (scroll from 80% to 20% of screen height)
            int startX = screenWidth / 2;
            int startY = (int) (screenHeight * 0.8);
            int endY = (int) (screenHeight * 0.2);
            
            // Perform scroll using W3C Actions API
            org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(
                org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
            org.openqa.selenium.interactions.Sequence scroll = new org.openqa.selenium.interactions.Sequence(finger, 0);
            
            // Move to start point, press, move to end point, release
            scroll.addAction(finger.createPointerMove(java.time.Duration.ZERO, 
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, startY));
            scroll.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            scroll.addAction(finger.createPointerMove(java.time.Duration.ofMillis(600), 
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, endY));
            scroll.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            
            driver.perform(java.util.Arrays.asList(scroll));
            logger.info("Scrolled down on screen");
        } catch (Exception e) {
            logger.error("Failed to scroll down: " + e.getMessage());
            throw new RuntimeException("Unable to scroll down", e);
        }
    }
}
