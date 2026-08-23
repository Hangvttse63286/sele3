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

    /**
     * Initiate Configuration
     *
     * @param initial true -> default values are set
     */
    public Configuration(boolean initial) {
        if (initial) {
            setCapabilities(parseCapabilities(System.getProperty("capabilities", "{}")));
            setHeadless(Boolean.parseBoolean(System.getProperty("headless", "false")));
            setPlatform(Platform.fromString(System.getProperty("platform", "chrome")));
            setRemote(Boolean.parseBoolean(System.getProperty("remote", "false")));
            setRemoteUrl(System.getProperty("remoteUrl", "http://localhost:4444/wd/hub"));
            setStartMaximized(Boolean.parseBoolean(System.getProperty("startMaximized", "false")));
            setPageLoadStrategy(System.getProperty("pageLoadStrategy", "normal"));
            setTimeout(Duration.ofMillis(Long.parseLong(System.getProperty("timeout", "60000"))));
            setPollingInterval(Duration.ofMillis(Long.parseLong(System.getProperty("pollingInterval", "500"))));
            setBaseUrl(System.getProperty("baseUrl", "http://localhost:8080"));
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
