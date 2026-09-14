package com.sele3.waits;

import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sele3.drivers.DriverRunner;
import com.sele3.elements.BaseElement;

import lombok.extern.slf4j.Slf4j;

/**
 * Element-independent {@link WebDriverWait} helpers (page load, jQuery activity, current URL,
 * arbitrary conditions) built around the current driver's configured timeout and polling
 * interval. See {@link ElementWait} for the element-bound counterparts (visibility, text,
 * attributes, etc.). Each wait method uses this instance's own {@link #wait}, so nesting one
 * of these inside another wait or a {@link RetryAction#retry} action adds that call's own full
 * timeout budget on top of the outer one rather than sharing a deadline with it.
 */
@Slf4j
public class SeleniumWait extends WebDriverWait{

    /**
     * Creates a {@link SeleniumWait} with no bound {@link BaseElement}, using the current
     * driver's configured timeout and polling interval. Only the element-independent waits
    * (e.g. {@link #untilPageToLoad}, {@link #untilUrlContains}) can be used until an element
    * is set.
     */
    public SeleniumWait() {
        super(DriverRunner.getWebDriver(), DriverRunner.getConfig().getTimeout(), DriverRunner.getConfig().getPollingInterval());
    }

    /**
     * Creates a {@link SeleniumWait} with no bound {@link BaseElement}, using the given timeout
     * and polling interval instead of the driver's configured defaults.
     *
     * @param timeout the maximum time to wait
     * @param pollingInterval how often to re-evaluate the condition while waiting
     */
    public SeleniumWait(Duration timeout, Duration pollingInterval) {
        super(DriverRunner.getWebDriver(), timeout, pollingInterval);
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
