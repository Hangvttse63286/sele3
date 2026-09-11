package com.sele3.drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.AbstractDriverOptions;

import com.sele3.configs.Configuration;

public interface IDriverFactory<T extends AbstractDriverOptions<?>> {

    /**
     * Returns the {@link IPlatform} that this factory produces a driver for.
     *
     * @return the platform
     */
    IPlatform getPlatform();

    /**
     * Creates a browser-specific {@link WebDriver} instance from the given options.
     *
     * @param options the driver options to launch the browser with, as returned by this same
     *        factory's {@link #getOptions(Configuration)}
     * @return the created {@link WebDriver}
     */
    WebDriver createDriver(T options);

    /**
     * Builds the browser-specific driver options from the given {@link Configuration}.
     *
     * @param config the test run configuration
     * @return the populated driver options
     */
    T getOptions(Configuration config);
}
