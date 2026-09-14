package com.sele3.drivers;

import java.time.Duration;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.sele3.configs.ConfigLoader;
import com.sele3.configs.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriverRunner {
    private static final DriverContainer driverContainer = new DriverContainer();

    /**
     * Initializes and binds a {@link WebDriver} to the current thread for the given configuration.
     * Chrome and Edge start maximized via a launch argument in their driver options; Firefox has
     * no equivalent CLI flag, so when {@code config.isStartMaximized()} is true and not headless,
     * its window is maximized here after launch instead.
     *
     * @param config the test run configuration
     */
    public static void initDriver(Configuration config) {
        driverContainer.initialize(config);

        if (Platform.FIREFOX.name().equalsIgnoreCase(config.getPlatform().name()) && config.isStartMaximized() && !config.isHeadless()) {
            getWebDriver().manage().window().maximize();
        }
    }

    /**
     * Returns the current thread's {@link WebDriver}.
     *
     * @return the current thread's {@link WebDriver}
     */
    public static WebDriver getWebDriver() {
        return getDriver().getWebDriver();
    }

    /**
     * Returns the {@link Configuration} of the current thread's driver.
     *
     * @return the current thread's {@link Configuration}
     */
    public static Configuration getConfig() {
        return getDriver().getConfig();
    }

    /**
     * Navigates the browser to the given URL.
     *
     * @param url the URL to navigate to
     */
    public static void open(String url) {
        log.info("Opening URL: {}", url);
        getWebDriver().navigate().to(url);
    }

    /**
     * Navigates the browser to the current configuration's base URL.
     *
     * @see #open(String)
     */
    public static void open() {
        if (!driverContainer.hasDriver()) {
            initDriver(ConfigLoader.loadConfig());
        }
        open(getConfig().getBaseUrl());
    }

    /**
     * Quits the current thread's driver and clears its thread-local binding.
     */
    public static void close() {
        driverContainer.quit();
    }

    /**
     * Captures a screenshot of the current page in the requested output format.
     *
     * @param type the desired {@link OutputType} (e.g. file, bytes, base64)
     * @param <T> the screenshot representation type
     * @return the screenshot in the requested format
     */
    public static <T> T takeScreenShot(OutputType<T> type) {
        return ((TakesScreenshot) getWebDriver()).getScreenshotAs(type);
    }

    /**
     * Maximizes the browser window.
     */
    public static void maximizeWindow() {
        getWebDriver().manage().window().maximize();
    }

    /**
     * Accepts the currently displayed browser alert/confirm/prompt dialog.
     */
    public static void acceptAlert() {
        getWebDriver().switchTo().alert().accept();
    }

    /**
     * Dismisses the currently displayed browser alert/confirm/prompt dialog.
     */
    public static void closeAlert() {
        getWebDriver().switchTo().alert().dismiss();
    }

    /**
     * Refreshes the current page.
     */
    public static void refreshPage() {
        getWebDriver().navigate().refresh();
    }

    /**
     * Returns the {@link Driver} bound to the current thread.
     *
     * @return the current thread's {@link Driver}
     */
    public static Driver getDriver() {
        return driverContainer.getDriver();
    }

    /**
     * Returns the current thread's {@link WebDriver} as a {@link RemoteWebDriver}.
     *
     * @return the current thread's {@link WebDriver}, cast to {@link RemoteWebDriver}
     * @throws ClassCastException if the current driver is not a {@link RemoteWebDriver}
     */
    public static RemoteWebDriver getRemoteWebDriver() {
        return (RemoteWebDriver) getWebDriver();
    }

    /**
     * Executes a JavaScript snippet in the context of the current page.
     *
     * @param script the JavaScript to execute
     * @param args arguments passed to the script, accessible via {@code arguments[n]}
     * @return the value returned by the script
     */
    public static Object executeJS(String script, Object... args) {
        return ((JavascriptExecutor) getWebDriver()).executeScript(script, args);
    }

    /**
     * Checks whether the current thread's driver is still responsive.
     *
     * @return {@code true} if the driver responds to a basic command, {@code false} otherwise
     */
    public static boolean isDriverAlive() {
        if (getWebDriver() == null) {
            return false;
        } else {
            try {
                getWebDriver().getCurrentUrl();
                return true;
            } catch (WebDriverException e) {
                return false;
            }
        }
    }

    /**
     * Sleeps the current thread for the given duration.
     *
     * @param duration how long to sleep
     */
    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            log.warn("Thread sleep interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Resizes the browser window.
     *
     * @param width the desired window width, in pixels
     * @param height the desired window height, in pixels
     */
    public static void setWindowSize(int width, int height) {
        getWebDriver().manage().window().setSize(new Dimension(width, height));
    }

    /**
     * Checks whether the current thread's driver is configured to run headless.
     *
     * @return {@code true} if headless mode is enabled in the current configuration
     */
    public static boolean isHeadless() {
        return getConfig().isHeadless();
    }
}
