package com.sele3.waits;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sele3.drivers.DriverRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeleniumWait {
    
    /**
     * Builds a {@link WebDriverWait} using the current driver's configured timeout and polling interval.
     *
     * @return a new {@link WebDriverWait}
     */
    public static WebDriverWait getWebDriverWait() {
        return new WebDriverWait(DriverRunner.getWebDriver(), DriverRunner.getConfig().getTimeout(), DriverRunner.getConfig().getPollingInterval());
    }

    /**
     * Builds a {@link WebDriverWait} using an explicit timeout and polling interval.
     *
     * @param timeout the maximum time to wait
     * @param pollingInterval how often to check the condition while waiting
     * @return a new {@link WebDriverWait}
     */
    public static WebDriverWait getWebDriverWait(Duration timeout, Duration pollingInterval) {
        return new WebDriverWait(DriverRunner.getWebDriver(), timeout, pollingInterval);
    }

    /**
     * Waits, using the default timeout/polling interval, until the given condition returns a
     * non-null/non-false result.
     *
     * @param condition the condition to evaluate against the {@link WebDriver}
     * @param <T> the result type of the condition
     * @return the result produced by {@code condition} once satisfied
     */
    public static <T> T executeWait(Function<WebDriver, T> condition) {
        return getWebDriverWait().until(condition);
    }

    /**
     * Waits, using an explicit timeout/polling interval, until the given condition returns a
     * non-null/non-false result.
     *
     * @param timeout the maximum time to wait
     * @param pollingInterval how often to check the condition while waiting
     * @param condition the condition to evaluate against the {@link WebDriver}
     * @param <T> the result type of the condition
     * @return the result produced by {@code condition} once satisfied
     */
    public static <T> T executeWait(Duration timeout, Duration pollingInterval, Function<WebDriver, T> condition) {
        return getWebDriverWait(timeout, pollingInterval).until(condition);
    }

    /**
     * Waits until {@code document.readyState} reports "complete".
     */
    public static void waitForPageToLoad() {
        ExpectedCondition<Boolean> javascriptDone = d -> {
            try {
                return ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete");
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        };
        executeWait(javascriptDone);
    }

    /**
     * Waits for {@code document.readyState} to report "loading", then waits for it to report
     * "complete". Useful for catching a page navigation that hasn't started yet.
     */
    public static void waitForPageLoadingAndComplete() {
        ExpectedCondition<Boolean> javascriptLoading = d -> {
            try {
                return ((JavascriptExecutor) d).executeScript("return document.readyState").equals("loading");
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        };
        executeWait(javascriptLoading);
        waitForPageToLoad();
    }

    /**
     * Waits until jQuery reports no active AJAX requests ({@code jQuery.active == 0}).
     * If jQuery is unavailable, the wait is treated as satisfied immediately.
     */
    public static void waitForJQueryToLoad() {
        ExpectedCondition<Boolean> jQueryDone = d -> {
            try {
                return ((Long) ((JavascriptExecutor) DriverRunner.getWebDriver()).executeScript("return jQuery.active") == 0);
            } catch (Exception e) {
                return true;
            }
        };
        executeWait(jQueryDone);
    }

    /**
     * Waits for jQuery to start an AJAX request ({@code jQuery.active > 0}), then waits for it
     * to finish. Useful for catching a jQuery request that hasn't started yet.
     */
    public static void waitForJQueryToProcessAndLoad() {
        ExpectedCondition<Boolean> jQueryProcess = d -> {
            try {
                return ((Long) ((JavascriptExecutor) DriverRunner.getWebDriver()).executeScript("return jQuery.active") > 0);
            } catch (Exception e) {
                return true;
            }
        };
        executeWait(jQueryProcess);
        waitForJQueryToLoad();
    }

    /**
     * Waits until the current URL contains the given text.
     *
     * @param text the substring expected to appear in the current URL
     */
    public static void waitForUrlContains(String text) {
        executeWait(d -> d.getCurrentUrl().contains(text));
    }

    /**
     * Waits until an element matching the locator is present in the DOM.
     *
     * @param locator the locator to search for
     * @return the found {@link WebElement}
     */
    public static WebElement waitForExist(By locator) {
        return executeWait(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Waits until at least one element matching the locator is present in the DOM.
     *
     * @param locator the locator to search for
     * @return all matching {@link WebElement}s
     */
    public static List<WebElement> waitForAllExist(By locator) {
        return executeWait(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    /**
     * Waits until an element matching the locator is present and visible.
     *
     * @param locator the locator to search for
     */
    public static void waitForVisible(By locator) {
        executeWait(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until all elements matching the locator are present and visible.
     *
     * @param locator the locator to search for
     */
    public static void waitForAllVisible(By locator) {
        executeWait(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /**
     * Waits until no element matching the locator is visible (or it is no longer present).
     *
     * @param locator the locator to search for
     */
    public static void waitForInvisible(By locator) {
        executeWait(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Waits until the given element is enabled.
     *
     * @param element the element to check
     */
    public static void waitForEnabled(WebElement element) {
        executeWait(driver -> element.isEnabled());
    }

    /**
     * Waits until the given element is disabled.
     *
     * @param element the element to check
     */
    public static void waitForDisabled(WebElement element) {
        executeWait(driver -> !element.isEnabled());
    }

    /**
     * Waits until an element matching the locator is visible and enabled.
     *
     * @param locator the locator to search for
     * @return the clickable {@link WebElement}
     */
    public static WebElement waitForClickable(By locator) {
        return executeWait(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Waits until the element's {@code value} attribute equals the given value.
     *
     * @param element the element to check
     * @param value the expected value
     */
    public static void waitForValueEquals(WebElement element, String value) {
        executeWait(driver -> element.getAttribute("value").equals(value));
    }

    /**
     * Waits until the element's {@code value} attribute no longer equals the given value.
     *
     * @param element the element to check
     * @param value the value expected to no longer match
     */
    public static void waitForValueNotEquals(WebElement element, String value) {
        executeWait(driver -> !element.getAttribute("value").equals(value));
    }

    /**
     * Waits until the element's {@code value} attribute contains the given text.
     *
     * @param element the element to check
     * @param value the substring expected to appear in the value
     */
    public static void waitForValueContains(WebElement element, String value) {
        executeWait(driver -> element.getAttribute("value").contains(value));
    }

    /**
     * Waits until the element's visible text equals the given text.
     *
     * @param element the element to check
     * @param text the expected text
     */
    public static void waitForTextEquals(WebElement element, String text) {
        executeWait(driver -> element.getText().equals(text));
    }

    /**
     * Waits until the element's visible text no longer equals the given text.
     *
     * @param element the element to check
     * @param text the text expected to no longer match
     */
    public static void waitForTextNotEquals(WebElement element, String text) {
        executeWait(driver -> !element.getText().equals(text));
    }

    /**
     * Waits until the element matching the locator contains the given text.
     *
     * @param locator the locator to search for
     * @param text the substring expected to appear in the element's text
     */
    public static void waitForTextContains(By locator, String text) {
        executeWait(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Waits until the given attribute on the element equals the given value.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the expected value
     */
    public static void waitForAttributeEquals(WebElement element, String attribute, String value) {
        executeWait(driver -> element.getAttribute(attribute).equals(value));
    }

    /**
     * Waits until the given attribute on the element no longer equals the given value.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the value expected to no longer match
     */
    public static void waitForAttributeNotEquals(WebElement element, String attribute, String value) {
        executeWait(driver -> !element.getAttribute(attribute).equals(value));
    }

    /**
     * Waits until the given attribute on the element contains the given value.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     */
    public static void waitForAttributeContains(WebElement element, String attribute, String value) {
        executeWait(ExpectedConditions.attributeContains(element, attribute, value));
    }

    /**
     * Waits until the {@code <select>} element matching the locator has its {@code <option>}
     * children populated.
     *
     * @param locator the locator of the {@code <select>} element
     */
    public static void waitForSelectOptionsLoaded(By locator) {
        executeWait(ExpectedConditions.presenceOfNestedElementsLocatedBy(locator, By.tagName("option")));
    }

    /**
     * Waits until the given element is selected/checked.
     *
     * @param element the element to check
     */
    public static void waitForChecked(WebElement element) {
        executeWait(driver -> element.isSelected());
    }

    /**
     * Waits until the given element is deselected/unchecked.
     *
     * @param element the element to check
     */
    public static void waitForUnchecked(WebElement element) {
        executeWait(driver -> !element.isSelected());
    }

    /**
     * Waits until the given element becomes stale (detached from the DOM).
     *
     * @param element the element expected to go stale
     */
    public static void waitForStaleness(WebElement element) {
        SeleniumWait.executeWait(ExpectedConditions.stalenessOf(element));
    }
}
