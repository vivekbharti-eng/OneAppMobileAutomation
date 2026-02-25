package com.automation.stepdefinitions.logout;

import com.automation.pages.HomePage;
import com.automation.pages.LoginPage;
import com.automation.reports.ExtentReportManager;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * Step definitions for EcoCash Logout feature
 * Contains step implementations for EcoCash logout scenarios
 */
public class LogoutSteps {
    
    private static final Logger logger = LogManager.getLogger(LogoutSteps.class);
    private HomePage homePage;
    private LoginPage loginPage;
    
    public LogoutSteps() {
        homePage = new HomePage();
        loginPage = new LoginPage();
    }
    
    @When("I click on profile picture")
    public void iClickOnProfilePicture() {
        logger.info("Step: Clicking on profile picture");
        ExtentReportManager.logInfo("Clicking on profile picture to open side menu");
        homePage.clickProfilePicture();
        ExtentReportManager.logPass("Successfully clicked on profile picture");
    }
    
    @When("I scroll to bottom of side menu")
    public void iScrollToBottomOfSideMenu() {
        logger.info("Step: Scrolling to bottom of side menu");
        ExtentReportManager.logInfo("Scrolling to bottom of side menu to find logout option");
        homePage.scrollToBottomOfSideMenu();
        ExtentReportManager.logPass("Successfully scrolled to bottom of side menu");
    }
    
    @When("I click on logout option")
    public void iClickOnLogoutOption() {
        logger.info("Step: Clicking on logout option");
        ExtentReportManager.logInfo("Clicking on logout option in side menu");
        homePage.clickLogoutOption();
        ExtentReportManager.logPass("Successfully clicked on logout option");
    }
    
    @When("I verify logout confirmation popup is displayed")
    public void iVerifyLogoutConfirmationPopupIsDisplayed() {
        logger.info("Step: Verifying logout confirmation popup");
        ExtentReportManager.logInfo("Verifying logout confirmation popup is displayed");
        homePage.verifyLogoutPopupDisplayed();
        ExtentReportManager.logPass("Logout confirmation popup is displayed");
    }
    
    @When("I click on logout anyway button")
    public void iClickOnLogoutAnywayButton() {
        logger.info("Step: Clicking on logout anyway button");
        ExtentReportManager.logInfo("Clicking on 'Logout Anyway' button to confirm logout");
        homePage.clickLogoutAnywayButton();
        ExtentReportManager.logPass("Successfully clicked on logout anyway button");
    }
    
    @When("I click on cancel button on popup")
    public void iClickOnCancelButtonOnPopup() {
        logger.info("Step: Clicking on cancel button");
        ExtentReportManager.logInfo("Clicking on 'Cancel' button to dismiss logout popup");
        homePage.clickCancelButton();
        ExtentReportManager.logPass("Successfully clicked on cancel button");
    }
    
    @Then("I should still be on home page")
    public void iShouldStillBeOnHomePage() {
        logger.info("Step: Verifying user is still on home page after canceling logout");
        ExtentReportManager.logInfo("Verifying home page is still displayed");
        
        try {
            Thread.sleep(1000); // Wait for popup to close
            homePage.verifyHomePageDisplayed();
            logger.info("Home page is still displayed after canceling logout");
            ExtentReportManager.logPass("User successfully canceled logout and remained on home page");
        } catch (InterruptedException e) {
            logger.error("Thread interrupted: " + e.getMessage());
            ExtentReportManager.logFail("Failed to verify home page: " + e.getMessage());
        }
    }
}
