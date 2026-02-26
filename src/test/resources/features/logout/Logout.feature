@smoke @logout
Feature: EcoCash Logout Functionality
  As a EcoCash user
  I want to logout from the application
  So that I can secure my account

  Background:
    Given I tap on profile image area

  @smoke @positive @logout @profile
  Scenario: User logout from profile menu
    When I scroll to bottom of side menu
    And I click on logout option
    And I verify logout confirmation popup is displayed
    And I click on logout anyway button
    Then I should see the login page
