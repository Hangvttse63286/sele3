package com.sele3.drivers;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

import com.sele3.configs.Configuration;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirefoxDriverFactory implements IDriverFactory {

    /**
     * Sets up GeckoDriver via WebDriverManager and creates a {@link FirefoxDriver} instance.
     *
     * @param options the driver options, expected to be a {@link FirefoxOptions}
     * @return the created {@link FirefoxDriver}
     * @throws IllegalArgumentException if {@code options} is not a {@link FirefoxOptions}
     */
    @Override
    public WebDriver createDriver(AbstractDriverOptions<?> options) {
        if (options instanceof FirefoxOptions firefoxOptions) {
            WebDriverManager.firefoxdriver().setup();
            return new FirefoxDriver(firefoxOptions);
        }
        throw new IllegalArgumentException("Invalid options for FirefoxDriver");
    }

    /**
     * Builds {@link FirefoxOptions} from the given {@link Configuration}, applying headless
     * and password-manager preferences before merging any extra capabilities.
     *
     * @param config the test run configuration
     * @return the populated {@link FirefoxOptions}
     */
    @Override
    public AbstractDriverOptions<?> getOptions(Configuration config) {
        FirefoxOptions options = new FirefoxOptions();

        if (config.isHeadless()) {
            options.addArguments("-headless");
        }

        options.addPreference("signon.rememberSignons", false);
        options.addPreference("extensions.formautofill.addresses.enabled", false);

        options.setPageLoadStrategy(PageLoadStrategy.fromString(config.getPageLoadStrategy()));
        options.setAcceptInsecureCerts(true);

        if (config.getCapabilities() != null) {
            options.merge(config.getCapabilities());
        }

        return options;
    }
}
