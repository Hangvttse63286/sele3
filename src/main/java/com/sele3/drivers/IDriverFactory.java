package com.sele3.drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.AbstractDriverOptions;

import com.sele3.configs.Configuration;

public interface IDriverFactory {

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
     *        factory's {@link #getOptions(Configuration)} — implementations cast back to their
     *        own concrete options type, which is always safe since {@link DriverFactory} never
     *        pairs one factory's options with another factory's {@code createDriver}
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
