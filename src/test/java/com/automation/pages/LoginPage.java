package com.automation.pages;

import com.automation.utils.LocatorUtils;
import com.automation.utils.PropertyReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * EcoCash Login page object
 * Contains methods for EcoCash login functionality
 */
public class LoginPage extends BasePage {
    
    // Locator keys from property files
    private static final String AGREE_CONTINUE_BUTTON = "login.agree.continue.button.xpath";
    private static final String COUNTRY_CODE_DROPDOWN = "login.countrycode.dropdown.xpath";
    private static final String COUNTRY_CODE_ZIMBABWE = "login.countrycode.zimbabwe.uiautomator";
    private static final String MOBILE_NUMBER_FIELD = "login.mobile.xpath";
    private static final String CONTINUE_BUTTON = "login.continue.button.xpath";
    private static final String OTP_FIELD = "login.otp.xpath";
    private static final String VERIFY_BUTTON = "login.verify.button.xpath";
    private static final String PIN_FIELD = "login.pin.xpath";
    // Unique screen indicator — only visible on the PIN login screen (not on OTP screen)
    private static final String PIN_SCREEN = "login.pin.screen.xpath";
    private static final String BIOMETRIC_CANCEL_BUTTON = "login.biometric.cancel.xpath";
    private static final String BIOMETRIC_POPUP = "login.biometric.popup.xpath";
    private static final String ERROR_MESSAGE = "login.error.xpath";
    
    /**
     * Click on Agree and Continue button on welcome/onboarding screen
     */
    public void clickAgreeAndContinue() {
        try {
            By agreeButtonLocator = LocatorUtils.getLocator(AGREE_CONTINUE_BUTTON);
            waitForElement(agreeButtonLocator, 10);
            click(agreeButtonLocator);
            logger.info("Clicked Agree and Continue button");
        } catch (Exception e) {
            logger.warn("Agree and Continue button not found, may already be on login page: " + e.getMessage());
        }
    }
    
    /**
     * Select country code from dropdown
     * @param countryCode Country code to select (e.g., +263 for Zimbabwe)
     */
    public void selectCountryCode(String countryCode) {
        try {
            // Step 1: Click on country code dropdown to open the bottom drawer
            By dropdownLocator = LocatorUtils.getLocator(COUNTRY_CODE_DROPDOWN);
            waitForElement(dropdownLocator, 10);
            click(dropdownLocator);
            logger.info("Step 1: Clicked country code dropdown to open bottom drawer");
            
            // Step 2: Wait for the drawer to animate up from bottom and list to load
            Thread.sleep(3000);
            logger.info("Step 2: Bottom drawer animation complete, list loaded");
            
            // Step 3: Locate and tap on the Zimbabwe option from the drawer list
            if (countryCode.equals("+263")) {
                By zimbabweLocator = LocatorUtils.getLocator(COUNTRY_CODE_ZIMBABWE);
                logger.info("Step 3: Searching for Zimbabwe (+263) in bottom drawer using content-desc...");
                waitForElement(zimbabweLocator, 15);
                logger.info("Step 3: Found Zimbabwe option, clicking now...");
                click(zimbabweLocator);
                logger.info("Step 3: Successfully clicked Zimbabwe (+263) from bottom drawer");
            } else {
                // For other country codes, use a generic xpath
                By countryLocator = By.xpath("//android.view.View[contains(@content-desc, '" + countryCode + "')]");
                logger.info("Step 3: Looking for country code " + countryCode + " option in the drawer...");
                waitForElement(countryLocator, 15);
                click(countryLocator);
                logger.info("Step 3: Tapped on country code " + countryCode + " option from drawer");
            }
            
            // Step 4: Wait for drawer to close after selection
            Thread.sleep(1000);
            logger.info("Step 4: Country code selected, drawer closing...");
        } catch (Exception e) {
            logger.error("Failed to select country code from drawer: " + e.getMessage());
            throw new RuntimeException("Unable to select country code " + countryCode, e);
        }
    }
    
    /**
     * Enter country code (deprecated - use selectCountryCode instead)
     * @param countryCode Country code to enter (e.g., +263)
     */
    @Deprecated
    public void enterCountryCode(String countryCode) {
        selectCountryCode(countryCode);
    }
    
