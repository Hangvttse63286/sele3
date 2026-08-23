package com.sele3.drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.AbstractDriverOptions;

import com.sele3.configs.Configuration;

public interface IDriverFactory {
    /**
     * Creates a browser-specific {@link WebDriver} instance from the given options.
     *
     * @param options the driver options to launch the browser with
     * @return the created {@link WebDriver}
     */
    WebDriver createDriver(AbstractDriverOptions<?> options);

    /**
     * Builds the browser-specific driver options from the given {@link Configuration}.
     *
     * @param config the test run configuration
     * @return the populated driver options
     */
    AbstractDriverOptions<?> getOptions(Configuration config);
}
