package com.sele3.drivers;

import java.util.HashMap;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

import com.sele3.configs.Configuration;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EdgeDriverFactory implements IDriverFactory {

    /**
     * Sets up EdgeDriver via WebDriverManager and creates an {@link EdgeDriver} instance.
     *
     * @param options the driver options, expected to be an {@link EdgeOptions}
     * @return the created {@link EdgeDriver}
     * @throws IllegalArgumentException if {@code options} is not an {@link EdgeOptions}
     */
    @Override
    public WebDriver createDriver(AbstractDriverOptions<?> options) {
        log.debug("Edge options: {}", options);
        if (options instanceof EdgeOptions edgeOptions) {
            WebDriverManager.edgedriver().setup();
            return new EdgeDriver(edgeOptions);
        }
        throw new IllegalArgumentException("Invalid options for EdgeDriver");
    }

    /**
     * Builds {@link EdgeOptions} from the given {@link Configuration}, applying headless,
     * security, and password-manager preferences before merging any extra capabilities.
     *
     * @param config the test run configuration
     * @return the populated {@link EdgeOptions}
     */
    @Override
    public AbstractDriverOptions<?> getOptions(Configuration config) {
        EdgeOptions options = new EdgeOptions();

        if (config.isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        options.addArguments("--disable-gpu", "--disable-dev-shm-usage", "--disable-web-security",
                "--allow-file-access-from-file", "--remote-allow-origin=*");

        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("autofill.profile_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        options.setPageLoadStrategy(PageLoadStrategy.fromString(config.getPageLoadStrategy()));
        options.setAcceptInsecureCerts(true);

        if (config.getCapabilities() != null) {
            options.merge(config.getCapabilities());
        }
        return options;
    }
}
