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
    
    @When("I enter country code from config")
    public void iEnterCountryCodeFromConfig() {
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
        String mobileNumber = PropertyReader.getTestDataProperty("mobile.number");
        loginPage.enterMobileNumber(mobileNumber);
        logger.info("Entered mobile number from testdata: " + mobileNumber);
        ExtentReportManager.logInfo("Entered mobile number: " + mobileNumber);
    }
    
    @When("I tap on continue button")
    public void iTapOnContinueButton() {
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
        String otp = PropertyReader.getTestDataProperty("otp");
        loginPage.enterOTP(otp);
        logger.info("Entered OTP from testdata");
        ExtentReportManager.logInfo("Entered OTP from testdata");
    }
    
    @When("I tap on verify button")
    public void iTapOnVerifyButton() {
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
        String pin = PropertyReader.getTestDataProperty("pin");
        loginPage.enterPIN(pin);
        logger.info("Entered PIN from testdata");
        ExtentReportManager.logInfo("Entered PIN from testdata");
    }
    
    @When("I login with valid credentials from config")
    public void iLoginWithValidCredentialsFromConfig() {
        loginPage.loginWithConfigCredentials();
        logger.info("Logged in with valid credentials from config");
        ExtentReportManager.logInfo("Logged in with EcoCash credentials from config");
    }
    
    @Then("I should see the home page")
    public void iShouldSeeTheHomePage() {
      /*  try {
            // If biometric bottom sheet is still open after cancel, press back to close it
            homePage.pressBackOnlyIfBiometricDrawerOpen();
            Thread.sleep(3000); // Wait for home page to load after any drawer is dismissed
        } catch (InterruptedException e) {
            logger.error("Thread interrupted: " + e.getMessage());
        }*/

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
        loginPage.handleBiometricPopup();
        ExtentReportManager.logPass("Biometric popup handling completed");
    }

    @When("I Tap to Cancel button biometric authentication popup if displayed")
    public void iTapToCancelBiometricPopup() {
        if ("virtual".equalsIgnoreCase(PropertyReader.getExecutionTarget())) {
            logger.info("Step: Virtual device — skipping biometric popup handling");
            ExtentReportManager.logInfo("Virtual device: biometric popup step skipped");
            return;
        }
        logger.info("Step: Waiting for Enable Biometric bottom sheet and clicking Cancel");
        ExtentReportManager.logInfo("Waiting for biometric bottom sheet then tapping Cancel");
        loginPage.handleBiometricPopup();
        ExtentReportManager.logPass("Biometric Cancel tapped (or popup not shown)");
    }
    
    @When("I click on screen to dismiss any drawer")
    public void iClickOnScreenToDismissAnyDrawer() {
        logger.info("Step: Clicking on screen to dismiss any drawer or popup");
        ExtentReportManager.logInfo("Clicking on screen to dismiss biometric drawer");
        homePage.clickOnScreen();
        ExtentReportManager.logPass("Clicked on screen successfully");
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
    
    @Then("I should see the login page")
    public void iShouldSeeTheLoginPage() {
        loginPage.waitForLoginPage();
        logger.info("Login page is displayed");
        ExtentReportManager.logPass("Navigated back to login page");
    }
    
}
