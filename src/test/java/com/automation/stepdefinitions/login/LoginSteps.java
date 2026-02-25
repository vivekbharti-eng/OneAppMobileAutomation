package com.automation.stepdefinitions.login;

import com.automation.pages.HomePage;
import com.automation.pages.LoginPage;
import com.automation.reports.ExtentReportManager;
import com.automation.utils.PropertyReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * Step definitions for EcoCash Login feature
 * Contains step implementations for EcoCash login scenarios
 */
public class LoginSteps {
    
    private static final Logger logger = LogManager.getLogger(LoginSteps.class);
    private LoginPage loginPage;
    private HomePage homePage;
    
    // Flag to track if user is already logged in (to skip login steps)
    private static boolean alreadyLoggedIn = false;
    
    public LoginSteps() {
        loginPage = new LoginPage();
        homePage = new HomePage();
    }
    
    @Given("the app is launched")
    public void theAppIsLaunched() {
        logger.info("EcoCash app is launched — checking current app state");
        try {
            // Quick check: if home page (Welcome) is already visible, skip restart entirely
            if (homePage.isHomePageDisplayedQuick(3)) {
                logger.info("Home page already visible — skipping app restart (reusing logged-in session)");
                ExtentReportManager.logInfo("App already on home page — no restart needed");
                return;
            }

            io.appium.java_client.AppiumDriver driver = com.automation.drivers.DriverManager.getDriver();
            if (driver instanceof io.appium.java_client.android.AndroidDriver androidDriver) {
                logger.info("App not on home page — terminating to clear any mid-state from previous test...");
                androidDriver.terminateApp("com.sasai.sasaipay");
                Thread.sleep(1500);
                logger.info("Activating app fresh from MainActivity...");
                androidDriver.activateApp("com.sasai.sasaipay");
                logger.info("App restarted successfully from MainActivity");
            }
            Thread.sleep(7000); // Wait for app to fully initialize after restart
            ExtentReportManager.logInfo("EcoCash app launched and ready");
        } catch (Exception e) {
            logger.warn("App restart failed, proceeding anyway: " + e.getMessage());
            try { Thread.sleep(3000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            ExtentReportManager.logInfo("App launched (restart fallback)");
        }
    }
    
    @When("I click on Agree and Continue")
    public void iClickOnAgreeAndContinue() {
        loginPage.clickAgreeAndContinue();
        logger.info("Clicked on Agree and Continue button");
        ExtentReportManager.logInfo("Clicked Agree and Continue button");
        
        // Wait for login page to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted: " + e.getMessage());
        }
    }
    
    @Given("I logout if already logged in")
    public void iLogoutIfAlreadyLoggedIn() {
        try {
            // Check if home page is displayed (user already logged in)
            if (homePage.isHomePageDisplayed()) {
                logger.info("User is already logged in, logging out...");
                ExtentReportManager.logInfo("User already logged in, initiating logout");
                
                // Click menu button
                homePage.clickMenu();
                Thread.sleep(1000); // Wait for menu to open
                
                // Click logout button
                homePage.clickLogout();
                Thread.sleep(2000); // Wait for logout to complete
                
                logger.info("Logout successful");
                ExtentReportManager.logInfo("Logout completed successfully");
            } else {
                logger.info("User is not logged in, proceeding to login");
                ExtentReportManager.logInfo("User not logged in, ready for login");
            }
        } catch (Exception e) {
            logger.info("Home page not displayed, user not logged in: " + e.getMessage());
            ExtentReportManager.logInfo("User not logged in, proceeding with login flow");
        }
    }
    
    @Given("I skip login if already logged in")
    public void iSkipLoginIfAlreadyLoggedIn() {
        try {
            String featureUri = com.automation.hooks.Hooks.currentFeatureUri.get();
            String pin = PropertyReader.getTestDataProperty("pin");

            // ---------------------------------------------------------------
            // Helper: detect PIN re-entry screen (app restarted with active session)
            // ---------------------------------------------------------------
            boolean onPinReEntry = homePage.isPinReEntryScreenDisplayed();

            // ---------------------------------------------------------------
            // LOGIN FEATURE — must always start from the FULL login screen
            // ---------------------------------------------------------------
            if (featureUri != null && featureUri.contains("Login.feature")) {
                logger.info("Login flow detected — ensuring app is on full login screen");

                if (onPinReEntry) {
                    logger.info("PIN re-entry screen detected — entering PIN to reach home, then will logout");
                    ExtentReportManager.logInfo("Login feature: PIN re-entry detected, entering PIN then logging out");
                    try {
                        loginPage.enterPIN(pin);
                        Thread.sleep(3000); // wait for biometric popup to appear after PIN
                        homePage.dismissBiometricPopup(); // click Cancel button on biometric popup
                        Thread.sleep(1000);
                        homePage.pressBackOnlyIfBiometricDrawerOpen(); // safe back — only if drawer still open
                        Thread.sleep(1500);
                        // Verify we are on the home page before proceeding to logout
                        if (homePage.isHomePageDisplayedQuick(5)) {
                            logger.info("Home page confirmed after biometric cancel");
                        } else {
                            logger.warn("Home page not detected after biometric cancel — proceeding anyway");
                        }
                    } catch (Exception e) {
                        logger.warn("PIN entry / biometric dismiss before logout failed: " + e.getMessage());
                    }
                }

                if (onPinReEntry || homePage.isAlreadyLoggedIn()) {
                    logger.info("Performing logout so Login test can run from full login screen");
                    try {
                        homePage.performLogout();
                        Thread.sleep(2000);
                        // Re-activate app — logout may have exited to Android launcher
                        io.appium.java_client.AppiumDriver driver = com.automation.drivers.DriverManager.getDriver();
                        if (driver instanceof io.appium.java_client.android.AndroidDriver androidDriver) {
                            androidDriver.activateApp("com.sasai.sasaipay");
                            Thread.sleep(3000);
                            logger.info("App re-activated after logout — should be on login screen");
                        }
                    } catch (Exception e) {
                        logger.warn("Logout before Login test failed (proceeding anyway): " + e.getMessage());
                    }
                } else {
                    logger.info("App is on full login screen — proceeding with Login test");
                }
                alreadyLoggedIn = false;
                return;
            }

            // ---------------------------------------------------------------
            // NON-LOGIN FEATURES (SendMoney, Logout, etc.)
            // ---------------------------------------------------------------
            logger.info("======================================");
            logger.info("Checking if user is already logged in");
            logger.info("======================================");

            if (onPinReEntry) {
                // App has an active session — enter PIN to resume, dismiss biometric, verify home
                logger.info("PIN re-entry screen detected — entering PIN to reach home page");
                ExtentReportManager.logInfo("PIN re-entry screen: entering PIN to resume session");
                try {
                    loginPage.enterPIN(pin);
                    Thread.sleep(3000); // wait for biometric popup to appear after PIN
                    homePage.dismissBiometricPopup(); // click Cancel on biometric popup
                    Thread.sleep(1000);
                    homePage.pressBackOnlyIfBiometricDrawerOpen(); // safe back only if drawer still open
                    Thread.sleep(1500);
                    // Only check for welcome/home page — no logout needed
                    if (homePage.isHomePageDisplayedQuick(5)) {
                        logger.info("Welcome/home page confirmed after biometric cancel");
                        ExtentReportManager.logPass("PIN re-entry + biometric dismissed — on home page");
                    } else {
                        logger.warn("Home page not detected after biometric cancel — proceeding");
                    }
                } catch (Exception e) {
                    logger.warn("PIN re-entry / biometric dismiss failed: " + e.getMessage());
                }
                alreadyLoggedIn = true;
                return;
            }

            // Already on home page (e.g. after Login scenario succeeded) — no logout check needed
            if (homePage.isAlreadyLoggedIn()) {
                logger.info("✓ User already on home page — skipping login steps");
                ExtentReportManager.logPass("Home page detected — skipping login");
                alreadyLoggedIn = true;
                // Do NOT press back or trigger any logout — biometric/back handled by Background steps
            } else {
                logger.info("✗ User is NOT logged in - Will proceed with login steps");
                ExtentReportManager.logInfo("User not logged in - proceeding with login flow");
                alreadyLoggedIn = false;
            }
        } catch (Exception e) {
            logger.warn("Could not verify login status: " + e.getMessage());
            ExtentReportManager.logWarning("Could not verify login status, will attempt login");
            alreadyLoggedIn = false;
        }
    }
    
    @When("I enter country code {string}")
    public void iEnterCountryCode(String countryCode) {
        loginPage.selectCountryCode(countryCode);
        logger.info("Selected country code: " + countryCode);
        ExtentReportManager.logInfo("Selected country code: " + countryCode);
    }
    
    @When("I enter country code from config")
    public void iEnterCountryCodeFromConfig() {
        if (alreadyLoggedIn) {
            logger.info("⏭ Skipping: User already logged in");
            ExtentReportManager.logInfo("Skipped - User already logged in");
            return;
        }
        String countryCode = PropertyReader.getTestDataProperty("country.code");
        loginPage.selectCountryCode(countryCode);
        logger.info("Selected country code from testdata: " + countryCode);
        ExtentReportManager.logInfo("Selected country code: " + countryCode);
    }
    
    @When("I enter mobile number {string}")
    public void iEnterMobileNumber(String mobileNumber) {
        loginPage.enterMobileNumber(mobileNumber);
        logger.info("Entered mobile number: " + mobileNumber);
        ExtentReportManager.logInfo("Entered mobile number: " + mobileNumber);
    }
    
    @When("I enter mobile number from config")
    public void iEnterMobileNumberFromConfig() {
        if (alreadyLoggedIn) {
            logger.info("⏭ Skipping: User already logged in");
            ExtentReportManager.logInfo("Skipped - User already logged in");
            return;
        }
        String mobileNumber = PropertyReader.getTestDataProperty("mobile.number");
        loginPage.enterMobileNumber(mobileNumber);
        logger.info("Entered mobile number from testdata: " + mobileNumber);
        ExtentReportManager.logInfo("Entered mobile number: " + mobileNumber);
    }
    
    @When("I tap on continue button")
    public void iTapOnContinueButton() {
        if (alreadyLoggedIn) {
            logger.info("⏭ Skipping: User already logged in");
            ExtentReportManager.logInfo("Skipped - User already logged in");
            return;
        }
        loginPage.clickContinueButton();
        logger.info("Tapped on continue button");
        ExtentReportManager.logInfo("Tapped on continue button");
    }
    
    @When("I enter OTP {string}")
    public void iEnterOTP(String otp) {
        loginPage.enterOTP(otp);
        logger.info("Entered OTP");
        ExtentReportManager.logInfo("Entered OTP: " + otp);
    }
    
    @When("I enter OTP from config")
    public void iEnterOTPFromConfig() {
        if (alreadyLoggedIn) {
            logger.info("⏭ Skipping: User already logged in");
            ExtentReportManager.logInfo("Skipped - User already logged in");
            return;
        }
        String otp = PropertyReader.getTestDataProperty("otp");
        loginPage.enterOTP(otp);
        logger.info("Entered OTP from testdata");
        ExtentReportManager.logInfo("Entered OTP from testdata");
    }
    
    @When("I tap on verify button")
    public void iTapOnVerifyButton() {
        if (alreadyLoggedIn) {
            logger.info("⏭ Skipping: User already logged in");
            ExtentReportManager.logInfo("Skipped - User already logged in");
            return;
        }
        try {
            loginPage.clickVerifyButton();
            logger.info("Tapped on verify button");
            ExtentReportManager.logPass("Verify button found and tapped — PIN screen loaded");
        } catch (RuntimeException e) {
            String msg = "Verify button feasibility check FAILED: " + e.getMessage();
            logger.error(msg);
            ExtentReportManager.logFail(msg);
            throw e; // re-throw to stop scenario execution
        }
    }
    
    @When("I enter PIN {string}")
    public void iEnterPIN(String pin) {
        loginPage.enterPIN(pin);
        logger.info("Entered PIN");
        ExtentReportManager.logInfo("Entered PIN");
    }
    
    @When("I enter PIN from config")
    public void iEnterPINFromConfig() {
        if (alreadyLoggedIn) {
            logger.info("⏭ Skipping: User already logged in");
            ExtentReportManager.logInfo("Skipped - User already logged in");
            return;
        }
        String pin = PropertyReader.getTestDataProperty("pin");
        loginPage.enterPIN(pin);
        logger.info("Entered PIN from testdata");
        ExtentReportManager.logInfo("Entered PIN from testdata");
    }
    
    @When("I login with valid credentials from config")
    public void iLoginWithValidCredentialsFromConfig() {
        if (alreadyLoggedIn) {
            logger.info("⏭ Skipping login: User already logged in");
            ExtentReportManager.logInfo("Skipped login - User already logged in");
            return;
        }
        loginPage.loginWithConfigCredentials();
        logger.info("Logged in with valid credentials from config");
        ExtentReportManager.logInfo("Logged in with EcoCash credentials from config");
    }
    
    @Then("I should see the home page")
    public void iShouldSeeTheHomePage() {
        if (alreadyLoggedIn) {
            logger.info("✓ Already on home page - User was already logged in");
            ExtentReportManager.logPass("Already on home page - User was already logged in");
            return;
        }
        try {
            Thread.sleep(5000); // Wait for home page to load
        } catch (InterruptedException e) {
            logger.error("Thread interrupted: " + e.getMessage());
        }
        
        boolean homePageDisplayed = homePage.isHomePageDisplayed();
        
        Assert.assertTrue(homePageDisplayed, "Home page is not displayed");
        logger.info("Home page is displayed");
        ExtentReportManager.logPass("EcoCash home page is displayed successfully");
    }
    
    @Then("I should see welcome message")
    public void iShouldSeeWelcomeMessage() {
        String welcomeMessage = homePage.getWelcomeMessage();
        
        Assert.assertNotNull(welcomeMessage, "Welcome message is null");
        Assert.assertFalse(welcomeMessage.isEmpty(), "Welcome message is empty");
        
        logger.info("Welcome message displayed: " + welcomeMessage);
        ExtentReportManager.logPass("Welcome message displayed: " + welcomeMessage);
    }
    
    @When("I dismiss biometric authentication popup if displayed")
    public void iDismissBiometricAuthenticationPopup() {
        logger.info("Step: Dismissing biometric authentication popup if displayed");
        ExtentReportManager.logInfo("Checking for biometric authentication popup");
        homePage.dismissBiometricPopup();
        ExtentReportManager.logPass("Biometric popup handling completed");
    }
    
    @When("I click on screen to dismiss any drawer")
    public void iClickOnScreenToDismissAnyDrawer() {
        logger.info("Step: Clicking on screen to dismiss any drawer or popup");
        ExtentReportManager.logInfo("Clicking on screen to dismiss biometric drawer");
        homePage.clickOnScreen();
        ExtentReportManager.logPass("Clicked on screen successfully");
    }
    
    @When("I press back button to dismiss biometric drawer")
    public void iPressBackButtonToDismissBiometricDrawer() {
        logger.info("Step: Pressing back button to dismiss biometric drawer (if still open)");
        ExtentReportManager.logInfo("Smart back press — only if biometric drawer is still visible");
        boolean pressed = homePage.pressBackOnlyIfBiometricDrawerOpen();
        if (pressed) {
            ExtentReportManager.logPass("Back button pressed — biometric drawer dismissed");
        } else {
            ExtentReportManager.logInfo("Back press skipped — no biometric drawer detected");
        }
    }

    @When("I navigate back to home screen")
    public void iNavigateBackToHomeScreen() {
        logger.info("Navigating back to home screen...");
        ExtentReportManager.logInfo("Pressing back to return to home screen");
        // Press back up to 7 times with a generous wait — real device animations are slow.
        // The loop stops as soon as a home screen element is detected.
        for (int i = 0; i < 7; i++) {
            try {
                if (homePage.isAlreadyLoggedIn()) {
                    logger.info("Home screen detected after " + i + " back press(es)");
                    break;
                }
                homePage.pressBackButton();
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.warn("Back press " + (i + 1) + " issue: " + e.getMessage());
            }
        }
        ExtentReportManager.logPass("Navigation to home screen completed");
    }
    
    @When("I tap on profile image area")
    public void iTapOnProfileImageArea() {
        logger.info("Step: Tapping on profile image area");
        ExtentReportManager.logInfo("Tapping at profile image location (top-left corner)");
        homePage.tapProfileImageArea();
        ExtentReportManager.logPass("Tapped on profile image area successfully");
    }
    
    @Then("I should see welcome message with username on home page")
    public void iShouldSeeWelcomeMessageWithUsername() {
        logger.info("Step: Verifying welcome message with username on home page");
        ExtentReportManager.logInfo("Verifying welcome message is displayed with username");
        
        try {
            Thread.sleep(2000); // Wait for home page to fully load
            homePage.verifyWelcomeMessage();
            String welcomeText = homePage.getWelcomeMessageText();
            logger.info("Welcome message verified: " + welcomeText);
            ExtentReportManager.logPass("Welcome message displayed successfully: " + welcomeText);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted: " + e.getMessage());
            ExtentReportManager.logFail("Failed to verify welcome message: " + e.getMessage());
            throw new RuntimeException("Thread interrupted during verification", e);
        } catch (AssertionError e) {
            ExtentReportManager.logFail("Welcome message verification failed: " + e.getMessage());
            throw e;
        }
    }
    
    @Then("I should see error message")
    public void iShouldSeeErrorMessage() {
        boolean errorDisplayed = loginPage.isErrorMessageDisplayed();
        
        Assert.assertTrue(errorDisplayed, "Error message is not displayed");
        logger.info("Error message is displayed");
        ExtentReportManager.logPass("Error message is displayed for invalid credentials");
    }
    
    @Then("I should see the login page")
    public void iShouldSeeTheLoginPage() {
        loginPage.waitForLoginPage();
        logger.info("Login page is displayed");
        ExtentReportManager.logPass("Navigated back to login page");
    }
    
}
