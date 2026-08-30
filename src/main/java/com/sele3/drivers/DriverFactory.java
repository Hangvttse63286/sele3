package com.sele3.drivers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.sele3.configs.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriverFactory {

    /**
     * Creates a {@link WebDriver} for the platform specified in the given configuration.
     * If {@code config.isRemote()} is true, a {@link RemoteWebDriver} pointed at
     * {@code config.getRemoteUrl()} is created instead of a local driver.
     *
     * @param config the test run configuration
     * @return the created {@link WebDriver}
     * @throws IllegalArgumentException if the configured platform is unsupported
     * @throws RuntimeException if {@code config.getRemoteUrl()} is not a valid URL
     */
    public static WebDriver createWebDriver(Configuration config) {
        log.debug("platform={}", config.getPlatform());
        log.debug("remote={}", config.isRemote());
        log.debug("remoteUrl={}", config.getRemoteUrl());
        log.debug("startMaximized={}", config.isStartMaximized());
        log.debug("capabilities={}", config.getCapabilities());
        log.debug("headless={}", config.isHeadless());
        log.debug("timeout={}", config.getTimeout());
        log.debug("pollingInterval={}", config.getPollingInterval());
        log.debug("pageLoadStrategy={}", config.getPageLoadStrategy());

        WebDriver driver = switch (config.getPlatform()) {
            case CHROME -> createWebDriver(new ChromeDriverFactory(), config);
            case FIREFOX -> createWebDriver(new FirefoxDriverFactory(), config);
            case EDGE -> createWebDriver(new EdgeDriverFactory(), config);
            default -> throw new IllegalArgumentException("Unsupported platform: " + config.getPlatform());
        };
        
        return driver;
    }

    /**
     * Builds the driver-specific options and creates the {@link WebDriver}, tying the options
     * type to the factory that produced it so no runtime type check is needed.
     *
     * @param driverFactory the browser-specific factory to use
     * @param config the test run configuration
     * @param <T> the driver-specific options type
     * @return the created {@link WebDriver}
     * @throws RuntimeException if {@code config.getRemoteUrl()} is not a valid URL
     */
    private static <T extends AbstractDriverOptions<?>> WebDriver createWebDriver(IDriverFactory<T> driverFactory, Configuration config) {
        T options = driverFactory.getOptions(config);

        if (config.isRemote()) {
            try {
                return new RemoteWebDriver(new URI(config.getRemoteUrl()).toURL(), options);
            } catch (MalformedURLException | URISyntaxException e) {
                throw new RuntimeException("Invalid remote url: " + config.getRemoteUrl(), e);
            }
        }
        return driverFactory.createDriver(options);
    }
}
