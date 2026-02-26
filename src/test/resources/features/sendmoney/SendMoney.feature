@smoke @sendmoney
Feature: EcoCash Send Money (P2P)
  As an EcoCash user
  I want to send money to another EcoCash subscriber
  So that I can transfer funds peer-to-peer

  Background:
    Given I should see the home page

  # ------------------------------------------------------------------
  # Positive Scenarios
  # ------------------------------------------------------------------

  @smoke @positive @p2p
  Scenario Outline: Successful P2P money transfer
    When I tap on Send Money on Pay page
    And I tap on Send from the bottom drawer
    And I search for mobile number "789124558"
    And I wait for contact "789124558" to appear and select it
    And I enter transfer amount "<amount>"
    And I select currency "<currency>"
    And I tap on Continue
    Then I should see the PIN verification page
    When I enter transaction PIN from config
    Then I should see a transaction chat bubble
    When I tap on the recent chat bubble
    Then I should see transaction details page with receipt
    And I verify transaction outcome on details page
    When I navigate back to home screen from details
    Then I should see home screen

    Examples:
      | currency | amount |
      | USD      | 1.20   |
      | ZWG      | 1.50   |
