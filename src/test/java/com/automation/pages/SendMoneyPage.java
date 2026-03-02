package com.automation.pages;

import com.automation.utils.LocatorUtils;
import com.automation.utils.WaitHelper;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

/**
 * Send Money (P2P) page object
 * Mirrors the exact EcoCash app flow:
 *  1. Tap Send Money on Home / Pay page
 *  2. Tap Send from bottom drawer
 *  3. Search mobile number → wait for contact → select contact
 *  4. Select currency (USD / ZWG)
 *  5. Enter amount
 *  6. Tap Continue
 *  7. Verify PIN page is displayed
 *  8. Enter PIN
 *  9. Verify confirmation bottom drawer
 * 10. Verify transaction details page (Success / Fail)
 */
public class SendMoneyPage extends BasePage {

    // -----------------------------------------------------------------------
    // Locator keys
    // -----------------------------------------------------------------------
    private static final String SEND_MONEY_BUTTON   = "sendmoney.button.xpath";
    private static final String DRAWER_SEND_OPTION  = "sendmoney.drawer.send.xpath";
    private static final String SEARCH_FIELD        = "sendmoney.search.field.xpath";
    private static final String NO_CONTACT_MESSAGE  = "sendmoney.no.contact.xpath";
    private static final String CURRENCY_USD        = "sendmoney.currency.usd.xpath";
    private static final String CURRENCY_ZWG        = "sendmoney.currency.zwg.xpath";
    private static final String AMOUNT_FIELD        = "sendmoney.amount.field.xpath";
    private static final String CONTINUE_BUTTON     = "sendmoney.continue.button.xpath";
    private static final String PIN_PAGE            = "sendmoney.pin.page.xpath";
    private static final String PIN_FIELD           = "sendmoney.pin.field.xpath";
    private static final String PIN_SUBMIT          = "sendmoney.pin.submit.xpath";
    private static final String CONFIRMATION_DRAWER = "sendmoney.confirmation.drawer.xpath";
    private static final String SUCCESS_MESSAGE     = "sendmoney.success.xpath";
    private static final String FAILURE_MESSAGE     = "sendmoney.failure.xpath";
    private static final String ERROR_MESSAGE       = "sendmoney.error.xpath";
    private static final String INSUFFICIENT_FUNDS  = "sendmoney.insufficient.funds.xpath";
    private static final String CANCEL_BUTTON       = "sendmoney.cancel.button.xpath";
    private static final String CHAT_BUBBLE          = "sendmoney.chat.bubble.xpath";
    private static final String DETAILS_ICON         = "sendmoney.details.icon.xpath";
    private static final String DETAILS_PAGE         = "sendmoney.details.page.xpath";
    private static final String SELF_TRANSFER_TOAST  = "sendmoney.self.toast.xpath";
    private static final String SEARCH_CLEAR_BUTTON  = "sendmoney.search.clear.btn.xpath";

