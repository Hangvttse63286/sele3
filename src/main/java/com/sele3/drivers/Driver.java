package com.sele3.drivers;

import org.openqa.selenium.WebDriver;

import com.sele3.configs.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Driver {
    private WebDriver driver;
    private Configuration config;

    /**
     * Creates a Driver bound to the given configuration.
     *
     * @param config the test run configuration to use when the underlying {@link WebDriver} is created
     */
    public Driver(Configuration config) {
        this.config = config;
    }

    /**
     * Creates and stores the underlying {@link WebDriver} using this driver's configuration.
     */
    public void createDriver() {
        this.driver = DriverFactory.createWebDriver(this.config);
    }

    /**
     * Returns the underlying {@link WebDriver}.
     *
     * @return the initialized {@link WebDriver}
     * @throws RuntimeException if {@link #createDriver()} has not been called yet
     */
    public synchronized WebDriver getWebDriver() {
        if (this.driver == null) {
            throw new RuntimeException("Driver is not initialized");
        }
        return this.driver;
    }
}
