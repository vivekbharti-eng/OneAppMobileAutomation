package com.automation.pages;

import com.automation.utils.LocatorUtils;
import org.openqa.selenium.By;

/**
 * Home page object
 * Contains methods for home screen functionality
 */
public class HomePage extends BasePage {
    
    // Locator keys from property files
    private static final String WELCOME_MESSAGE = "home.welcome.xpath";
    private static final String WELCOME_MESSAGE_TEXT = "home.welcome.message.xpath";
    private static final String USERNAME_TEXT = "home.username.xpath";
    private static final String MENU_BUTTON = "home.menu.xpath";
    private static final String LOGOUT_BUTTON = "home.logout.xpath";
    private static final String PROFILE_LINK = "home.profile.xpath";
    private static final String PROFILE_PICTURE = "home.profile.picture.xpath";
    private static final String SIDE_MENU_DRAWER = "home.side.menu.drawer.xpath";
    private static final String SIDE_MENU_LOGOUT = "home.side.menu.logout.xpath";
    private static final String LOGOUT_POPUP = "home.logout.popup.xpath";
    private static final String LOGOUT_POPUP_TITLE = "home.logout.popup.title.xpath";
    private static final String LOGOUT_ANYWAY_BUTTON = "home.logout.anyway.button.xpath";
    private static final String LOGOUT_CANCEL_BUTTON = "home.logout.cancel.button.xpath";
    
    /**
     * Check if home page is displayed
     * @return true if home page displayed, false otherwise
     */
    public boolean isHomePageDisplayed() {
        try {
            By welcomeLocator = LocatorUtils.getLocator(WELCOME_MESSAGE);
            logger.info("Checking if home page is displayed...");
            // Wait longer for home page to appear after login
            waitForElement(welcomeLocator, 15);
            boolean displayed = isElementDisplayed(welcomeLocator);
            logger.info("Home page displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Primary home page check failed: " + e.getMessage());
            // Fallback: check for specific home screen elements only (not generic TextViews)
            try {
                By homeSpecificElement = By.xpath(
                    "//*[@content-desc='Send Money' or @text='Send Money']" +
                    " | //*[contains(@content-desc,'Balance') or contains(@text,'Balance')]" +
                    " | //*[@resource-id='button_profile_picture']");
                boolean found = isElementDisplayed(homeSpecificElement, 5);
                if (found) {
                    logger.info("Found specific home page element, home page is displayed");
                    return true;
                }
            } catch (Exception ex) {
                logger.error("No home page elements found");
            }
            return false;
        }
    }

    /**
     * Quick check for home page visibility with a short configurable timeout.
     * Use this to avoid killing the app when we're already on the home page.
     * @param timeoutSeconds max seconds to wait
     * @return true if Welcome element is visible within the timeout
     */
    public boolean isHomePageDisplayedQuick(int timeoutSeconds) {
        try {
            By welcomeLocator = LocatorUtils.getLocator(WELCOME_MESSAGE);
            boolean displayed = isElementDisplayed(welcomeLocator, timeoutSeconds);
            if (displayed) {
                logger.info("Quick home page check: VISIBLE (skipping app restart)");
            } else {
                logger.info("Quick home page check: not visible");
            }
            return displayed;
        } catch (Exception e) {
            logger.info("Quick home page check: not visible (" + e.getMessage() + ")");
            return false;
        }
    }

