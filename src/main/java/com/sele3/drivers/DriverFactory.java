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

        IDriverFactory driverFactory;

        switch (config.getPlatform()) {
            case CHROME -> driverFactory = new ChromeDriverFactory();
            case FIREFOX -> driverFactory = new FirefoxDriverFactory();
            case EDGE -> driverFactory = new EdgeDriverFactory();
            default -> throw new IllegalArgumentException("Unsupported platform: " + config.getPlatform());
        }

        AbstractDriverOptions<?> options = driverFactory.getOptions(config);

        if (config.isRemote()) {
            try {
                return new RemoteWebDriver(new URI(config.getRemoteUrl()).toURL(), options);
            } catch (MalformedURLException | URISyntaxException e) {
                throw new RuntimeException("Invalid remote url: " + config.getRemoteUrl(), e);
            }
        } else {
            return driverFactory.createDriver(options);
        }
    }
}
