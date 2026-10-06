package com.sele3.drivers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ServiceLoader;

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
     * @throws IllegalArgumentException if the configured platform has no matching {@link IDriverFactory}
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

        return createWebDriver(findDriverFactory(config.getPlatform()), config);
    }

    /**
     * Finds the {@link IDriverFactory} whose {@link IDriverFactory#getPlatform()} matches
     * {@code platform}, discovered via {@link ServiceLoader} from every
     * {@code META-INF/services/com.sele3.drivers.IDriverFactory} entry on the classpath. Adding
     * support for a new {@link IPlatform} is therefore purely additive from outside this
     * package: implement {@link IDriverFactory} (with a public no-arg constructor) and list it in
     * that services file — nothing here needs to change.
     *
     * @throws IllegalArgumentException if no registered {@link IDriverFactory} matches {@code platform}
     */
    private static IDriverFactory<?> findDriverFactory(IPlatform platform) {
        return ServiceLoader.load(IDriverFactory.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(factory -> platform.name().equalsIgnoreCase(factory.getPlatform().name()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported platform: " + platform + ". Add a META-INF/services/"
                    + IDriverFactory.class.getName()
                    + " entry for an IDriverFactory implementation whose getPlatform() returns it."));
    }

    /**
     * Builds the driver-specific options and creates the {@link WebDriver}. Generic over the
     * factory's own options type {@code T} (rather than taking {@code IDriverFactory<?>}
     * directly) so that the {@code options} built by {@link IDriverFactory#getOptions} can be
     * passed straight into that same factory's {@link IDriverFactory#createDriver} with no cast —
     * the compiler ties both calls to the same captured {@code T} for this one invocation.
     *
     * @param driverFactory the browser-specific factory to use
     * @param config the test run configuration
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