    /**
     * Check if app is on the PIN re-entry screen.
     * This screen appears when the app is restarted while an existing session is active.
     * It shows "Enter your PIN to log in" and requires PIN (not full login credentials).
     * @return true if PIN re-entry screen is displayed
     */
    public boolean isPinReEntryScreenDisplayed() {
        try {
            By pinReEntryLocator = By.xpath(
                "//*[@resource-id='enter_your_pin_to_login_text' or @content-desc='Enter your PIN to log in']"
            );
            boolean displayed = isElementDisplayed(pinReEntryLocator, 4);
            if (displayed) {
                logger.info("PIN re-entry screen detected (existing session, app was restarted)");
            }
            return displayed;
        } catch (Exception e) {
            logger.debug("PIN re-entry screen check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get welcome message text
     * @return Welcome message text
     */
    public String getWelcomeMessage() {
        By welcomeLocator = LocatorUtils.getLocator(WELCOME_MESSAGE);
        return getText(welcomeLocator);
    }
    
    /**
     * Get welcome message with username
     * @return Welcome message with username
     */
    public String getWelcomeMessageText() {
        try {
            By welcomeMessageLocator = LocatorUtils.getLocator(WELCOME_MESSAGE_TEXT);
            logger.info("Looking for welcome message with 'Welcome!' text...");
            waitForElement(welcomeMessageLocator, 10);
            String welcomeText = getText(welcomeMessageLocator);
            logger.info("Welcome message found: " + welcomeText);
            return welcomeText;
        } catch (Exception e) {
            logger.warn("Failed to get welcome message: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Verify welcome message is displayed with Welcome text
     * @return true if welcome message contains 'Welcome', false otherwise
     */
    public boolean isWelcomeMessageDisplayed() {
        try {
            String welcomeText = getWelcomeMessageText();
            boolean hasWelcome = welcomeText != null && !welcomeText.isEmpty() && 
                                (welcomeText.toLowerCase().contains("welcome") || 
                                 welcomeText.toLowerCase().contains("hi") ||
                                 welcomeText.toLowerCase().contains("hello"));
            logger.info("Welcome message verification: " + hasWelcome);
            return hasWelcome;
        } catch (Exception e) {
            logger.error("Failed to verify welcome message: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user is already logged in by detecting Welcome text on home page
     * @return true if already logged in (Welcome text visible), false otherwise
     */
    public boolean isAlreadyLoggedIn() {
        try {
            logger.info("Checking if user is already logged in...");

            // Primary check: look for Welcome/greeting text
            By welcomeMessageLocator = LocatorUtils.getLocator(WELCOME_MESSAGE_TEXT);
            if (isElementDisplayed(welcomeMessageLocator, 3)) {
                String welcomeText = getText(welcomeMessageLocator);
                if (welcomeText != null && !welcomeText.isEmpty()) {
                    logger.info("✓ User is already logged in. Found: '" + welcomeText + "'");
                    return true;
                }
            }

            // Fallback: check for known home screen elements (Send Money tile, Pay tab, balance).
            // NOTE: Do NOT include generic 'Home' content-desc here — it appears on other pages
            // (e.g. Transaction Details success page) and would cause a false positive.
            By homeElementLocator = By.xpath(
                "//*[@content-desc='Send Money' or @text='Send Money']" +
                " | //*[contains(@content-desc,'Balance') or contains(@text,'Balance')]" +
                " | //*[contains(@content-desc,'USD') and contains(@content-desc,'Balance')]");
            if (isElementDisplayed(homeElementLocator, 3)) {
                logger.info("✓ Home screen element detected - user is logged in");
                return true;
            }

            logger.info("✗ User is not logged in - no home screen elements found");
            return false;
        } catch (Exception e) {
            logger.info("✗ User is not logged in: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify welcome message contains expected text
     * @throws AssertionError if welcome message is not displayed properly
     */
    public void verifyWelcomeMessage() {
        String welcomeText = getWelcomeMessageText();
        if (welcomeText == null || welcomeText.isEmpty()) {
            logger.error("Welcome message is not displayed");
            throw new AssertionError("Welcome message is not displayed on home page");
        }
        if (!welcomeText.toLowerCase().contains("welcome") && 
            !welcomeText.toLowerCase().contains("hi") &&
            !welcomeText.toLowerCase().contains("hello")) {
            logger.error("Welcome message does not contain expected greeting text. Found: " + welcomeText);
            throw new AssertionError("Welcome message does not contain expected greeting. Found: " + welcomeText);
        }
        logger.info("Welcome message verified successfully: " + welcomeText);
    }
    
    /**
     * Click on screen to dismiss any drawer or popup
     */
    public void clickOnScreen() {
        try {
            logger.info("Clicking on screen to dismiss any drawer/popup...");
            // Click on center of screen
            int screenWidth = driver.manage().window().getSize().getWidth();
            int screenHeight = driver.manage().window().getSize().getHeight();
            
            // Tap at center of screen
            org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(
                org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
            org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(finger, 0);
            
            tap.addAction(finger.createPointerMove(java.time.Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), screenWidth / 2, screenHeight / 2));
            tap.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            
            driver.perform(java.util.Arrays.asList(tap));
            logger.info("Clicked on screen successfully");
            Thread.sleep(1000); // Wait for any drawer to dismiss
        } catch (Exception e) {
            logger.warn("Failed to click on screen: " + e.getMessage());
        }
    }
    
    /**
     * Press back ONLY if the biometric cancel button is still visible on screen.
     * Safe to call when we are not sure if the biometric drawer is open — avoids
     * accidentally navigating away from the EcoCash home page to the Android launcher.
     * @return true if back was pressed, false if skipped
     */
    public boolean pressBackOnlyIfBiometricDrawerOpen() {
        try {
            By cancelLocator = org.openqa.selenium.By.xpath(
                "//android.widget.Button[@text='Cancel' or @content-desc='Cancel' or contains(@text,'Cancel')]"
            );
            boolean biometricOpen = isElementDisplayed(cancelLocator, 2);
            if (biometricOpen) {
                logger.info("Biometric drawer detected — pressing back to dismiss");
                driver.navigate().back();
                Thread.sleep(1000);
                logger.info("Back pressed — biometric drawer dismissed");
                return true;
            } else {
                logger.info("No biometric drawer visible — skipping back press (staying on home page)");
                return false;
            }
        } catch (Exception e) {
            logger.warn("pressBackOnlyIfBiometricDrawerOpen check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Press back button to dismiss biometric drawer or any overlay.
     * Using driver.navigate().back() (W3C endpoint) instead of pressKey / press_keycode
     * because press_keycode crashes the UiAutomator2 instrumentation on some devices.
     */
    public void pressBackButton() {
        try {
            logger.info("Pressing back button (navigate().back())...");
            driver.navigate().back();
            logger.info("Back button pressed successfully");
            Thread.sleep(1000); // Wait for drawer/overlay to dismiss
        } catch (Exception e) {
            logger.warn("Failed to press back button: " + e.getMessage());
        }
    }
    
    /**
     * Ensure the app is on the Home screen before tapping the profile button.
     * Presses Back repeatedly until {@code button_profile_picture} is visible or max attempts exhausted.
     */
    private void ensureOnHomeScreen() {
        By profileBtn = By.xpath("//android.widget.ImageView[@resource-id='button_profile_picture']");
        By homeIndicator = By.xpath(
            "//*[@content-desc='Send Money' or @text='Send Money']" +
            " | //*[contains(@content-desc,'Balance') or contains(@text,'Balance')]" +
            " | //*[@resource-id='button_profile_picture']");

        // Already on home? — fast path
        if (isElementDisplayed(homeIndicator, 3)) {
            logger.info("Already on home screen — no navigation needed");
            return;
        }

        logger.warn("Not on home screen — pressing Back up to 5 times to navigate home...");
        for (int i = 0; i < 5; i++) {
            try {
                driver.navigate().back();
                Thread.sleep(1500);
            } catch (Exception ignored) {}
            if (isElementDisplayed(homeIndicator, 3)) {
                logger.info("Reached home screen after " + (i + 1) + " back press(es)");
                return;
            }
        }

        // Last resort: activate the app to bring it to foreground home tab
        try {
            logger.warn("Back presses did not reach home — activating app package...");
            if (driver instanceof io.appium.java_client.android.AndroidDriver androidDriver) {
                String appPackage = androidDriver.getCurrentPackage();
                if (appPackage != null && !appPackage.isBlank()) {
                    androidDriver.activateApp(appPackage);
                    Thread.sleep(2000);
                }
            }
        } catch (Exception ex) {
            logger.warn("App activate failed: " + ex.getMessage());
        }

        // Wait one more time for home indicators
        if (isElementDisplayed(homeIndicator, 5)) {
            logger.info("Home screen reached after app activation");
        } else {
            logger.warn("Could not confirm home screen — proceeding with profile tap anyway");
        }
    }

    /**
     * Tap profile picture button (resource-id: button_profile_picture) to open side menu.
     * Navigates to home screen first if needed, then falls back through multiple strategies.
     */
    public void tapProfileImageArea() {
        try {
            logger.info("Tapping profile picture button to open side menu...");

            // Ensure we are on the home screen before attempting the tap
            ensureOnHomeScreen();

            // Strategy 1: resource-id — most reliable
            By byResourceId = By.xpath("//android.widget.ImageView[@resource-id='button_profile_picture']");
            if (isElementDisplayed(byResourceId, 6)) {
                click(byResourceId);
                logger.info("Clicked profile picture via resource-id 'button_profile_picture'");
                Thread.sleep(2000);
                // Verify side menu opened (look for any menu element)
                By sideMenuCheck = By.xpath(
                    "//*[@content-desc='Logout' or @content-desc='Profile' or @content-desc='Settings']");
                if (!isElementDisplayed(sideMenuCheck, 4)) {
                    logger.warn("Side menu may not have opened after click — retrying once...");
                    click(byResourceId);
                    Thread.sleep(2000);
                }
                return;
            }
            logger.warn("Strategy 1 (resource-id) not found, trying UIAutomator...");

            // Strategy 2: UIAutomator resource-id
            try {
                By uia = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"button_profile_picture\")"
                );
                if (isElementDisplayed(uia, 5)) {
                    click(uia);
                    logger.info("Clicked profile picture via UIAutomator resourceId");
                    Thread.sleep(2000);
                    return;
                }
            } catch (Exception e2) {
                logger.warn("Strategy 2 (UIAutomator resourceId) failed: " + e2.getMessage());
            }

            // Strategy 3: XPath on any clickable ImageView in top-left quadrant
            try {
                By topLeftImg = By.xpath(
                    "//android.widget.ImageView[@clickable='true' and @bounds]");
                java.util.List<org.openqa.selenium.WebElement> imgs = driver.findElements(topLeftImg);
                for (org.openqa.selenium.WebElement img : imgs) {
                    int x = img.getLocation().getX();
                    int y = img.getLocation().getY();
                    if (x < 200 && y < 300) { // top-left area
                        img.click();
                        logger.info("Clicked top-left ImageView at (" + x + "," + y + ") as profile button");
                        Thread.sleep(2000);
                        return;
                    }
                }
            } catch (Exception e3) {
                logger.warn("Strategy 3 (top-left ImageView) failed: " + e3.getMessage());
            }

            // Strategy 4: coordinate tap at known profile icon position
            logger.warn("Falling back to coordinate tap at profile picture area...");
            tapAtCoordinates(60, 220);
            logger.info("Tapped at profile picture coordinates (60, 220)");
            Thread.sleep(2000);

        } catch (Exception e) {
            logger.error("tapProfileImageArea failed: " + e.getMessage());
        }
    }
    
    /**
     * Click menu button
     */
    public void clickMenu() {
        By menuLocator = LocatorUtils.getLocator(MENU_BUTTON);
        click(menuLocator);
        logger.info("Clicked menu button");
    }
    
    /**
     * Click logout button
     */
    public void clickLogout() {
        By logoutLocator = LocatorUtils.getLocator(LOGOUT_BUTTON);
        click(logoutLocator);
        logger.info("Clicked logout button");
    }
    
    /**
     * Click profile link
     */
    public void clickProfile() {
        By profileLocator = LocatorUtils.getLocator(PROFILE_LINK);
        click(profileLocator);
        logger.info("Clicked profile link");
    }
    
    /**
     * Wait for home page to load
     */
    public void waitForHomePage() {
        By welcomeLocator = LocatorUtils.getLocator(WELCOME_MESSAGE);
        waitForElement(welcomeLocator);
        logger.info("Home page loaded");
    }
    
    /**
     * Verify user is on home page
     * @throws AssertionError if home page is not displayed
     */
    public void verifyHomePageDisplayed() {
        if (!isHomePageDisplayed()) {
            logger.error("Home page is not displayed");
            throw new AssertionError("Home page is not displayed");
        }
        logger.info("Home page is displayed successfully");
    }
    
    /**
     * Click on profile picture to open side menu
     * Tries multiple strategies: XPath, AndroidUIAutomator, and coordinate-based tap
     */
    public void clickProfilePicture() {
        try {
            logger.info("Attempting to find and click profile picture...");
            
            // Strategy 1: Try XPath locator
            try {
                By profilePictureLocator = LocatorUtils.getLocator(PROFILE_PICTURE);
                logger.info("Strategy 1: Trying XPath locator...");
                waitForElement(profilePictureLocator, 5);
                click(profilePictureLocator);
                logger.info("Successfully clicked profile picture using XPath");
                Thread.sleep(2000);
                return;
            } catch (Exception e1) {
                logger.warn("Strategy 1 failed: " + e1.getMessage());
            }
            
            // Strategy 2: Try AndroidUIAutomator with clickable ImageView
            try {
                logger.info("Strategy 2: Trying AndroidUIAutomator with clickable ImageView...");
                By uiAutomatorLocator = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.ImageView\").clickable(true).instance(0)"
                );
                waitForElement(uiAutomatorLocator, 5);
                click(uiAutomatorLocator);
                logger.info("Successfully clicked profile picture using UIAutomator");
                Thread.sleep(2000);
                return;
            } catch (Exception e2) {
                logger.warn("Strategy 2 failed: " + e2.getMessage());
            }
            
            // Strategy 3: Try tapping top-left corner where profile pictures usually are
            try {
                logger.info("Strategy 3: Trying coordinate-based tap at top-left corner...");
                // Profile pictures are typically in top-left, around (100, 200) considering status bar
                tapAtCoordinates(100, 250);
                logger.info("Successfully tapped at profile picture location");
                Thread.sleep(2000);
                return;
            } catch (Exception e3) {
                logger.warn("Strategy 3 failed: " + e3.getMessage());
            }
            
            throw new RuntimeException("All strategies failed to click profile picture");
            
        } catch (Exception e) {
            logger.error("Failed to click profile picture: " + e.getMessage());
            throw new RuntimeException("Unable to click profile picture", e);
        }
    }
    
    /**
     * Tap at specific coordinates
     */
    private void tapAtCoordinates(int x, int y) {
        try {
            logger.info("Tapping at coordinates: (" + x + ", " + y + ")");
            org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(
                org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
            org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(finger, 0);
            
            tap.addAction(finger.createPointerMove(java.time.Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
            tap.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            
            driver.perform(java.util.Arrays.asList(tap));
            logger.info("Successfully tapped at coordinates");
        } catch (Exception e) {
            logger.error("Failed to tap at coordinates: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Scroll to bottom of side menu to reveal the Logout option.
     * Uses UIAutomator UiScrollable to scroll within the side menu panel.
     */
    public void scrollToBottomOfSideMenu() {
        try {
            logger.info("Scrolling to bottom of side menu to find Logout...");

            // First attempt: UIAutomator UiScrollable — scroll to end of any scrollable list
            try {
                By scrollable = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
                    ".scrollToEnd(5, 5)"
                );
                driver.findElement(scrollable);
                logger.info("UIAutomator scrollToEnd completed");
                Thread.sleep(800);
                return;
            } catch (Exception e1) {
                logger.warn("UIAutomator scrollToEnd attempt: " + e1.getMessage());
            }

            // Fallback: swipe up within left-side menu area (x=270, y: 1800→400)
            // Side menu typically occupies left ~60% of screen width
            logger.info("Falling back to swipe-up in side menu area...");
            for (int i = 0; i < 3; i++) {
                swipeInSideMenu();
                Thread.sleep(600);
                logger.debug("Side menu swipe " + (i + 1) + " of 3");
            }
            logger.info("Side menu scroll completed");

        } catch (Exception e) {
            String errorMsg = "Failed to scroll in side menu: " + e.getMessage();
            logger.error(errorMsg);
            logger.error("Stack trace: ", e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Swipe up within the side menu area (left portion of screen).
     */
    private void swipeInSideMenu() {
        try {
            // Screen: 1080x2392. Side menu is on the left ~600px.
            // Swipe from y=1800 to y=600 within x=300 (mid of side menu)
            org.openqa.selenium.interactions.PointerInput finger =
                new org.openqa.selenium.interactions.PointerInput(
                    org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
            org.openqa.selenium.interactions.Sequence swipe =
                new org.openqa.selenium.interactions.Sequence(finger, 0);
            swipe.addAction(finger.createPointerMove(java.time.Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), 300, 1800));
            swipe.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(finger.createPointerMove(java.time.Duration.ofMillis(600),
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), 300, 600));
            swipe.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(java.util.Arrays.asList(swipe));
            logger.debug("Swiped up in side menu area");
        } catch (Exception e) {
            logger.warn("swipeInSideMenu failed: " + e.getMessage());
        }
    }

    /**
     * Click on logout option in side menu.
     * Tries direct locator first, then UIAutomator scrollIntoView as fallback.
     */
    public void clickLogoutOption() {
        try {
            logger.info("Looking for logout option in side menu...");

            // Strategy 1: Confirmed content-desc="Logout" on android.view.View
            By contentDescLocator = By.xpath("//android.view.View[@content-desc='Logout']");
            if (isElementDisplayed(contentDescLocator, 6)) {
                click(contentDescLocator);
                logger.info("Clicked logout option (content-desc='Logout')");
                Thread.sleep(1500);
                return;
            }

            // Strategy 2: locator from properties file (TextView fallback)
            By logoutOptionLocator = LocatorUtils.getLocator(SIDE_MENU_LOGOUT);
            logger.warn("content-desc Logout not found — trying properties locator...");
            if (isElementDisplayed(logoutOptionLocator, 5)) {
                click(logoutOptionLocator);
                logger.info("Clicked logout option (properties locator)");
                Thread.sleep(1500);
                return;
            }

            // Strategy 3: UIAutomator description match
            logger.warn("Properties locator not found — trying UIAutomator description...");
            try {
                By uia = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().description(\"Logout\")"
                );
                if (isElementDisplayed(uia, 5)) {
                    click(uia);
                    logger.info("Clicked logout option (UIAutomator description)");
                    Thread.sleep(1500);
                    return;
                }
            } catch (Exception eUia) {
                logger.warn("UIAutomator description 'Logout' failed: " + eUia.getMessage());
            }

            // Strategy 4: UIAutomator scrollIntoView
            logger.warn("Trying UIAutomator scrollIntoView for Logout...");
            try {
                By scrollToLogout = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
                    ".scrollIntoView(new UiSelector().description(\"Logout\").instance(0))"
                );
                driver.findElement(scrollToLogout).click();
                logger.info("Clicked logout via UIAutomator scrollIntoView description");
                Thread.sleep(1500);
                return;
            } catch (Exception eUia2) {
                logger.warn("UIAutomator scrollIntoView failed: " + eUia2.getMessage());
            }

            throw new RuntimeException("Logout option not found in side menu after all strategies.");
        } catch (Exception e) {
            String errorMsg = "Failed to click logout option: " + e.getMessage();
            logger.error(errorMsg);
            logger.error("Stack trace: ", e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Check if logout confirmation popup is displayed
     * @return true if popup displayed, false otherwise
     */
    public boolean isLogoutPopupDisplayed() {
        logger.info("Checking for logout confirmation popup...");

        // Strategy 1: look for 'Logout anyway' button — definitive indicator the popup is open
        try {
            By s1 = By.xpath("//*[contains(translate(@content-desc,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'anyway') or contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'anyway')]");
            if (isElementDisplayed(s1, 8)) {
                logger.info("Logout popup detected via 'anyway' button");
                return true;
            }
        } catch (Exception e1) {
            logger.debug("Strategy 1 (anyway button): " + e1.getMessage());
        }

        // Strategy 2: UIAutomator — description or text contains 'Logout' on popup
        try {
            By s2 = io.appium.java_client.AppiumBy.androidUIAutomator(
                "new UiSelector().descriptionContains(\"Logout\").clickable(true)"
            );
            if (isElementDisplayed(s2, 5)) {
                logger.info("Logout popup detected via UIAutomator clickable Logout");
                return true;
            }
        } catch (Exception e2) {
            logger.debug("Strategy 2 (UIAutomator Logout clickable): " + e2.getMessage());
        }

        // Strategy 3: broad popup title — 'Are you sure' or 'Log out'
        try {
            By s3 = By.xpath("//*[contains(translate(@content-desc,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'are you sure') or contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'are you sure') or contains(translate(@content-desc,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log out') or contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log out')]");
            if (isElementDisplayed(s3, 5)) {
                logger.info("Logout popup detected via 'Are you sure'/'Log out' text");
                return true;
            }
        } catch (Exception e3) {
            logger.debug("Strategy 3 (Are you sure / Log out): " + e3.getMessage());
        }

        // Strategy 4: properties-file locator (catch-all)
        try {
            By popupLocator = LocatorUtils.getLocator(LOGOUT_POPUP);
            if (isElementDisplayed(popupLocator, 5)) {
                logger.info("Logout popup detected via properties locator");
                return true;
            }
        } catch (Exception e4) {
            logger.debug("Strategy 4 (properties locator): " + e4.getMessage());
        }

        logger.warn("Logout confirmation popup NOT detected after all strategies");
        return false;
    }
    
    /**
     * Click on 'Logout Anyway' button in confirmation popup.
     * Tries multiple strategies to handle different label/element-type variants
     * that the EcoCash app may use for this button.
     */
    public void clickLogoutAnywayButton() {
        try {
            logger.info("Looking for 'Logout Anyway' button on popup...");

            // Strategy 1: content-desc="Logout anyway" — exact case (android.view.View)
            By s1 = By.xpath("//android.view.View[@content-desc='Logout anyway']");
            if (isElementDisplayed(s1, 5)) {
                click(s1);
                logger.info("Clicked 'Logout anyway' (content-desc exact, View)");
                Thread.sleep(2000);
                return;
            }

            // Strategy 2: all element types with content-desc containing 'anyway' (case variations)
            By s2 = By.xpath("//*[contains(translate(@content-desc,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'anyway')]");
            if (isElementDisplayed(s2, 4)) {
                click(s2);
                logger.info("Clicked 'Logout anyway' (content-desc contains 'anyway')");
                Thread.sleep(2000);
                return;
            }

            // Strategy 3: text contains 'anyway' (Button or TextView)
            By s3 = By.xpath("//*[contains(translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'anyway')]");
            if (isElementDisplayed(s3, 4)) {
                click(s3);
                logger.info("Clicked 'Logout anyway' (text contains 'anyway')");
                Thread.sleep(2000);
                return;
            }

            // Strategy 4: UIAutomator — descriptionContains 'anyway' (case-insensitive)
            try {
                By uia1 = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().descriptionContains(\"anyway\")"
                );
                if (isElementDisplayed(uia1, 4)) {
                    click(uia1);
                    logger.info("Clicked via UIAutomator descriptionContains 'anyway'");
                    Thread.sleep(2000);
                    return;
                }
            } catch (Exception e1) {
                logger.warn("UIAutomator descriptionContains 'anyway': " + e1.getMessage());
            }

            // Strategy 5: UIAutomator — textContains 'Logout' on clickable element
            try {
                By uia2 = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Logout\").clickable(true)"
                );
                if (isElementDisplayed(uia2, 4)) {
                    click(uia2);
                    logger.info("Clicked via UIAutomator textContains 'Logout' clickable");
                    Thread.sleep(2000);
                    return;
                }
            } catch (Exception e2) {
                logger.warn("UIAutomator textContains 'Logout': " + e2.getMessage());
            }

            // Strategy 6: UIAutomator — descriptionContains 'Logout' on clickable element
            try {
                By uia3 = io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiSelector().descriptionContains(\"Logout\").clickable(true)"
                );
                if (isElementDisplayed(uia3, 4)) {
                    click(uia3);
                    logger.info("Clicked via UIAutomator descriptionContains 'Logout' clickable");
                    Thread.sleep(2000);
                    return;
                }
            } catch (Exception e3) {
                logger.warn("UIAutomator descriptionContains 'Logout': " + e3.getMessage());
            }

            // Strategy 7: properties locator (catch-all from android_locators.properties)
            By propLocator = LocatorUtils.getLocator(LOGOUT_ANYWAY_BUTTON);
            if (isElementDisplayed(propLocator, 4)) {
                click(propLocator);
                logger.info("Clicked 'Logout Anyway' button (properties locator)");
                Thread.sleep(2000);
                return;
            }

            throw new RuntimeException("Logout anyway button not found on popup after all strategies.");
        } catch (Exception e) {
            String errorMsg = "Failed to click logout anyway button: " + e.getMessage();
            logger.error(errorMsg);
            logger.error("Stack trace: ", e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Click on 'Cancel' button in logout confirmation popup
     */
    public void clickCancelButton() {
        try {
            By cancelButtonLocator = LocatorUtils.getLocator(LOGOUT_CANCEL_BUTTON);
            logger.info("Looking for 'Cancel' button...");
            waitForElement(cancelButtonLocator, 10);
            click(cancelButtonLocator);
            logger.info("Clicked on 'Cancel' button");
            Thread.sleep(1000); // Wait for popup to close
        } catch (Exception e) {
            logger.error("Failed to click cancel button: " + e.getMessage());
            throw new RuntimeException("Unable to click cancel button", e);
        }
    }
    
    /**
     * Verify logout confirmation popup is displayed
     * @throws AssertionError if popup is not displayed
     */
    public void verifyLogoutPopupDisplayed() {
        if (!isLogoutPopupDisplayed()) {
            String errorMsg = "Logout confirmation popup is not displayed";
            logger.error(errorMsg);
            logger.error("Possible reasons: Popup failed to appear, wrong locator, popup appeared and auto-dismissed");
            throw new AssertionError(errorMsg);
        }
        logger.info("Logout confirmation popup verified");
    }
    
    /**
     * Perform complete logout flow (for cleanup purposes)
     * Used in @AfterSuite to ensure user is logged out
     * Silently handles errors to not break suite teardown
     */
    public void performLogout() {
        try {
            logger.info("Attempting to logout...");
            
            // Press back to dismiss any drawer
            pressBackButton();
            
            // Tap profile area to open side menu
            tapProfileImageArea();
            
            // Scroll to find logout
            scrollToBottomOfSideMenu();
            
            // Click logout option
            By logoutOptionLocator = LocatorUtils.getLocator(SIDE_MENU_LOGOUT);
            if (isElementDisplayed(logoutOptionLocator, 3)) {
                click(logoutOptionLocator);
                Thread.sleep(1000);
                
                // Click logout anyway if popup appears
                By logoutAnywayLocator = LocatorUtils.getLocator(LOGOUT_ANYWAY_BUTTON);
                if (isElementDisplayed(logoutAnywayLocator, 3)) {
                    click(logoutAnywayLocator);
                    logger.info("Logout completed successfully");
                    Thread.sleep(2000);
                } else {
                    logger.warn("Logout confirmation popup not found, but logout may have succeeded");
                }
            } else {
                logger.warn("Logout option not found in side menu");
            }
        } catch (Exception e) {
            logger.warn("Error during logout: " + e.getMessage());
            logger.warn("Continuing with suite teardown...");
        }
    }
}
