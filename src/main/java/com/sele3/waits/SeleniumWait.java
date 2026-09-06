package com.sele3.waits;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sele3.drivers.DriverRunner;
import com.sele3.elements.BaseElement;

import lombok.extern.slf4j.Slf4j;

/**
 * Selenium {@link WebDriverWait} helpers built around the current driver's configured timeout
 * and polling interval. Each {@code waitFor*}/{@code executeWait} call builds and uses its own
 * fresh {@link WebDriverWait}, so nesting one of these inside another wait or a
 * {@link RetryAction#retry} action adds that call's own full timeout budget on top of the
 * outer one rather than sharing a deadline with it.
 */
@Slf4j
public class SeleniumWait {
     
    /**
     * Builds a {@link WebDriverWait} using the current driver's configured timeout and polling
     * interval.
     *
     * @return a new {@link WebDriverWait}
     */
    public static WebDriverWait getWebDriverWait() {
        return getWebDriverWait(DriverRunner.getConfig().getTimeout(), DriverRunner.getConfig().getPollingInterval());
    }

    /**
     * Builds a {@link WebDriverWait} using an explicit timeout and polling interval.
     *
     * @param timeout the maximum time to wait
     * @param pollingInterval how often to check the condition while waiting
     * @return a new {@link WebDriverWait}
     */
    public static WebDriverWait getWebDriverWait(Duration timeout, Duration pollingInterval) {
        WebDriverWait wait = new WebDriverWait(DriverRunner.getWebDriver(), timeout, pollingInterval);
        return wait;
    }

    /**
     * Builds a {@link WebDriverWait} using an explicit timeout, the current driver's configured
     * polling interval, and ignoring {@link StaleElementReferenceException} while polling.
     *
     * @param timeout the maximum time to wait
     * @return a new {@link WebDriverWait}
     */
    public static WebDriverWait getWebDriverWait(Duration timeout) {
        return getWebDriverWait(timeout, DriverRunner.getConfig().getPollingInterval());
    }

