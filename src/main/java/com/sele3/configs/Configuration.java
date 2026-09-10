package com.sele3.configs;

import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.json.Json;

import com.sele3.drivers.Platform;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Configuration {
    private Platform platform;
    private boolean headless;
    private boolean remote;
    private String remoteUrl;
    private boolean startMaximized;
    private String pageLoadStrategy;
    private MutableCapabilities capabilities;
    private Duration timeout;
    private Duration pollingInterval;
    private String baseUrl;
    private String windowSize;

    /**
     * Initiate Configuration
     *
     * @param initial true -> default values are set
     */
    public Configuration(boolean initial) {
        if (initial) {
            setCapabilities(parseCapabilities(System.getProperty(ConfigKey.CAPABILITIES, "{}")));
            setHeadless(Boolean.parseBoolean(System.getProperty(ConfigKey.HEADLESS, "false")));
            setPlatform(Platform.fromString(System.getProperty(ConfigKey.PLATFORM, "chrome")));
            setRemote(Boolean.parseBoolean(System.getProperty(ConfigKey.REMOTE, "false")));
            setRemoteUrl(System.getProperty(ConfigKey.REMOTE_URL, "http://localhost:4444/wd/hub"));
            setStartMaximized(Boolean.parseBoolean(System.getProperty(ConfigKey.START_MAXIMIZED, "false")));
            setPageLoadStrategy(System.getProperty(ConfigKey.PAGE_LOAD_STRATEGY, "normal"));
            setTimeout(Duration.ofMillis(Long.parseLong(System.getProperty(ConfigKey.TIMEOUT, "60000"))));
            setPollingInterval(Duration.ofMillis(Long.parseLong(System.getProperty(ConfigKey.POLLING_INTERVAL, "500"))));
            setBaseUrl(System.getProperty(ConfigKey.BASE_URL, "http://localhost:8080"));
            setWindowSize(System.getProperty(ConfigKey.WINDOW_SIZE, "1920,1080"));
        }
    }

    /**
     * Parses a JSON-formatted "capabilities" system property (e.g. {"browserVersion":"120"})
     * into a {@link MutableCapabilities} instance.
     */
    private static MutableCapabilities parseCapabilities(String json) {
        Map<String, Object> rawCapabilities = new Json().toType(json, Map.class);
        return new MutableCapabilities(rawCapabilities);
    }
}