    // -----------------------------------------------------------------------
    // Private helper — clear field and type text (reliable on real device)
    // -----------------------------------------------------------------------
    private void clearAndType(By locator, String text) {
        try {
            WebElement element = WaitHelper.waitForElementToBeVisible(locator, 15);
            element.click();
            sleep(400);
            element.clear();
            sleep(200);
            element.sendKeys(text);
            logger.info("Typed '" + text + "' into " + locator);
        } catch (Exception e) {
            logger.error("clearAndType failed for " + locator + ": " + e.getMessage());
            throw new RuntimeException("Failed to type text: " + text, e);
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // -----------------------------------------------------------------------
    // Terms & Conditions popup handler (appears on virtual device first run)
    // -----------------------------------------------------------------------

    public void dismissTermsAndConditionsIfPresent() {
        try {
            By agreeBtn = LocatorUtils.getLocator("login.agree.continue.button.xpath");
            WebElement btn = WaitHelper.waitForElementToBeVisible(agreeBtn, 4);
            if (btn != null && btn.isDisplayed()) {
                btn.click();
                sleep(2000);
                logger.info("Dismissed Terms & Conditions popup (Agree & Continue)");
            }
        } catch (Exception ignored) {
            logger.debug("No Terms & Conditions popup detected — continuing");
        }
    }

    // -----------------------------------------------------------------------
    // Step 1 — Tap Send Money tile on Home / Pay page
    // -----------------------------------------------------------------------

    public void tapSendMoneyOnPayPage() {
        try {
            By locator = LocatorUtils.getLocator(SEND_MONEY_BUTTON);

            // Quick check — is Send Money already visible?
            if (!isElementDisplayed(locator, 5)) {
                logger.warn("'Send Money' not immediately visible — attempting to navigate to Pay tab...");
                // Try clicking Pay tab in bottom navigation bar
                By payTab = By.xpath(
                    "//*[@content-desc='Pay' or @text='Pay' or @resource-id='pay_tab']" +
                    " | //android.widget.TextView[@text='Pay']" +
                    " | //*[contains(@content-desc,'Pay') and @clickable='true']"
                );
                try {
                    if (isElementDisplayed(payTab, 4)) {
                        click(payTab);
                        logger.info("Clicked Pay tab — waiting for Send Money to appear...");
                        sleep(2000);
                    } else {
                        // Fallback: try clicking the Home tab first to reset navigation state
                        By homeTab = By.xpath(
                            "//*[@content-desc='Home' or @text='Home' or @resource-id='home_tab']" +
                            " | //android.widget.TextView[@text='Home']"
                        );
                        if (isElementDisplayed(homeTab, 3)) {
                            click(homeTab);
                            sleep(1500);
                            logger.info("Clicked Home tab to reset navigation");
                        }
                    }
                } catch (Exception navEx) {
                    logger.warn("Tab navigation attempt failed: " + navEx.getMessage());
                }
            }

            // Now wait for and tap Send Money
            waitForElement(locator, 20);
            click(locator);
            sleep(2000); // wait for bottom drawer animation
            dismissTermsAndConditionsIfPresent(); // handle T&C on virtual device
            logger.info("Tapped Send Money on Pay page");
        } catch (Exception e) {
            logger.error("Failed to tap Send Money: " + e.getMessage());
            throw new RuntimeException("Send Money button not found on Pay page", e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 2 — Tap Send from bottom drawer
    // -----------------------------------------------------------------------

    public void tapSendFromBottomDrawer() {
        try {
            sleep(2000); // wait for drawer to fully render
            dismissTermsAndConditionsIfPresent(); // T&C may appear before drawer
            By locator = LocatorUtils.getLocator(DRAWER_SEND_OPTION);
            waitForElement(locator, 15);
            click(locator);
            sleep(2000); // wait for contact search screen
            dismissTermsAndConditionsIfPresent(); // T&C may appear after tapping Send
            logger.info("Tapped Send from bottom drawer");
        } catch (Exception e) {
            logger.error("Failed to tap Send from drawer: " + e.getMessage());
            throw new RuntimeException("Send option not found in bottom drawer", e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 3 — Search mobile number & select contact
    // -----------------------------------------------------------------------

    public void searchMobileNumber(String mobileNumber) {
        try {
            sleep(1500); // wait for search screen
            By locator = LocatorUtils.getLocator(SEARCH_FIELD);
            // EcoCash renders the search field as ImageView (React Native TextInput).
            // sendKeys fires React Native onChange twice (double-type bug).
            // Fix: click to focus, clear via UiAutomator2 setText, then use mobile:type.
            WebElement searchField = waitForElement(locator, 15);
            searchField.click();
            sleep(500);
            // Clear any existing text using UiAutomator2 setText before typing
            try {
                ((AndroidDriver) driver).executeScript("mobile: clearTextField",
                        java.util.Map.of("elementId", searchField.getAttribute("id")));
            } catch (Exception ignored) {
                try { searchField.clear(); } catch (Exception ignored2) {}
            }
            sleep(300);
            // Use mobile:type — sends raw key events, avoids React Native double-onChange
            ((AndroidDriver) driver).executeScript("mobile: type",
                    java.util.Map.of("text", mobileNumber));
            sleep(500);
            // Dismiss keyboard without adding any extra character
            try { ((AndroidDriver) driver).hideKeyboard(); } catch (Exception ignored) {}
            sleep(2500); // wait for search results list to load
            logger.info("Searched for mobile number: " + mobileNumber);
        } catch (Exception e) {
            logger.error("Failed to search mobile number: " + e.getMessage());
            throw new RuntimeException("Search field not found", e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 3b — Tap a contact result without waiting for the keyboard
    //           (used for own-number negative test: tap triggers error, not amount screen)
    // -----------------------------------------------------------------------

    public void tapContactByNumber(String number) {
        try {
            WebElement contact = null;
            // Strategy 1: UIAutomator
            try {
                By uiAuto = AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceIdMatches(\"contact_item_.*\").descriptionContains(\"" + number + "\")"
                );
                contact = WaitHelper.waitForElementToBeVisible(uiAuto, 8);
                logger.info("Own-number contact found via UIAutomator: " + number);
            } catch (Exception ignored) {
                logger.warn("UIAutomator own-number search failed, trying XPath");
            }
            // Strategy 2: XPath — clickable elements containing the number (exclude search field)
            if (contact == null) {
                By xpath = By.xpath((
                    "//*[starts-with(@resource-id,'contact_item_') and contains(@content-desc,'%s')] | " +
                        "//*[@clickable='true' and contains(@content-desc,'%s') and not(@resource-id='search_textfield')] | " +
                        "//*[@clickable='true' and contains(@text,'%s') and not(@resource-id='search_textfield')]").formatted(
                    number, number, number));
                contact = WaitHelper.waitForElementToBeVisible(xpath, 8);
                logger.info("Own-number contact found via XPath: " + number);
            }
            int cx = contact.getLocation().getX() + contact.getSize().getWidth()  / 2;
            int cy = contact.getLocation().getY() + contact.getSize().getHeight() / 2;
            logger.info("Tapping own-number contact at (" + cx + "," + cy + ")");
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "ownFinger");
            Sequence tap = new Sequence(finger, 0);
            tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), cx, cy));
            tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Arrays.asList(tap));
            sleep(2500); // wait for toast / error to appear
        } catch (Exception e) {
            logger.warn("Own-number contact tap failed (may be expected if no result shown): " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Step 3c — Capture self-transfer error toast/message and log to report
    // -----------------------------------------------------------------------

    public String captureAndLogSelfTransferError() {
        String errorText = "(error message not captured)";
        try {
            By locator = LocatorUtils.getLocator(SELF_TRANSFER_TOAST);
            // Poll up to 6 s — toasts are short-lived
            long deadline = System.currentTimeMillis() + 6_000;
            WebElement toastEl = null;
            while (System.currentTimeMillis() < deadline) {
                java.util.List<WebElement> els = driver.findElements(locator);
                if (!els.isEmpty()) { toastEl = els.getFirst(); break; }
                sleep(300);
            }
            if (toastEl != null) {
                // Try content-desc first (React Native), fall back to text attribute
                String desc = toastEl.getAttribute("content-desc");
                String txt  = toastEl.getAttribute("text");
                errorText = (desc != null && !desc.isBlank()) ? desc
                          : (txt  != null && !txt.isBlank())  ? txt
                          : "(error element found but text blank)";
                logger.info("Self-transfer error captured: " + errorText);
            } else {
                // Fallback — grab any visible text that looks like an error message
                By fallback = By.xpath("//*[contains(@text,'not') or contains(@text,'invalid') or contains(@text,'error') " +
                    "or contains(@content-desc,'not') or contains(@content-desc,'invalid')]");
                java.util.List<WebElement> fbEls = driver.findElements(fallback);
                if (!fbEls.isEmpty()) {
                    String fbText = fbEls.getFirst().getAttribute("content-desc");
                    if (fbText == null || fbText.isBlank()) fbText = fbEls.getFirst().getAttribute("text");
                    if (fbText != null && !fbText.isBlank()) errorText = fbText;
                }
                logger.warn("Toast not found within 6 s. Captured text: " + errorText);
            }
        } catch (Exception e) {
            logger.warn("Could not capture self-transfer error toast: " + e.getMessage());
        }
        return errorText;
    }

    // -----------------------------------------------------------------------
    // Step 3d — Clear the search field and type a new number
    //           Taps the X/close button inside the field, then retypes
    // -----------------------------------------------------------------------

    public void clearSearchAndSearchFor(String newNumber) {
        try {
            // ── Guard: if own-number tap opened the amount/contact screen, navigate back ──
            By searchLoc0 = LocatorUtils.getLocator(SEARCH_FIELD);
            if (driver.findElements(searchLoc0).isEmpty()) {
                logger.warn("Search field not visible — likely navigated away after own-number tap; going back");
                driver.navigate().back();
                sleep(1500);
                if (driver.findElements(searchLoc0).isEmpty()) {
                    logger.warn("Search field still not visible after back press — trying once more");
                    driver.navigate().back();
                    sleep(1500);
                }
            }

            // Tap the inline X / clear button (resource-id='close') if it exists
            By clearBtn = LocatorUtils.getLocator(SEARCH_CLEAR_BUTTON);
            java.util.List<WebElement> btns = driver.findElements(clearBtn);
            if (!btns.isEmpty()) {
                btns.getFirst().click();
                sleep(800);
                logger.info("Tapped search-field clear (X) button");
            } else {
                // Fallback: click the EditText, select all, delete
                By searchLoc = LocatorUtils.getLocator(SEARCH_FIELD);
                WebElement sf = waitForElement(searchLoc, 8);
                sf.click();
                sleep(400);
                sf.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
                sleep(200);
                sf.sendKeys(org.openqa.selenium.Keys.DELETE);
                sleep(400);
                logger.info("Cleared search field via CTRL+A + DELETE");
            }
            // Now type the new search number — use mobile:type to avoid React Native double-onChange
            By searchLoc = LocatorUtils.getLocator(SEARCH_FIELD);
            WebElement sf = waitForElement(searchLoc, 8);
            sf.click();
            sleep(400);
            try {
                ((AndroidDriver) driver).executeScript("mobile: clearTextField",
                        java.util.Map.of("elementId", sf.getAttribute("id")));
            } catch (Exception ignored) {
                try { sf.clear(); } catch (Exception ignored2) {}
            }
            sleep(300);
            ((AndroidDriver) driver).executeScript("mobile: type",
                    java.util.Map.of("text", newNumber));
            sleep(500);
            try { ((AndroidDriver) driver).hideKeyboard(); } catch (Exception ignored) {}
            sleep(2500);
            logger.info("Cleared search and typed new number: " + newNumber);
        } catch (Exception e) {
            logger.error("clearSearchAndSearchFor failed: " + e.getMessage());
            throw new RuntimeException("Failed to clear search and retype: " + newNumber, e);
        }
    }

    public void waitForContactAndSelect(String mobileNumber) {
        try {
            // Step 0: Dismiss keyboard before searching.
            // Keep timeouts SHORT — long XPath waits stress UiAutomator2 and cause it to crash.
            try { ((AndroidDriver) driver).hideKeyboard(); } catch (Exception ignored) {}
            sleep(2000); // allow contact list to render after keyboard hides

            logger.info("Searching for contact result element for: " + mobileNumber);
            WebElement contact = null;

            // Pass 1: broadest search — any element containing the number (no @clickable filter).
            // React Native list items often don't have @clickable='true' set on the row itself.
            By xpath1 = By.xpath(
                "//*[contains(@content-desc,'" + mobileNumber + "') and not(contains(@resource-id,'search_textfield')) and not(contains(@resource-id,'input'))]" +
                " | //*[contains(@text,'" + mobileNumber + "') and not(contains(@resource-id,'search_textfield')) and not(contains(@resource-id,'input'))]"
            );
            try {
                // Short timeout (3s) — keeps UiAutomator2 stable between attempts
                contact = WaitHelper.waitForElementToBeVisible(xpath1, 3);
                logger.info("Contact found via broad XPath (pass 1): " + mobileNumber);
            } catch (Exception e1) {
                logger.warn("Pass 1 XPath not found. Waiting 2s for list to settle...");
                sleep(2000);
            }

            // Pass 2: try again after extra wait — list may still be loading
            if (contact == null) {
                try {
                    contact = WaitHelper.waitForElementToBeVisible(xpath1, 3);
                    logger.info("Contact found via XPath pass 2 (after extra wait): " + mobileNumber);
                } catch (Exception e2) {
                    logger.warn("Pass 2 XPath still not found.");
                }
            }

            // Pass 3: coordinate tap fallback — use Appium JS 'mobile: tap' (survives UA2 instability)
            // Tap at the first result row position: x=540 centre, y tries 500→600→700→800
            if (contact == null) {
                logger.warn("XPath contact search failed for {}. Using coordinate tap fallback.", mobileNumber);
                int[] yPositions = {500, 600, 700, 800};
                boolean tapped = false;
                for (int yPos : yPositions) {
                    try {
                        // 'mobile: tap' is more resilient than PointerInput when UA2 is under stress
                        ((AndroidDriver) driver).executeScript("mobile: tap",
                            java.util.Map.of("x", 540, "y", yPos));
                        logger.info("Coordinate tap dispatched at (540, {}) for contact result", yPos);
                        sleep(1500);
                        // Check if keyboard appeared — means we tapped the contact correctly
                        try {
                            if (((AndroidDriver) driver).isKeyboardShown()) {
                                logger.info("Keyboard appeared after tap at y={} — contact selected!", yPos);
                                tapped = true;
                                break;
                            }
                        } catch (Exception ignored) {}
                    } catch (Exception tapEx) {
                        logger.warn("Coordinate tap at y={} failed: {}", yPos, tapEx.getMessage());
                    }
                }
                if (!tapped) {
                    // Last resort: PointerInput tap at y=600
                    try {
                        logger.info("Attempting PointerInput tap at (540, 600) as last resort...");
                        PointerInput f = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                        Sequence s = new Sequence(f, 0);
                        s.addAction(f.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), 540, 600));
                        s.addAction(f.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                        s.addAction(f.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                        driver.perform(Arrays.asList(s));
                        logger.info("PointerInput tap dispatched at (540, 600)");
                    } catch (Exception pointerEx) {
                        logger.warn("PointerInput tap also failed: {}", pointerEx.getMessage());
                    }
                }
                sleep(2000);
                logger.info("Coordinate fallback complete for: " + mobileNumber);
                return; // proceed — amount entry will reveal if contact was tapped correctly
            }

            // Coordinate-based tap on found element (React Native touch targets need this)
            int cx = contact.getLocation().getX() + contact.getSize().getWidth() / 2;
            int cy = contact.getLocation().getY() + contact.getSize().getHeight() / 2;
            if (cy < 300) { cy = 600; } // guard: don't tap inside search bar
            logger.info("Tapping contact element at ({}, {})", cx, cy);
            try {
                ((AndroidDriver) driver).executeScript("mobile: tap", java.util.Map.of("x", cx, "y", cy));
                logger.info("Contact tapped via mobile:tap at ({}, {})", cx, cy);
            } catch (Exception tapEx) {
                logger.warn("mobile:tap failed ({}), trying PointerInput...", tapEx.getMessage());
                PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
                Sequence tap = new Sequence(finger, 0);
                tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), cx, cy));
                tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
                tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
                driver.perform(Arrays.asList(tap));
                logger.info("Contact tapped via PointerInput at ({}, {})", cx, cy);
            }
            sleep(500);
            logger.info("Tapped search result for: " + mobileNumber);
        } catch (Exception e) {
            logger.error("Contact search result not found for: " + mobileNumber);
            throw new RuntimeException("Contact '" + mobileNumber + "' not visible in search results after typing", e);
        }
    }

    public boolean isNoContactMessageDisplayed() {
        try {
            By locator = LocatorUtils.getLocator(NO_CONTACT_MESSAGE);
            waitForElement(locator, 10);
            boolean displayed = isElementDisplayed(locator);
            logger.info("No contact found message displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("No contact message not found: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Step 4 — Select currency (toggle on the same amount-entry screen)
    // -----------------------------------------------------------------------

    public void selectCurrency(String currency) {
        try {
            sleep(1500); // wait for amount screen to fully render after contact selection

            // Check current currency from the amount EditText hint attribute.
            // Hint format: "Currency\nYou are paying\nUSD\n0" or "...ZWG..."
            // If the target currency is already active, skip clicking to avoid disrupting the UI.
            try {
                By amountField = LocatorUtils.getLocator(AMOUNT_FIELD);
                WebElement amtEl = WaitHelper.waitForElementToBeVisible(amountField, 5);
                String hint = amtEl.getAttribute("hint");
                if (hint != null && hint.toUpperCase().contains(currency.toUpperCase())) {
                    logger.info("Currency '" + currency + "' already selected (hint=" + hint.replace("\n", "|") + ") — skipping click");
                    return;
                }
            } catch (Exception ignored) {
                logger.warn("Could not read amount field hint to detect current currency — will click toggle");
            }

            // Click the currency toggle button
            By locator = currency.equalsIgnoreCase("ZWG")
                    ? LocatorUtils.getLocator(CURRENCY_ZWG)
                    : LocatorUtils.getLocator(CURRENCY_USD);
            waitForElement(locator, 10);
            click(locator);
            sleep(1000); // wait for UI to reflect the selection
            logger.info("Selected currency: " + currency);
        } catch (Exception e) {
            logger.error("Failed to select currency " + currency + ": " + e.getMessage());
            throw new RuntimeException("Currency option '" + currency + "' not found", e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 5 — Enter amount  (shown immediately after contact selection)
    // -----------------------------------------------------------------------

    public void enterAmount(String amount) {
        try {
            logger.info("Looking for amount field...");

            // Locate the field by exact resource-id XPath (confirmed from page source).
            By locator = LocatorUtils.getLocator(AMOUNT_FIELD);
            WebElement amtField = WaitHelper.waitForElementToBeVisible(locator, 15);
            logger.info("Amount field found — keyboard already open, typing now");

            // Keyboard is already shown (confirmed in waitForContactAndSelect).
            // Type directly — React Native onChangeText fires per character.
            amtField.clear();
            amtField.sendKeys(amount);
            logger.info("Typed '" + amount + "' into amount field");

            // Poll Continue until enabled (up to 10 s) to confirm amount accepted.
            By continueLocator = LocatorUtils.getLocator(CONTINUE_BUTTON);
            long deadline = System.currentTimeMillis() + 10_000;
            String enabledState = "false";
            while (System.currentTimeMillis() < deadline) {
                try {
                    WebElement continueBtn = driver.findElement(continueLocator);
                    enabledState = continueBtn.getAttribute("enabled");
                    if ("true".equalsIgnoreCase(enabledState)) {
                        logger.info("Continue button is ENABLED (blue) — amount accepted");
                        break;
                    }
                } catch (Exception ignored) {}
                sleep(500);
            }
            logger.info("Continue enabled after amount entry: " + enabledState);
        } catch (Exception e) {
            logger.error("Failed to enter amount: " + e.getMessage());
            throw new RuntimeException("Amount field not found", e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 6 — Tap Continue
    // -----------------------------------------------------------------------

    public void tapContinue() {
        try {
            By continueLocator = LocatorUtils.getLocator(CONTINUE_BUTTON);

            // Wait for Continue to be visible and enabled — it turns blue only after
            // a valid amount is entered. Poll up to 10 s in case of React Native re-render lag.
            WebElement btn = waitForElement(continueLocator, 15);
            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline) {
                try {
                    btn = driver.findElement(continueLocator);
                    String enabled = btn.getAttribute("enabled");
                    if ("true".equalsIgnoreCase(enabled)) {
                        logger.info("Continue button is ENABLED (blue) — proceeding to tap");
                        break;
                    }
                    logger.debug("Continue still disabled, waiting...");
                } catch (Exception ignored) {}
                sleep(500);
            }

            String enabled   = btn.getAttribute("enabled");
            String clickable = btn.getAttribute("clickable");
            logger.info("Continue button — enabled: " + enabled + ", clickable: " + clickable +
                        ", bounds: [" + btn.getLocation() + "][" + btn.getSize() + "]");

            // React Native Pressable/TouchableOpacity responds ONLY to real touch events,
            // not to accessibility ACTION_CLICK. The keyboard is already dismissed by
            // enterAmount() so the button coordinates are unobstructed.
            // Use W3C Actions to send a genuine touch event at the element's centre.
            org.openqa.selenium.Point loc = btn.getLocation();
            org.openqa.selenium.Dimension sz = btn.getSize();
            int centerX = loc.getX() + sz.getWidth()  / 2;
            int centerY = loc.getY() + sz.getHeight() / 2;
            logger.info("Tapping Continue via W3C touch at (" + centerX + ", " + centerY + ")");

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "continueFinger");
            Sequence tap = new Sequence(finger, 1);
            tap.addAction(finger.createPointerMove(Duration.ZERO,
                    PointerInput.Origin.viewport(), centerX, centerY));
            tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Arrays.asList(tap));
            sleep(3000);
            logger.info("Tapped Continue button via W3C touch gesture");
        } catch (Exception e) {
            logger.error("Failed to tap Continue: " + e.getMessage());
            throw new RuntimeException("Continue button not found", e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 7 — Verify PIN page
    // -----------------------------------------------------------------------

    public boolean isPinPageDisplayed() {
        try {
            By locator = LocatorUtils.getLocator(PIN_PAGE);
            waitForElement(locator, 25); // generous — there may be a review step after Continue
            boolean displayed = isElementDisplayed(locator);
            logger.info("PIN page displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("PIN page not found: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Step 8 — Enter PIN
    // -----------------------------------------------------------------------

    public void enterPin(String pin) {
        try {
            // Poll for the keyboard to appear — the PIN screen auto-focuses
            // its field and shows the keyboard as soon as the page loads.
            logger.info("Waiting for keyboard on PIN page...");
            long kbDeadline = System.currentTimeMillis() + 10_000;
            boolean keyboardUp = false;
            while (System.currentTimeMillis() < kbDeadline) {
                try {
                    if (((AndroidDriver) driver).isKeyboardShown()) {
                        keyboardUp = true;
                        break;
                    }
                } catch (Exception ignored) {}
                sleep(300);
            }

            if (!keyboardUp) {
                // Keyboard not auto-shown — tap the PIN field to open it.
                logger.info("Keyboard not shown — tapping PIN field to open keyboard");
                By pinLocator = LocatorUtils.getLocator(PIN_FIELD);
                WebElement pinField = WaitHelper.waitForElementToBeVisible(pinLocator, 10);
                pinField.click();
                sleep(800);
            }

            // Type each PIN digit directly via native key events — no sendKeys on field.
            logger.info("Keyboard present — pressing PIN digits via native key events");
            AndroidDriver androidDriver = (AndroidDriver) driver;
            for (char c : pin.toCharArray()) {
                AndroidKey key;
                switch (c) {
                    case '0': key = AndroidKey.DIGIT_0; break;
                    case '1': key = AndroidKey.DIGIT_1; break;
                    case '2': key = AndroidKey.DIGIT_2; break;
                    case '3': key = AndroidKey.DIGIT_3; break;
                    case '4': key = AndroidKey.DIGIT_4; break;
                    case '5': key = AndroidKey.DIGIT_5; break;
                    case '6': key = AndroidKey.DIGIT_6; break;
                    case '7': key = AndroidKey.DIGIT_7; break;
                    case '8': key = AndroidKey.DIGIT_8; break;
                    case '9': key = AndroidKey.DIGIT_9; break;
                    default:  logger.warn("Skipping non-digit char in PIN: " + c); continue;
                }
                androidDriver.pressKey(new KeyEvent(key));
                sleep(150);
            }
            logger.info("PIN digits pressed — now tapping Pay via W3C touch");
            sleep(500);

            // Tap Pay / Submit via W3C touch — React Native Pressable ignores accessibility click.
            By submitLocator = LocatorUtils.getLocator(PIN_SUBMIT);
            WebElement payBtn = waitForElement(submitLocator, 10);
            org.openqa.selenium.Point payLoc = payBtn.getLocation();
            org.openqa.selenium.Dimension paySz = payBtn.getSize();
            int payCX = payLoc.getX() + paySz.getWidth()  / 2;
            int payCY = payLoc.getY() + paySz.getHeight() / 2;
            logger.info("Pay button bounds: [" + payLoc + "][" + paySz + "] — tapping at (" + payCX + ", " + payCY + ")");
            PointerInput payFinger = new PointerInput(PointerInput.Kind.TOUCH, "payFinger");
            Sequence payTap = new Sequence(payFinger, 1);
            payTap.addAction(payFinger.createPointerMove(Duration.ZERO,
                    PointerInput.Origin.viewport(), payCX, payCY));
            payTap.addAction(payFinger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            payTap.addAction(payFinger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Arrays.asList(payTap));
            sleep(3000); // wait for transaction to process and confirmation to load
            logger.info("Tapped Pay button via W3C touch gesture");
        } catch (Exception e) {
            logger.error("Failed to enter PIN: " + e.getMessage());
            throw new RuntimeException("PIN entry failed", e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 9 — Verify confirmation bottom drawer
    // -----------------------------------------------------------------------

    public boolean isConfirmationDrawerDisplayed() {
        try {
            sleep(2000);
            By locator = LocatorUtils.getLocator(CONFIRMATION_DRAWER);
            waitForElement(locator, 20);
            boolean displayed = isElementDisplayed(locator);
            logger.info("Confirmation bottom drawer displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Confirmation drawer not found: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Step 10 — Transaction details page
    // -----------------------------------------------------------------------

    public boolean isTransferSuccessDisplayed() {
        try {
            By locator = LocatorUtils.getLocator(SUCCESS_MESSAGE);
            waitForElement(locator, 20);
            boolean displayed = isElementDisplayed(locator);
            logger.info("Transfer success displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Success message not found: " + e.getMessage());
            return false;
        }
    }

    public boolean isTransferFailureDisplayed() {
        try {
            By locator = LocatorUtils.getLocator(FAILURE_MESSAGE);
            waitForElement(locator, 20);
            boolean displayed = isElementDisplayed(locator);
            logger.info("Transfer failure displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Failure message not found: " + e.getMessage());
            return false;
        }
    }

    /** Returns true if either success OR failure details page is shown */
    public boolean isTransactionDetailsPageDisplayed() {
        return isTransferSuccessDisplayed() || isTransferFailureDisplayed();
    }

    public boolean isErrorDisplayed() {
        try {
            By locator = LocatorUtils.getLocator(ERROR_MESSAGE);
            return isElementDisplayed(locator);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isInsufficientFundsDisplayed() {
        try {
            By locator = LocatorUtils.getLocator(INSUFFICIENT_FUNDS);
            return isElementDisplayed(locator);
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessageText() {
        try {
            By locator = LocatorUtils.getLocator(ERROR_MESSAGE);
            return getText(locator);
        } catch (Exception e) {
            return "";
        }
    }

    // -----------------------------------------------------------------------
    // Step 11 — Verify chat bubble
    // -----------------------------------------------------------------------

    public boolean isChatBubbleDisplayed() {
        By chatLocator    = LocatorUtils.getLocator(CHAT_BUBBLE);
        By detailsLocator = LocatorUtils.getLocator(DETAILS_PAGE);
        // Poll up to 25 s for EITHER a chat bubble OR direct navigation to Transaction Details
        long deadline = System.currentTimeMillis() + 25_000;
        while (System.currentTimeMillis() < deadline) {
            if (isElementDisplayed(chatLocator)) {
                logger.info("Chat bubble displayed — will tap it next");
                return true;
            }
            if (isElementDisplayed(detailsLocator)) {
                logger.info("App navigated directly to Transaction Details (no chat bubble) — treating as bubble visible");
                return true;
            }
            sleep(500);
        }
        logger.warn("Neither chat bubble nor Transaction Details found within 25 s");
        return false;
    }

    // -----------------------------------------------------------------------
    // Step 11b — Tap the most recent chat bubble
    // -----------------------------------------------------------------------

    public void tapRecentChatBubble() {
        try {
            // If already on Transaction Details page, skip tapping — app navigated there directly
            By detailsLocator = LocatorUtils.getLocator(DETAILS_PAGE);
            if (isElementDisplayed(detailsLocator)) {
                logger.info("Already on Transaction Details page — skipping chat bubble tap");
                return;
            }
            By locator = LocatorUtils.getLocator(CHAT_BUBBLE);
            // Find the LAST matching element — most recent bubble is at the bottom
            java.util.List<WebElement> bubbles = driver.findElements(locator);
            WebElement recent = bubbles.isEmpty()
                    ? waitForElement(locator, 15)          // fallback: wait for at least one
                    : bubbles.getLast();    // last = most recent
            org.openqa.selenium.Point p = recent.getLocation();
            org.openqa.selenium.Dimension d = recent.getSize();
            int cx = p.getX() + d.getWidth()  / 2;
            int cy = p.getY() + d.getHeight() / 2;
            logger.info("Tapping recent chat bubble via W3C touch at (" + cx + ", " + cy + ")");
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "bubbleFinger");
            Sequence tap = new Sequence(finger, 1);
            tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), cx, cy));
            tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Arrays.asList(tap));
            sleep(2000);
            logger.info("Tapped recent chat bubble");
        } catch (Exception e) {
            logger.error("Failed to tap recent chat bubble: " + e.getMessage());
            throw new RuntimeException("Recent chat bubble tap failed", e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 12 — Tap details icon (top-right corner of chat bubble)
    // -----------------------------------------------------------------------

    public void tapDetailsIcon() {
        try {
            By locator = LocatorUtils.getLocator(DETAILS_ICON);
            WebElement icon = waitForElement(locator, 15);
            // React Native — use W3C touch gesture
            org.openqa.selenium.Point p = icon.getLocation();
            org.openqa.selenium.Dimension d = icon.getSize();
            int cx = p.getX() + d.getWidth()  / 2;
            int cy = p.getY() + d.getHeight() / 2;
            logger.info("Tapping details icon via W3C touch at (" + cx + ", " + cy + ")");
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "detailFinger");
            Sequence tap = new Sequence(finger, 1);
            tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), cx, cy));
            tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(Arrays.asList(tap));
            sleep(2000);
            logger.info("Tapped details icon");
        } catch (Exception e) {
            logger.error("Failed to tap details icon: " + e.getMessage());
            throw new RuntimeException("Details icon not found", e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 13 — Verify transaction details page
    // -----------------------------------------------------------------------

    public boolean isDetailPageDisplayed() {
        try {
            By locator = LocatorUtils.getLocator(DETAILS_PAGE);
            waitForElement(locator, 20);
            boolean displayed = isElementDisplayed(locator);
            logger.info("Transaction details page displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Transaction details page not found: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Step 14 — Verify success or failure message on details page
    // -----------------------------------------------------------------------

    /**
     * Checks for success OR failure message on the Transaction Details page.
     * Returns "SUCCESS", "FAILURE", or throws if neither found within timeout.
     */
    public String verifyTransactionOutcome() {
        try {
            By successLocator = LocatorUtils.getLocator(SUCCESS_MESSAGE);
            By failureLocator = LocatorUtils.getLocator(FAILURE_MESSAGE);
            // Poll up to 15 s for either message
            long deadline = System.currentTimeMillis() + 15_000;
            while (System.currentTimeMillis() < deadline) {
                if (isElementDisplayed(successLocator)) {
                    logger.info("Transaction outcome: SUCCESS");
                    return "SUCCESS";
                }
                if (isElementDisplayed(failureLocator)) {
                    logger.info("Transaction outcome: FAILURE");
                    return "FAILURE";
                }
                sleep(500);
            }
            throw new RuntimeException("Neither success nor failure message found on Transaction Details page after 15 s");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error verifying transaction outcome: " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Step 15 — Navigate back to home from Transaction Details page
    // FAIL  → 1 back press  (app went directly to Details, no chat screen in between)
    // SUCCESS → 2 back presses (Details → Chat screen → Home)
    // -----------------------------------------------------------------------

    public void navigateBackToHomeFromDetails(String outcome) {
        try {
            logger.info("Back press 1: leaving Transaction Details page (outcome=" + outcome + ")");
            driver.navigate().back();
            sleep(1500);
            if (!"FAILURE".equalsIgnoreCase(outcome)) {
                // SUCCESS path: need a second back to leave the chat/conversation screen
                logger.info("Back press 2: leaving chat screen — returning to Home");
                driver.navigate().back();
                sleep(2000);
            }
            logger.info("Navigated back to home screen");
        } catch (Exception e) {
            logger.warn("Issue navigating back to home: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Step 16 — Confirm home screen is visible (Send Money tile present)
    // -----------------------------------------------------------------------

    public boolean isHomeScreenDisplayed() {
        try {
            By locator = LocatorUtils.getLocator(SEND_MONEY_BUTTON);
            waitForElement(locator, 15);
            boolean displayed = isElementDisplayed(locator);
            logger.info("Home screen (Send Money tile) displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn("Home screen not detected: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Cancel
    // -----------------------------------------------------------------------

    public void tapCancel() {
        try {
            By locator = LocatorUtils.getLocator(CANCEL_BUTTON);
            waitForElement(locator, 8);
            click(locator);
            logger.info("Tapped Cancel");
        } catch (Exception e) {
            logger.warn("Cancel button not found: " + e.getMessage());
        }
    }
}
