@smoke @regression @logout
Feature: EcoCash Logout Functionality
  As a EcoCash user
  I want to logout from the application
  So that I can secure my account

  Background:
    Given the app is launched
    When I click on Agree and Continue
    And I skip login if already logged in
    And I login with valid credentials from config
    Then I should see the home page
    When I dismiss biometric authentication popup if displayed
    And I press back button to dismiss biometric drawer
    And I tap on profile image area

  @positive @logout @profile
  Scenario: User logout from profile menu
    When I scroll to bottom of side menu
    And I click on logout option
    And I verify logout confirmation popup is displayed
    And I click on logout anyway button
    Then I should see the login page

  @positive @logout @verify
  Scenario: User logout and cancel
    When I scroll to bottom of side menu
    And I click on logout option
    And I verify logout confirmation popup is displayed
    And I click on cancel button on popup
    Then I should still be on home page