    /**
     * Enter mobile number
     * @param mobileNumber Mobile number to enter
     */
    public void enterMobileNumber(String mobileNumber) {
        try {
            By mobileLocator = LocatorUtils.getLocator(MOBILE_NUMBER_FIELD);
            logger.info("Waiting for mobile number field to appear...");
            waitForElement(mobileLocator, 10);
            Thread.sleep(1000); // Wait for field to be fully ready
            
            // Step 1: Click on mobile number field first
            logger.info("Clicking on mobile number field...");
            click(mobileLocator);
            Thread.sleep(500);
            
            // Step 2: Get the element and clear it
            WebElement mobileElement = driver.findElement(mobileLocator);
            
            // Clear the field aggressively - try clear() multiple times
            logger.info("Clearing mobile number field...");
            for (int i = 0; i < 3; i++) {
                try {
                    mobileElement.clear();
                    Thread.sleep(200);
                } catch (Exception e) {
                    logger.debug("Clear attempt " + (i+1) + " had issue, continuing...");
                }
            }
            
            // Step 3: Enter mobile number
            logger.info("Entering mobile number: " + mobileNumber);
            mobileElement.sendKeys(mobileNumber);
            logger.info("Entered mobile number: " + mobileNumber);
            
            // Verify text was entered correctly
            Thread.sleep(500);
            String enteredText = mobileElement.getText();
            logger.info("Mobile number field contains: '" + enteredText + "'");
            
            // If prefix is still there, try to clear and re-enter
            if (enteredText != null && !enteredText.equals(mobileNumber) && enteredText.contains(mobileNumber)) {
                logger.warn("Field has prefix, attempting to clear and re-enter...");
                mobileElement.clear();
                Thread.sleep(300);
                mobileElement.clear();
                Thread.sleep(300);
                mobileElement.sendKeys(mobileNumber);
                Thread.sleep(500);
                String finalText = mobileElement.getText();
                logger.info("After re-entry, field contains: '" + finalText + "'");
            }
            
        } catch (Exception e) {
            logger.error("Failed to enter mobile number: " + e.getMessage());
            throw new RuntimeException("Unable to enter mobile number", e);
        }
    }
    
    /**
     * Click continue button
     */
    public void clickContinueButton() {
        try {
            By continueButtonLocator = LocatorUtils.getLocator(CONTINUE_BUTTON);
            waitForElement(continueButtonLocator, 10);
            Thread.sleep(500); // Small wait for button to be stable
            click(continueButtonLocator);
            logger.info("Clicked continue button");
            Thread.sleep(2000); // Wait for next page to load
        } catch (Exception e) {
            logger.error("Failed to click continue button: " + e.getMessage());
            throw new RuntimeException("Unable to click continue button", e);
        }
    }
    
    /**
     * Enter OTP
     * @param otp OTP to enter
     */
    public void enterOTP(String otp) {
        try {
            By otpLocator = LocatorUtils.getLocator(OTP_FIELD);
            logger.info("Waiting for OTP field to appear...");
            waitForElement(otpLocator, 15);
            Thread.sleep(500);
            
            WebElement otpElement = driver.findElement(otpLocator);
            
            // Click and clear field multiple times
            click(otpLocator);
            Thread.sleep(300);
            
            for (int i = 0; i < 3; i++) {
                try {
                    otpElement.clear();
                    Thread.sleep(150);
                } catch (Exception e) {
                    logger.debug("OTP clear attempt " + (i+1) + " had issue");
                }
            }
            
            otpElement.sendKeys(otp);
            logger.info("Entered OTP: " + otp);
        } catch (Exception e) {
            logger.error("Failed to enter OTP: " + e.getMessage());
            throw new RuntimeException("Unable to enter OTP", e);
        }
    }
    
