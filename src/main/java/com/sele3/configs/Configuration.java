package com.sele3.configs;

import java.time.Duration;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.json.Json;

import com.sele3.drivers.IPlatform;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Configuration {
    private IPlatform platform;
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
            updateFromSystemProperties();
        }
    }

    /**
     * Parses a JSON-formatted "capabilities" string (e.g. {"browserVersion":"120"}) into a
     * {@link MutableCapabilities} instance.
     */
    private static MutableCapabilities parseCapabilities(String json) {
        Map<String, Object> rawCapabilities = new Json().toType(json, Map.class);
        return new MutableCapabilities(rawCapabilities);
    }

    /**
     * Applies any explicitly-set system properties on top of this configuration's current
     * values — e.g. as already loaded from a JSON file via {@link ConfigLoader} — falling back to
     * a hardcoded default only for fields still unset after that. A system property, when
     * present, always wins; a field this configuration already has a value for is otherwise left
     * alone rather than silently overwritten with the hardcoded default, since that would make
     * loading a config file pointless — every field it set would be immediately clobbered by
     * whichever default happened to apply.
     */
    public void updateFromSystemProperties() {
        setCapabilities(resolveCapabilities());
        setHeadless(resolveBoolean(ConfigKey.HEADLESS, headless));
        setPlatform(resolvePlatform());
        setRemote(resolveBoolean(ConfigKey.REMOTE, remote));
        setRemoteUrl(resolveString(ConfigKey.REMOTE_URL, remoteUrl, "http://localhost:4444/wd/hub"));
        setStartMaximized(resolveBoolean(ConfigKey.START_MAXIMIZED, startMaximized));
        setPageLoadStrategy(resolveString(ConfigKey.PAGE_LOAD_STRATEGY, pageLoadStrategy, "normal"));
        setTimeout(resolveDuration(ConfigKey.TIMEOUT, timeout, 60000));
        setPollingInterval(resolveDuration(ConfigKey.POLLING_INTERVAL, pollingInterval, 500));
        setBaseUrl(resolveString(ConfigKey.BASE_URL, baseUrl, "http://localhost:8080"));
        setWindowSize(resolveString(ConfigKey.WINDOW_SIZE, windowSize, "1920,1080"));
    }

    /** Resolves a {@code String} field: the {@code key} system property if set, else {@code currentValue}, else {@code hardcodedDefault}. */
    private static String resolveString(String key, String currentValue, String hardcodedDefault) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return systemProperty;
        }
        return currentValue != null ? currentValue : hardcodedDefault;
    }

    /** Resolves a {@code boolean} field: the {@code key} system property if set, else {@code currentValue} unchanged. */
    private static boolean resolveBoolean(String key, boolean currentValue) {
        String systemProperty = System.getProperty(key);
        return systemProperty != null ? Boolean.parseBoolean(systemProperty) : currentValue;
    }

    /** Resolves a {@link Duration} field: the {@code key} system property (millis) if set, else {@code currentValue}, else {@code hardcodedDefaultMillis}. */
    private static Duration resolveDuration(String key, Duration currentValue, long hardcodedDefaultMillis) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return Duration.ofMillis(Long.parseLong(systemProperty));
        }
        return currentValue != null ? currentValue : Duration.ofMillis(hardcodedDefaultMillis);
    }

    /** Resolves {@link #capabilities}: the {@value ConfigKey#CAPABILITIES} system property if set, else the current value, else empty. */
    private MutableCapabilities resolveCapabilities() {
        String systemProperty = System.getProperty(ConfigKey.CAPABILITIES);
        if (systemProperty != null) {
            return parseCapabilities(systemProperty);
        }
        return capabilities != null ? capabilities : new MutableCapabilities();
    }

    /** Resolves {@link #platform}: the {@value ConfigKey#PLATFORM} system property if set, else the current value, else {@code "chrome"}. */
    private IPlatform resolvePlatform() {
        String systemProperty = System.getProperty(ConfigKey.PLATFORM);
        if (systemProperty != null) {
            return IPlatform.fromString(systemProperty);
        }
        return platform != null ? platform : IPlatform.fromString("chrome");
    }
}