    /**
     * Waits, using an explicit timeout and the current driver's configured polling interval,
     * until the given condition returns a non-null/non-false result.
     *
     * @param timeout the maximum time to wait
     * @param condition the condition to evaluate against the {@link WebDriver}
     * @param <T> the result type of the condition
     * @return the result produced by {@code condition} once satisfied
     */
    public static <T> T executeWait(Duration timeout, Function<WebDriver, T> condition) {
        return getWebDriverWait(timeout).until(condition);
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
        return executeWait(DriverRunner.getConfig().getTimeout(), DriverRunner.getConfig().getPollingInterval(), condition);
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
     *
     * @param timeout the maximum time to wait
     */
    public static void waitForPageToLoad(Duration timeout) {
        ExpectedCondition<Boolean> javascriptDone = d -> {
            try {
                return ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete");
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        };
        executeWait(timeout, javascriptDone);
    }

    /**
     * Waits for {@code document.readyState} to report "loading", then waits for it to report
     * "complete". Useful for catching a page navigation that hasn't started yet.
     *
     * @param timeout the maximum time to wait for each of the two stages
     */
    public static void waitForPageLoadingAndComplete(Duration timeout) {
        ExpectedCondition<Boolean> javascriptLoading = d -> {
            try {
                return ((JavascriptExecutor) d).executeScript("return document.readyState").equals("loading");
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        };
        executeWait(timeout, javascriptLoading);
        waitForPageToLoad(timeout);
    }

    /**
     * Waits until jQuery reports no active AJAX requests ({@code jQuery.active == 0}).
     * If jQuery is unavailable, the wait is treated as satisfied immediately.
     *
     * @param timeout the maximum time to wait
     */
    public static void waitForJQueryToLoad(Duration timeout) {
        ExpectedCondition<Boolean> jQueryDone = d -> {
            try {
                return ((Long) ((JavascriptExecutor) DriverRunner.getWebDriver()).executeScript("return jQuery.active") == 0);
            } catch (Exception e) {
                return true;
            }
        };
        executeWait(timeout, jQueryDone);
    }

    /**
     * Waits for jQuery to start an AJAX request ({@code jQuery.active > 0}), then waits for it
     * to finish. Useful for catching a jQuery request that hasn't started yet.
     *
     * @param timeout the maximum time to wait for each of the two stages
     */
    public static void waitForJQueryToProcessAndLoad(Duration timeout) {
        ExpectedCondition<Boolean> jQueryProcess = d -> {
            try {
                return ((Long) ((JavascriptExecutor) DriverRunner.getWebDriver()).executeScript("return jQuery.active") > 0);
            } catch (Exception e) {
                return true;
            }
        };
        executeWait(timeout, jQueryProcess);
        waitForJQueryToLoad(timeout);
    }

    /**
     * Waits until the current URL contains the given text.
     *
     * @param text the substring expected to appear in the current URL
     * @param timeout the maximum time to wait
     */
    public static void waitForUrlContains(String text, Duration timeout) {
        executeWait(timeout, d -> d.getCurrentUrl().contains(text));
    }

    /**
     * Waits until an element matching the locator is present in the DOM.
     *
     * @param element the element to search for
     * @param timeout the maximum time to wait
     * @return the found {@link WebElement}
     */
    public static WebElement waitForExist(BaseElement element, Duration timeout) {
        return executeWait(timeout, ExpectedConditions.presenceOfElementLocated(element.getLocator()));
    }

    /**
     * Waits until at least one element matching the locator is present in the DOM.
     *
     * @param element the element to search for
     * @param timeout the maximum time to wait
     * @return all matching {@link WebElement}s
     */
    public static List<WebElement> waitForAllExist(BaseElement element, Duration timeout) {
        return executeWait(timeout, ExpectedConditions.presenceOfAllElementsLocatedBy(element.getLocator()));
    }

    /**
     * Waits until an element matching the locator is present and visible.
     *
     * @param element the element to wait for
     * @param timeout the maximum time to wait
     * @return the visible {@link WebElement}
     */
    public static WebElement waitForVisible(BaseElement element, Duration timeout) {
        return executeWait(timeout, ExpectedConditions.visibilityOfElementLocated(element.getLocator()));
    }

    /**
     * Waits until all elements matching the locator are present and visible.
     *
     * @param element the element to wait for
     * @param timeout the maximum time to wait
     * @return the visible {@link WebElement}s
     */
    public static List<WebElement> waitForAllVisible(BaseElement element, Duration timeout) {
        return executeWait(timeout, ExpectedConditions.visibilityOfAllElementsLocatedBy(element.getLocator()));
    }

    /**
     * Waits until no element matching the locator is visible (or it is no longer present).
     *
     * @param element the element to search for
     * @param timeout the maximum time to wait
     */
    public static void waitForInvisible(BaseElement element, Duration timeout) {
        executeWait(timeout, ExpectedConditions.invisibilityOfElementLocated(element.getLocator()));
    }

    /**
     * Waits until the given element is enabled.
     *
     * @param element the element to check
     * @param timeout the maximum time to wait
     */
    public static void waitForEnabled(BaseElement element, Duration timeout) {
        executeWait(timeout, ExpectedConditions.elementToBeClickable(element.getLocator()));
    }

    /**
     * Waits until the given element is disabled.
     *
     * @param element the element to check
     * @param timeout the maximum time to wait
     */
    public static void waitForDisabled(BaseElement element, Duration timeout) {
        executeWait(timeout, driver -> !driver.findElement(element.getLocator()).isEnabled());
    }

    /**
     * Waits until an element matching the locator is visible and enabled.
     *
     * @param element the element to search for
     * @param timeout the maximum time to wait
     * @return the clickable {@link WebElement}
     */
    public static WebElement waitForClickable(BaseElement element, Duration timeout) {
        return executeWait(timeout, ExpectedConditions.elementToBeClickable(element.getLocator()));
    }

    /**
     * Waits until the element's {@code value} attribute equals the given value.
     *
     * @param element the element to check
     * @param value the expected value
     * @param timeout the maximum time to wait
     */
    public static void waitForValueEquals(BaseElement element, String value, Duration timeout) {
        executeWait(timeout, driver -> element.getElement().getAttribute("value").equals(value));
    }

    /**
     * Waits until the element's {@code value} attribute no longer equals the given value.
     *
     * @param element the element to check
     * @param value the value expected to no longer match
     * @param timeout the maximum time to wait
     */
    public static void waitForValueNotEquals(BaseElement element, String value, Duration timeout) {
        executeWait(timeout, driver -> !element.getElement().getAttribute("value").equals(value));
    }

    /**
     * Waits until the element's {@code value} attribute contains the given text.
     *
     * @param element the element to check
     * @param value the substring expected to appear in the value
     * @param timeout the maximum time to wait
     */
    public static void waitForValueContains(BaseElement element, String value, Duration timeout) {
        executeWait(timeout, driver -> element.getElement().getAttribute("value").contains(value));
    }

    /**
     * Waits until the element's visible text equals the given text.
     *
     * @param element the element to check
     * @param text the expected text
     * @param timeout the maximum time to wait
     */
    public static void waitForTextEquals(BaseElement element, String text, Duration timeout) {
        executeWait(timeout, driver -> element.getElement().getText().equals(text));
    }

    /**
     * Waits until the element's visible text no longer equals the given text.
     *
     * @param element the element to check
     * @param text the text expected to no longer match
     * @param timeout the maximum time to wait
     */
    public static void waitForTextNotEquals(BaseElement element, String text, Duration timeout) {
        executeWait(timeout, driver -> !element.getElement().getText().equals(text));
    }

    /**
     * Waits until the element matching the locator contains the given text.
     *
     * @param element the element to check
     * @param text the substring expected to appear in the element's text
     * @param timeout the maximum time to wait
     */
    public static void waitForTextContains(BaseElement element, String text, Duration timeout) {
        executeWait(timeout, ExpectedConditions.textToBePresentInElementLocated(element.getLocator(), text));
    }

    /**
     * Waits until the given attribute on the element equals the given value.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the expected value
     * @param timeout the maximum time to wait
     */
    public static void waitForAttributeEquals(BaseElement element, String attribute, String value, Duration timeout) {
        executeWait(timeout, driver -> element.getElement().getAttribute(attribute).equals(value));
    }

    /**
     * Waits until the given attribute on the element no longer equals the given value.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the value expected to no longer match
     * @param timeout the maximum time to wait
     */
    public static void waitForAttributeNotEquals(BaseElement element, String attribute, String value, Duration timeout) {
        executeWait(timeout, driver -> !element.getElement().getAttribute(attribute).equals(value));
    }

    /**
     * Waits until the given attribute on the element contains the given value.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     * @param timeout the maximum time to wait
     */
    public static void waitForAttributeContains(BaseElement element, String attribute, String value, Duration timeout) {
        executeWait(timeout, ExpectedConditions.attributeContains(element.getLocator(), attribute, value));
    }

    /**
     * Waits until the {@code <select>} element matching the locator has its {@code <option>}
     * children populated.
     *
     * @param element the {@code <select>} element to check
     * @param timeout the maximum time to wait
     */
    public static void waitForSelectOptionsLoaded(BaseElement element, Duration timeout) {
        executeWait(timeout, ExpectedConditions.presenceOfNestedElementsLocatedBy(element.getLocator(), By.tagName("option")));
    }

    /**
     * Waits until the given element is selected/checked.
     *
     * @param element the element to check
     * @param timeout the maximum time to wait
     */
    public static void waitForChecked(BaseElement element, Duration timeout) {
        executeWait(timeout, driver -> element.getElement().isSelected());
    }

    /**
     * Waits until the given element is deselected/unchecked.
     *
     * @param element the element to check
     * @param timeout the maximum time to wait
     */
    public static void waitForUnchecked(BaseElement element, Duration timeout) {
        executeWait(timeout, driver -> !element.getElement().isSelected());
    }

    /**
     * Convenience overload of {@link #waitForPageToLoad(Duration)} using the current driver's
     * configured timeout.
     */
    public static void waitForPageToLoad() {
        waitForPageToLoad(DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForPageLoadingAndComplete(Duration)} using the current
     * driver's configured timeout.
     */
    public static void waitForPageLoadingAndComplete() {
        waitForPageLoadingAndComplete(DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForJQueryToLoad(Duration)} using the current driver's
     * configured timeout.
     */
    public static void waitForJQueryToLoad() {
        waitForJQueryToLoad(DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForJQueryToProcessAndLoad(Duration)} using the current
     * driver's configured timeout.
     */
    public static void waitForJQueryToProcessAndLoad() {
        waitForJQueryToProcessAndLoad(DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForUrlContains(String, Duration)} using the current
     * driver's configured timeout.
     *
     * @param text the substring expected to appear in the current URL
     */
    public static void waitForUrlContains(String text) {
        waitForUrlContains(text, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForExist(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to search for
     * @return the found {@link WebElement}
     */
    public static WebElement waitForExist(BaseElement element) {
        return waitForExist(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForAllExist(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to search for
     * @return all matching {@link WebElement}s
     */
    public static List<WebElement> waitForAllExist(BaseElement element) {
        return waitForAllExist(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForVisible(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to wait for
     * @return the visible {@link WebElement}
     */
    public static WebElement waitForVisible(BaseElement element) {
        return waitForVisible(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForAllVisible(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to wait for
     * @return the visible {@link WebElement}s
     */
    public static List<WebElement> waitForAllVisible(BaseElement element) {
        return waitForAllVisible(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForInvisible(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to search for
     */
    public static void waitForInvisible(BaseElement element) {
        waitForInvisible(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForEnabled(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to check
     */
    public static void waitForEnabled(BaseElement element) {
        waitForEnabled(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForDisabled(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to check
     */
    public static void waitForDisabled(BaseElement element) {
        waitForDisabled(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForClickable(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to search for
     * @return the clickable {@link WebElement}
     */
    public static WebElement waitForClickable(BaseElement element) {
        return waitForClickable(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForValueEquals(BaseElement, String, Duration)} using
     * the current driver's configured timeout.
     *
     * @param element the element to check
     * @param value the expected value
     */
    public static void waitForValueEquals(BaseElement element, String value) {
        waitForValueEquals(element, value, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForValueNotEquals(BaseElement, String, Duration)}
     * using the current driver's configured timeout.
     *
     * @param element the element to check
     * @param value the value expected to no longer match
     */
    public static void waitForValueNotEquals(BaseElement element, String value) {
        waitForValueNotEquals(element, value, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForValueContains(BaseElement, String, Duration)} using
     * the current driver's configured timeout.
     *
     * @param element the element to check
     * @param value the substring expected to appear in the value
     */
    public static void waitForValueContains(BaseElement element, String value) {
        waitForValueContains(element, value, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForTextEquals(BaseElement, String, Duration)} using
     * the current driver's configured timeout.
     *
     * @param element the element to check
     * @param text the expected text
     */
    public static void waitForTextEquals(BaseElement element, String text) {
        waitForTextEquals(element, text, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForTextNotEquals(BaseElement, String, Duration)} using
     * the current driver's configured timeout.
     *
     * @param element the element to check
     * @param text the text expected to no longer match
     */
    public static void waitForTextNotEquals(BaseElement element, String text) {
        waitForTextNotEquals(element, text, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForTextContains(BaseElement, String, Duration)} using
     * the current driver's configured timeout.
     *
     * @param element the element to check
     * @param text the substring expected to appear in the element's text
     */
    public static void waitForTextContains(BaseElement element, String text) {
        waitForTextContains(element, text, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForAttributeEquals(BaseElement, String, String, Duration)}
     * using the current driver's configured timeout.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the expected value
     */
    public static void waitForAttributeEquals(BaseElement element, String attribute, String value) {
        waitForAttributeEquals(element, attribute, value, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForAttributeNotEquals(BaseElement, String, String, Duration)}
     * using the current driver's configured timeout.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the value expected to no longer match
     */
    public static void waitForAttributeNotEquals(BaseElement element, String attribute, String value) {
        waitForAttributeNotEquals(element, attribute, value, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForAttributeContains(BaseElement, String, String, Duration)}
     * using the current driver's configured timeout.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     */
    public static void waitForAttributeContains(BaseElement element, String attribute, String value) {
        waitForAttributeContains(element, attribute, value, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForSelectOptionsLoaded(BaseElement, Duration)} using
     * the current driver's configured timeout.
     *
     * @param element the {@code <select>} element to check
     */
    public static void waitForSelectOptionsLoaded(BaseElement element) {
        waitForSelectOptionsLoaded(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForChecked(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to check
     */
    public static void waitForChecked(BaseElement element) {
        waitForChecked(element, DriverRunner.getConfig().getTimeout());
    }

    /**
     * Convenience overload of {@link #waitForUnchecked(BaseElement, Duration)} using the current
     * driver's configured timeout.
     *
     * @param element the element to check
     */
    public static void waitForUnchecked(BaseElement element) {
        waitForUnchecked(element, DriverRunner.getConfig().getTimeout());
    }
}
