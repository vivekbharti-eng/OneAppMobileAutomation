@smoke @regression
Feature: EcoCash Login Functionality
  As a EcoCash user
  I want to login to the EcoCash mobile application
  So that I can access my wallet and perform transactions

  Background:
    Given the app is launched
    When I click on Agree and Continue

  @smoke @positive @login
  Scenario: Successful login with valid credentials from config
    When I enter country code from config
    And I enter mobile number from config
    And I tap on continue button
    And I enter OTP from config
    And I tap on verify button
    And I enter PIN from config
    And I Tap to Cancel button biometric authentication popup if displayed
    Then I should see the home page

  @negative @login
  Scenario Outline: Login with invalid mobile number
    When I enter country code "+263"
    And I enter mobile number "<mobile_number>"
    And I tap on continue button
    Then I should see error message

    Examples:
      | mobile_number |
      | 123456789     |
      | 000000000     |
      | 999999999     |

