package com.sele3.waits;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sele3.drivers.DriverRunner;
import com.sele3.elements.BaseElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeleniumWait {
    
    private static final List<Class<? extends Throwable>> COMMON_EXCEPTIONS =
        List.of(
            StaleElementReferenceException.class,
            NoSuchElementException.class);

    private static final List<Class<? extends Throwable>> CLICK_EXCEPTIONS =
        Stream.concat(
                COMMON_EXCEPTIONS.stream(),
                Stream.of(
                    ElementClickInterceptedException.class,
                    ElementNotInteractableException.class,
                    InvalidElementStateException.class))
            .toList();

    
    /**
     * Builds a {@link WebDriverWait} using the current driver's configured timeout and polling
     * interval. Ignores nothing during polling; use {@link #getWebDriverWait(List)} to have the
     * wait ignore specific exceptions (e.g. {@link StaleElementReferenceException}) while polling.
     *
     * @return a new {@link WebDriverWait}
     */
    public static WebDriverWait getWebDriverWait() {
        return getWebDriverWait(DriverRunner.getConfig().getTimeout(), DriverRunner.getConfig().getPollingInterval(), null);
    }

    /**
     * Builds a {@link WebDriverWait} using the current driver's configured timeout and polling
     * interval, ignoring the given exceptions during polling so a condition that re-locates an
     * element (e.g. {@link ExpectedConditions#visibilityOfElementLocated}) retries instead of failing.
     *
     * @param ignoreExceptions the exception types to ignore while polling, or {@code null} to ignore none
     * @return a new {@link WebDriverWait}
     */
    public static WebDriverWait getWebDriverWait(List<Class<? extends Throwable>> ignoreExceptions) {
        return getWebDriverWait(DriverRunner.getConfig().getTimeout(), DriverRunner.getConfig().getPollingInterval(), ignoreExceptions);
    }

    /**
     * Builds a {@link WebDriverWait} using an explicit timeout and polling interval, ignoring the
     * given exceptions during polling so a condition that re-locates an element (e.g.
     * {@link ExpectedConditions#visibilityOfElementLocated}) retries instead of failing.
     *
     * @param timeout the maximum time to wait
     * @param pollingInterval how often to check the condition while waiting
     * @param ignoreExceptions the exception types to ignore while polling, or {@code null} to ignore none
     * @return a new {@link WebDriverWait}
     */
    public static WebDriverWait getWebDriverWait(Duration timeout, Duration pollingInterval, List<Class<? extends Throwable>> ignoreExceptions) {
        WebDriverWait wait = new WebDriverWait(DriverRunner.getWebDriver(), timeout, pollingInterval);
        if (ignoreExceptions != null) {
            wait.ignoreAll(ignoreExceptions);
        }
        return wait;
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
        return executeWait(DriverRunner.getConfig().getTimeout(), DriverRunner.getConfig().getPollingInterval(), null, condition);
    }

    /**
     * Waits, using the current driver's configured timeout and polling interval, until the given condition returns a
     * non-null/non-false result.
     *
     * @param ignoreExceptions the list of exceptions to ignore
     * @param condition the condition to evaluate against the {@link WebDriver}
     * @param <T> the result type of the condition
     * @return the result produced by {@code condition} once satisfied
     */
    public static <T> T executeWait(List<Class<? extends Throwable>> ignoreExceptions, Function<WebDriver, T> condition) {
        return executeWait(DriverRunner.getConfig().getTimeout(), DriverRunner.getConfig().getPollingInterval(), ignoreExceptions, condition);
    }

    /**
     * Waits, using an explicit timeout/polling interval, until the given condition returns a
     * non-null/non-false result.
     *
     * @param timeout the maximum time to wait
     * @param pollingInterval how often to check the condition while waiting
     * @param ignoreExceptions the list of exceptions to ignore
     * @param condition the condition to evaluate against the {@link WebDriver}
     * @param <T> the result type of the condition
     * @return the result produced by {@code condition} once satisfied
     * @throws RuntimeException wrapping the {@link TimeoutException} if {@code condition} never succeeds before {@code timeout} elapses
     */
    public static <T> T executeWait(Duration timeout, Duration pollingInterval, List<Class<? extends Throwable>> ignoreExceptions, Function<WebDriver, T> condition) {
        try {
            return getWebDriverWait(timeout, pollingInterval, ignoreExceptions).until(condition);
        } catch (TimeoutException e) {
            log.error("Error during wait execution", e);
            throw new RuntimeException(String.format("Timeout after %s", DriverRunner.getConfig().getTimeout()), e);
        }
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
     * @param element the element to search for
     * @return the found {@link WebElement}
     */
    public static WebElement waitForExist(BaseElement element) {
        return executeWait(COMMON_EXCEPTIONS, ExpectedConditions.presenceOfElementLocated(element.getLocator()));
    }

    /**
     * Waits until at least one element matching the locator is present in the DOM.
     *
     * @param element the element to search for
     * @return all matching {@link WebElement}s
     */
    public static List<WebElement> waitForAllExist(BaseElement element) {
        return executeWait(COMMON_EXCEPTIONS, ExpectedConditions.presenceOfAllElementsLocatedBy(element.getLocator()));
    }

    /**
     * Waits until an element matching the locator is present and visible.
     *
     * @param element the element to wait for
     * @return the visible {@link WebElement}
     */
    public static WebElement waitForVisible(BaseElement element) {
        return executeWait(COMMON_EXCEPTIONS, ExpectedConditions.visibilityOfElementLocated(element.getLocator()));
    }

    /**
     * Waits until all elements matching the locator are present and visible.
     *
     * @param element the element to wait for
     * @return the visible {@link WebElement}s
     */
    public static List<WebElement> waitForAllVisible(BaseElement element) {
        return executeWait(COMMON_EXCEPTIONS, ExpectedConditions.visibilityOfAllElementsLocatedBy(element.getLocator()));
    }

    /**
     * Waits until no element matching the locator is visible (or it is no longer present).
     *
     * @param element the element to search for
     */
    public static void waitForInvisible(BaseElement element) {
        executeWait(ExpectedConditions.invisibilityOfElementLocated(element.getLocator()));
    }

    /**
     * Waits until the given element is enabled.
     *
     * @param element the element to check
     */
    public static void waitForEnabled(BaseElement element) {
        executeWait(COMMON_EXCEPTIONS, driver -> driver.findElement(element.getLocator()).isEnabled());
    }

    /**
     * Waits until the given element is disabled.
     *
     * @param element the element to check
     */
    public static void waitForDisabled(BaseElement element) {
        executeWait(COMMON_EXCEPTIONS, driver -> !driver.findElement(element.getLocator()).isEnabled());
    }

    /**
     * Waits until an element matching the locator is visible and enabled.
     *
     * @param element the element to search for
     * @return the clickable {@link WebElement}
     */
    public static WebElement waitForClickable(BaseElement element) {
        return executeWait(CLICK_EXCEPTIONS, ExpectedConditions.elementToBeClickable(element.getLocator()));
    }

    /**
     * Waits until the element's {@code value} attribute equals the given value.
     *
     * @param element the element to check
     * @param value the expected value
     */
    public static void waitForValueEquals(BaseElement element, String value) {
        executeWait(COMMON_EXCEPTIONS, driver -> driver.findElement(element.getLocator()).getAttribute("value").equals(value));
    }

    /**
     * Waits until the element's {@code value} attribute no longer equals the given value.
     *
     * @param element the element to check
     * @param value the value expected to no longer match
     */
    public static void waitForValueNotEquals(BaseElement element, String value) {
        executeWait(COMMON_EXCEPTIONS, driver -> !driver.findElement(element.getLocator()).getAttribute("value").equals(value));
    }

    /**
     * Waits until the element's {@code value} attribute contains the given text.
     *
     * @param element the element to check
     * @param value the substring expected to appear in the value
     */
    public static void waitForValueContains(BaseElement element, String value) {
        executeWait(COMMON_EXCEPTIONS, driver -> driver.findElement(element.getLocator()).getAttribute("value").contains(value));
    }

    /**
     * Waits until the element's visible text equals the given text.
     *
     * @param element the element to check
     * @param text the expected text
     */
    public static void waitForTextEquals(BaseElement element, String text) {
        executeWait(COMMON_EXCEPTIONS, driver -> driver.findElement(element.getLocator()).getText().equals(text));
    }

    /**
     * Waits until the element's visible text no longer equals the given text.
     *
     * @param element the element to check
     * @param text the text expected to no longer match
     */
    public static void waitForTextNotEquals(BaseElement element, String text) {
        executeWait(COMMON_EXCEPTIONS, driver -> !driver.findElement(element.getLocator()).getText().equals(text));
    }

    /**
     * Waits until the element matching the locator contains the given text.
     *
     * @param element the element to check
     * @param text the substring expected to appear in the element's text
     */
    public static void waitForTextContains(BaseElement element, String text) {
        executeWait(COMMON_EXCEPTIONS, ExpectedConditions.textToBePresentInElementLocated(element.getLocator(), text));
    }

    /**
     * Waits until the given attribute on the element equals the given value.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the expected value
     */
    public static void waitForAttributeEquals(BaseElement element, String attribute, String value) {
        executeWait(COMMON_EXCEPTIONS, driver -> driver.findElement(element.getLocator()).getAttribute(attribute).equals(value));
    }

    /**
     * Waits until the given attribute on the element no longer equals the given value.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the value expected to no longer match
     */
    public static void waitForAttributeNotEquals(BaseElement element, String attribute, String value) {
        executeWait(COMMON_EXCEPTIONS, driver -> !driver.findElement(element.getLocator()).getAttribute(attribute).equals(value));
    }

    /**
     * Waits until the given attribute on the element contains the given value.
     *
     * @param element the element to check
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     */
    public static void waitForAttributeContains(BaseElement element, String attribute, String value) {
        executeWait(COMMON_EXCEPTIONS, ExpectedConditions.attributeContains(element.getLocator(), attribute, value));
    }

    /**
     * Waits until the {@code <select>} element matching the locator has its {@code <option>}
     * children populated.
     *
     * @param element the {@code <select>} element to check
     */
    public static void waitForSelectOptionsLoaded(BaseElement element) {
        executeWait(CLICK_EXCEPTIONS, ExpectedConditions.presenceOfNestedElementsLocatedBy(element.getLocator(), By.tagName("option")));
    }

    /**
     * Waits until the given element is selected/checked.
     *
     * @param element the element to check
     */
    public static void waitForChecked(BaseElement element) {
        executeWait(CLICK_EXCEPTIONS, driver -> driver.findElement(element.getLocator()).isSelected());
    }

    /**
     * Waits until the given element is deselected/unchecked.
     *
     * @param element the element to check
     */
    public static void waitForUnchecked(BaseElement element) {
        executeWait(CLICK_EXCEPTIONS, driver -> !driver.findElement(element.getLocator()).isSelected());
    }
}
