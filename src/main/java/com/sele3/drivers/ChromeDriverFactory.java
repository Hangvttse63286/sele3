package com.sele3.drivers;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

import com.sele3.configs.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromeDriverFactory implements IDriverFactory {

    @Override
    public IPlatform getPlatform() {
        return Platform.CHROME;
    }

    /**
     * Creates a {@link ChromeDriver} instance. Selenium Manager resolves and downloads a
     * matching chromedriver binary automatically.
     *
     * @param options the driver options, always a {@link ChromeOptions} from this same factory's {@link #getOptions(Configuration)}
     * @return the created {@link ChromeDriver}
     */
    @Override
    public WebDriver createDriver(AbstractDriverOptions<?> options) {
        return new ChromeDriver((ChromeOptions) options);
    }

    /**
     * Builds {@link ChromeOptions} from the given {@link Configuration}: applies headless mode,
     * window size, start-maximized (when not headless), and page load strategy, then merges any
     * extra capabilities.
     *
     * @param config the test run configuration
     * @return the populated {@link ChromeOptions}
     */
    @Override
    public ChromeOptions getOptions(Configuration config) {
        ChromeOptions options = new ChromeOptions();

        if (config.isHeadless()) {
            options.addArguments("--headless=new");
        }
        if (config.getWindowSize() != null && !config.getWindowSize().isEmpty()) {
            options.addArguments("--window-size=" + config.getWindowSize());
        }
        if (config.isStartMaximized() && !config.isHeadless()) {
            options.addArguments("--start-maximized");
        }

        options.setPageLoadStrategy(PageLoadStrategy.fromString(config.getPageLoadStrategy()));

        if (config.getCapabilities() != null) {
            options.merge(config.getCapabilities());
        }
        return options;
    }
}
