package com.sele3.drivers;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.sele3.configs.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirefoxDriverFactory implements IDriverFactory<FirefoxOptions> {

    @Override
    public IPlatform getPlatform() {
        return Platform.FIREFOX;
    }

    /**
     * Creates a {@link FirefoxDriver} instance. Selenium Manager resolves and downloads a
     * matching geckodriver binary automatically.
     *
     * @param options the driver options, from this same factory's {@link #getOptions(Configuration)}
     * @return the created {@link FirefoxDriver}
     */
    @Override
    public WebDriver createDriver(FirefoxOptions options) {
        return new FirefoxDriver(options);
    }

    /**
     * Builds {@link FirefoxOptions} from the given {@link Configuration}: applies headless mode,
     * window size, and page load strategy, then merges any extra capabilities. Firefox has no
     * CLI flag for starting maximized; see {@link DriverRunner#initDriver} for that handling.
     *
     * @param config the test run configuration
     * @return the populated {@link FirefoxOptions}
     */
    @Override
    public FirefoxOptions getOptions(Configuration config) {
        FirefoxOptions options = new FirefoxOptions();

        if (config.isHeadless()) {
            options.addArguments("-headless");
        }
        if (config.getWindowSize() != null && !config.getWindowSize().isEmpty()) {
            String[] size = config.getWindowSize().split(",");
            options.addArguments("-width", size[0].trim());
            options.addArguments("-height", size[1].trim());
        }

        options.setPageLoadStrategy(PageLoadStrategy.fromString(config.getPageLoadStrategy()));

        if (config.getCapabilities() != null) {
            options.merge(config.getCapabilities());
        }

        return options;
    }
}
