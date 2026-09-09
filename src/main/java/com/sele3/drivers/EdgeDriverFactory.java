package com.sele3.drivers;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

import com.sele3.configs.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EdgeDriverFactory implements IDriverFactory {

    @Override
    public IPlatform getPlatform() {
        return Platform.EDGE;
    }

    /**
     * Creates an {@link EdgeDriver} instance. Selenium Manager resolves and downloads a
     * matching msedgedriver binary automatically.
     *
     * @param options the driver options, always an {@link EdgeOptions} from this same factory's {@link #getOptions(Configuration)}
     * @return the created {@link EdgeDriver}
     */
    @Override
    public WebDriver createDriver(AbstractDriverOptions<?> options) {
        log.debug("Edge options: {}", options);
        return new EdgeDriver((EdgeOptions) options);
    }

    /**
     * Builds {@link EdgeOptions} from the given {@link Configuration}: applies headless mode,
     * window size, start-maximized (when not headless), and page load strategy, then merges any
     * extra capabilities.
     *
     * @param config the test run configuration
     * @return the populated {@link EdgeOptions}
     */
    @Override
    public EdgeOptions getOptions(Configuration config) {
        EdgeOptions options = new EdgeOptions();

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