    /**
     * Click verify button after OTP entry.
     * Feasibility check: if Verify button is NOT visible and the app has NOT
     * auto-advanced to the PIN screen, execution is stopped with a clear error.
     */
    public void clickVerifyButton() {
        By verifyButtonLocator = LocatorUtils.getLocator(VERIFY_BUTTON);
        By pinFieldLocator     = LocatorUtils.getLocator(PIN_FIELD);
        // Use the PIN screen unique indicator (not generic EditText) to detect if we're on PIN screen
        By pinScreenLocator    = LocatorUtils.getLocator(PIN_SCREEN);

        // 1. App may auto-advance straight to PIN screen after OTP — detect using the PIN screen unique indicator
        if (isElementDisplayed(pinScreenLocator, 3)) {
            logger.info("App auto-advanced to PIN screen — Verify button not required");
            return;
        }

        // 2. Verify button is present — click it
        if (isElementDisplayed(verifyButtonLocator, 7)) {
            try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            click(verifyButtonLocator);
            logger.info("Verify button found and clicked");
            try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

            // Confirm the app moved forward (PIN screen should appear)
            if (!isElementDisplayed(pinScreenLocator, 8)) {
                String msg = "Verify button was clicked but PIN screen did not appear — " +
                             "possible OTP error or navigation failure";
                logger.error(msg);
                throw new RuntimeException(msg);
            }
            return;
        }

        // 3. Neither Verify button nor PIN screen is available — hard stop
        String errorMsg = "FEASIBILITY CHECK FAILED: Verify button is not visible on screen " +
                          "and app has not auto-advanced to PIN screen. " +
                          "Possible causes: OTP screen not loaded, wrong OTP entered, " +
                          "or unexpected navigation. Stopping execution.";
        logger.error(errorMsg);
        throw new RuntimeException(errorMsg);
    }
    
    /**
     * Enter PIN
     * @param pin PIN to enter
     */
    public void enterPIN(String pin) {
        try {
            By pinLocator = LocatorUtils.getLocator(PIN_FIELD);
            logger.info("Waiting for PIN field to appear...");
            waitForElement(pinLocator, 15);
            Thread.sleep(500);
            
            // Step 1: Click on PIN field first
            logger.info("Step 1: Clicking on PIN field...");
            click(pinLocator);
            Thread.sleep(500);
            
            // Step 2: Get element and clear it
            WebElement pinElement = driver.findElement(pinLocator);
            logger.info("Step 2: Clearing PIN field...");
            for (int i = 0; i < 3; i++) {
                try {
                    pinElement.clear();
                    Thread.sleep(150);
                } catch (Exception e) {
                    logger.debug("PIN clear attempt " + (i+1) + " had issue");
                }
            }
            
            // Step 3: Enter PIN
            logger.info("Step 3: Entering PIN...");
            pinElement.sendKeys(pin);
            logger.info("Entered PIN");
            
            // After entering PIN, check if there's a submit button, otherwise wait for auto-submit
            try {
                By submitButton = By.xpath("//android.widget.Button[@content-desc='Submit' or @text='Submit']");
                if (isElementDisplayed(submitButton, 3)) {
                    click(submitButton);
                    logger.info("Clicked submit button after PIN entry");
                } else {
                    logger.info("No submit button found, waiting for auto-submit...");
                }
            } catch (Exception e) {
                logger.info("No submit button, assuming auto-submit after PIN entry");
            }

            // NOTE: Biometric popup is handled by an explicit step after PIN entry.
            // Do NOT call handleBiometricPopup() here — keep PIN entry pure.
            Thread.sleep(2000); // Brief wait for the biometric bottom sheet to appear
            logger.info("PIN entered — waiting for biometric prompt or home page...");
        } catch (Exception e) {
            logger.error("Failed to enter PIN: " + e.getMessage());
            throw new RuntimeException("Unable to enter PIN", e);
        }
    }
    
    /**
     * Perform complete login action with all credentials
     * @param countryCode Country code
     * @param mobileNumber Mobile number
     * @param otp OTP
     * @param pin PIN
     */
    public void login(String countryCode, String mobileNumber, String otp, String pin) {
        logger.info("Performing EcoCash login with mobile: " + countryCode + mobileNumber);
        enterCountryCode(countryCode);
        enterMobileNumber(mobileNumber);
        clickContinueButton();
        enterOTP(otp);
        clickVerifyButton();
        enterPIN(pin);
    }
    
    /**
     * Login with credentials from feature-specific testdata file (login.properties)
     */
    public void loginWithConfigCredentials() {
        String countryCode = PropertyReader.getTestDataProperty("country.code");
        String mobileNumber = PropertyReader.getTestDataProperty("mobile.number");
        String otp          = PropertyReader.getTestDataProperty("otp");
        String pin          = PropertyReader.getTestDataProperty("pin");
        login(countryCode, mobileNumber, otp, pin);
    }
    
