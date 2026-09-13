package com.sele3.waits;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sele3.drivers.DriverRunner;
import com.sele3.elements.BaseElement;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Element-independent {@link WebDriverWait} helpers (page load, jQuery activity, current URL,
 * arbitrary conditions) built around the current driver's configured timeout and polling
 * interval. See {@link ElementWait} for the element-bound counterparts (visibility, text,
 * attributes, etc.). Each {@code waitFor*} call uses this instance's own {@link #wait}, so
 * nesting one of these inside another wait or a {@link RetryAction#retry} action adds that
 * call's own full timeout budget on top of the outer one rather than sharing a deadline with it.
 */
@Slf4j
@Getter
public class SeleniumWait {
    protected WebDriverWait wait;

    /**
     * Creates a {@link SeleniumWait} with no bound {@link BaseElement}, using the current
     * driver's configured timeout and polling interval. Only the element-independent waits
     * (e.g. {@link #waitFor}, {@link #pageToLoad}) can be used until an element is set.
     */
    public SeleniumWait() {
        this.wait = createWebDriverWait(DriverRunner.getConfig().getTimeout(), DriverRunner.getConfig().getPollingInterval());
    }

    /**
     * Creates a {@link SeleniumWait} with no bound {@link BaseElement}, using the given timeout
     * and polling interval instead of the driver's configured defaults.
     *
     * @param timeout the maximum time to wait
     * @param pollingInterval how often to re-evaluate the condition while waiting
     */
    public SeleniumWait(Duration timeout, Duration pollingInterval) {
        this.wait = createWebDriverWait(timeout, pollingInterval);
    }
     
    /**
     * Builds a {@link WebDriverWait} using the current driver's configured timeout and polling
     * interval.
     *
     * @return a new {@link WebDriverWait}
     */
    private WebDriverWait createWebDriverWait(Duration timeout, Duration pollingInterval) {
        return new WebDriverWait(DriverRunner.getWebDriver(), timeout, pollingInterval);
    }

    /**
     * Adds exception types that {@link #wait} should ignore (retry through) while polling,
     * in addition to the {@link org.openqa.selenium.TimeoutException} it always propagates.
     *
     * @param exceptions the exception types to ignore while polling
     */
    public void ignore(List<Class<? extends Throwable>> exceptions) {
        this.wait.ignoreAll(exceptions);
    }

    /**
     * Changes the timeout used by subsequent waits on this instance's {@link #wait}.
     *
     * @param timeout the new maximum time to wait
     */
    public void setTimeout(Duration timeout) {
        this.wait.withTimeout(timeout);
    }

    /**
     * Changes the polling interval used by subsequent waits on this instance's {@link #wait}.
     *
     * @param pollingInterval the new interval between condition re-evaluations
     */
    public void setPollingInterval(Duration pollingInterval) {
        this.wait.pollingEvery(pollingInterval);
    }

    /**
     * {@link #setTimeout(Duration)} and {@link #setPollingInterval(Duration)} together.
     *
     * @param timeout the new maximum time to wait
     * @param pollingInterval the new interval between condition re-evaluations
     */
    public void setTimeoutAndInterval(Duration timeout, Duration pollingInterval) {
        setTimeout(timeout);
        setPollingInterval(pollingInterval);
    }

    /**
     * Waits, using the default timeout/polling interval, until the given condition returns a
     * non-null/non-false result.
     *
     * @param condition the condition to evaluate against the {@link WebDriver}
     * @param <T> the result type of the condition
     * @return the result produced by {@code condition} once satisfied
     */
    public <T> T until(Function<WebDriver, T> condition) {
        return getWait().until(condition);
    }

    /**
     * {@link Supplier} variant of {@link #until(Function)}, for a condition that doesn't need
     * the {@link WebDriver}.
     *
     * @param action the condition to evaluate
     * @param <T> the result type of the condition
     * @return the result produced by {@code action} once satisfied
     */
    public <T> T until(Supplier<T> action) {
        return getWait().until(driver -> action.get());
    }

    /**
     * Waits until {@code document.readyState} reports "complete".
     */
    public void untilPageToLoad() {
        ExpectedCondition<Boolean> javascriptDone = d -> {
            try {
                return ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete");
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        };
        until(javascriptDone);
    }

    /**
     * Waits for {@code document.readyState} to report "loading", then waits for it to report
     * "complete". Useful for catching a page navigation that hasn't started yet.
     */
    public void untilPageToLoadingAndComplete() {
        ExpectedCondition<Boolean> javascriptLoading = d -> {
            try {
                return ((JavascriptExecutor) d).executeScript("return document.readyState").equals("loading");
            } catch (Exception e) {
                return Boolean.FALSE;
            }
        };
        until(javascriptLoading);
        untilPageToLoad();
    }

    /**
     * Waits until jQuery reports no active AJAX requests ({@code jQuery.active == 0}).
     * If jQuery is unavailable, the wait is treated as satisfied immediately.
     */
    public void untilJQueryToLoad() {
        ExpectedCondition<Boolean> jQueryDone = d -> {
            try {
                return ((Long) ((JavascriptExecutor) DriverRunner.getWebDriver()).executeScript("return jQuery.active") == 0);
            } catch (Exception e) {
                return true;
            }
        };
        until(jQueryDone);
    }

    /**
     * Waits for jQuery to start an AJAX request ({@code jQuery.active > 0}), then waits for it
     * to finish. Useful for catching a jQuery request that hasn't started yet.
     */
    public void untilJQueryToProcessAndLoad() {
        ExpectedCondition<Boolean> jQueryProcess = d -> {
            try {
                return ((Long) ((JavascriptExecutor) DriverRunner.getWebDriver()).executeScript("return jQuery.active") > 0);
            } catch (Exception e) {
                return true;
            }
        };
        until(jQueryProcess);
        untilJQueryToLoad();
    }

    /**
     * Waits until the current URL contains the given text.
     *
     * @param text the substring expected to appear in the current URL
     */
    public void untilUrlContains(String text) {
        until(ExpectedConditions.urlContains(text));
    }
}
