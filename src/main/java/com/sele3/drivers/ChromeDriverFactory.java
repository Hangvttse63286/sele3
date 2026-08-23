package com.sele3.drivers;

import java.util.HashMap;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

import com.sele3.configs.Configuration;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromeDriverFactory implements IDriverFactory {

    /**
     * Sets up ChromeDriver via WebDriverManager and creates a {@link ChromeDriver} instance.
     *
     * @param options the driver options, expected to be a {@link ChromeOptions}
     * @return the created {@link ChromeDriver}
     * @throws IllegalArgumentException if {@code options} is not a {@link ChromeOptions}
     */
    @Override
    public WebDriver createDriver(AbstractDriverOptions<?> options) {
        if (options instanceof ChromeOptions chromeOptions) {
            WebDriverManager.chromedriver().setup();
            return new ChromeDriver(chromeOptions);
        }
        throw new IllegalArgumentException("Invalid options for ChromeDriver");
    }

    /**
     * Builds {@link ChromeOptions} from the given {@link Configuration}, applying headless,
     * security, and password-manager preferences before merging any extra capabilities.
     *
     * @param config the test run configuration
     * @return the populated {@link ChromeOptions}
     */
    @Override
    public AbstractDriverOptions<?> getOptions(Configuration config) {
        ChromeOptions options = new ChromeOptions();

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