    /**
     * Check if error message is displayed
     * @return true if error message displayed, false otherwise
     */
    public boolean isErrorMessageDisplayed() {
        By errorLocator = LocatorUtils.getLocator(ERROR_MESSAGE);
        return isElementDisplayed(errorLocator);
    }
    
    /**
     * Get error message text
     * @return Error message text
     */
    public String getErrorMessage() {
        By errorLocator = LocatorUtils.getLocator(ERROR_MESSAGE);
        return getText(errorLocator);
    }
    
    /**
     * Wait for login page to load
     */
    public void waitForLoginPage() {
        By countryCodeLocator = LocatorUtils.getLocator(COUNTRY_CODE_DROPDOWN);
        waitForElement(countryCodeLocator);
        logger.info("EcoCash login page loaded");
    }
    
    /**
     * Wait for the "Enable Biometric" bottom sheet to appear after PIN entry,
     * then click the CANCEL button (never Continue).
     * Called automatically after PIN entry and also available as a public step.
     */
    public void handleBiometricPopup() {
        // Virtual device (emulator) does not show biometric prompt — skip entirely
        if ("virtual".equalsIgnoreCase(PropertyReader.getExecutionTarget())) {
            logger.info("Virtual device detected — biometric popup handling skipped");
            return;
        }
        try {
            // Step 1: Wait up to 5s for the biometric bottom sheet to appear
            By popupLocator = LocatorUtils.getLocator(BIOMETRIC_POPUP);
            boolean popupVisible = isElementDisplayed(popupLocator, 5);

            if (!popupVisible) {
                // Fallback: look for any biometric-related text on screen
                By fallbackPopup = By.xpath(
                    "//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'biometric')" +
                    " or contains(translate(@content-desc,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'biometric')" +
                    " or contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'fingerprint')" +
                    " or contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'face id')]"
                );
                popupVisible = isElementDisplayed(fallbackPopup, 3);
            }

            if (!popupVisible) {
                logger.info("No biometric popup detected after PIN entry — continuing.");
                return;
            }

            logger.info("Biometric bottom sheet detected — looking for Cancel button...");

            // Step 2: Click CANCEL — try most-specific locators first to avoid hitting Continue

            // UIAutomator exact text 'Cancel'
            try {
                By uiaCancel = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().text(\"Cancel\")"
                );
                if (isElementDisplayed(uiaCancel, 3)) {
                    click(uiaCancel);
                    logger.info("Clicked Cancel on biometric popup (UIAutomator text='Cancel')");
                    Thread.sleep(1500);
                    return;
                }
            } catch (Exception e1) {
                logger.debug("UIAutomator text='Cancel' not found: " + e1.getMessage());
            }

            // UIAutomator exact content-desc 'Cancel'
            try {
                By uiaCancelDesc = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().description(\"Cancel\")"
                );
                if (isElementDisplayed(uiaCancelDesc, 3)) {
                    click(uiaCancelDesc);
                    logger.info("Clicked Cancel on biometric popup (UIAutomator desc='Cancel')");
                    Thread.sleep(1500);
                    return;
                }
            } catch (Exception e2) {
                logger.debug("UIAutomator desc='Cancel' not found: " + e2.getMessage());
            }

            // XPath exact-match Cancel (from locator property)
            By propLocator = LocatorUtils.getLocator(BIOMETRIC_CANCEL_BUTTON);
            if (isElementDisplayed(propLocator, 3)) {
                click(propLocator);
                logger.info("Clicked Cancel on biometric popup (property locator)");
                Thread.sleep(1500);
                return;
            }

            // Last resort: XPath exact text/desc 'Cancel' across all element types
            By xpathCancel = By.xpath(
                "//*[@text='Cancel' or @content-desc='Cancel']"
            );
            if (isElementDisplayed(xpathCancel, 3)) {
                click(xpathCancel);
                logger.info("Clicked Cancel on biometric popup (XPath exact text/desc)");
                Thread.sleep(1500);
                return;
            }

            logger.warn("Biometric popup visible but Cancel button not found — skipping.");
        } catch (Exception e) {
            logger.info("Biometric popup handling skipped: " + e.getMessage());
        }
    }
}
