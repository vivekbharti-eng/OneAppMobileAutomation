@smoke
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

