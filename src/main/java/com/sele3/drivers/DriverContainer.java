package com.sele3.drivers;

import org.openqa.selenium.WebDriverException;

import com.sele3.configs.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriverContainer {
    private final ThreadLocal<Driver> threadDriver = new ThreadLocal<>();

    /**
     * Creates a new {@link Driver} for the given configuration and binds it to the current thread.
     *
     * @param config the test run configuration
     */
    public void initialize(Configuration config) {
        log.info("Initializing Driver");
        Driver driver = new Driver(config);
        driver.createDriver();
        log.debug("Driver: {}", driver);
        threadDriver.set(driver);
    }

    /**
     * Returns the {@link Driver} bound to the current thread.
     *
     * @return the current thread's {@link Driver}
     * @throws RuntimeException if no driver has been bound via {@link #initialize(Configuration)}
     */
    public Driver getDriver() {
        if (threadDriver.get() == null) {
            throw new RuntimeException("No driver is bound to current thread. You need to initialize the driver first.");
        }
        return threadDriver.get();
    }

    /**
     * Quits the current thread's underlying WebDriver, if one was created, and removes the
     * thread-local binding regardless of whether quitting succeeds.
     *
     * @throws RuntimeException if an error occurs while quitting the driver
     */
    public void quit() {
        try {
            Driver driver = threadDriver.get();
            if (driver != null && driver.getDriver() != null) {
                log.info("Quitting driver");
                driver.getDriver().quit();
            }
        } catch (WebDriverException e) {
            throw new RuntimeException("Error occurs when trying to quit driver", e);
        } finally {
            threadDriver.remove();
        }
    }
}
