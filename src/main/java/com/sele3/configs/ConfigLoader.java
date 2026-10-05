package com.sele3.configs;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.openqa.selenium.MutableCapabilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sele3.adapters.CapabilitiesAdapter;
import com.sele3.adapters.DurationAdapter;
import com.sele3.adapters.PlatformAdapter;
import com.sele3.drivers.IPlatform;
import com.sele3.drivers.Platform;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigLoader {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(MutableCapabilities.class, new CapabilitiesAdapter())
            .registerTypeAdapter(IPlatform.class, new PlatformAdapter())
            .create();

    private static final String CONFIG_SOURCE_PATH = System.getProperty(ConfigKey.CONFIG_SOURCE_PATH, "src/main/resources/configs/");
    /** Classpath folder of the framework's bundled default config files. */
    private static final String CLASSPATH_CONFIG_DIR = "configs/";

    /**
     * Loads a {@link Configuration} from a JSON file, then applies any explicitly-set system
     * properties on top via {@link Configuration#updateFromSystemProperties()} — a system property
     * overrides the file for that one field, but every other field the file set is left as-is.
     *
     * <p>The file is the {@value ConfigKey#CONFIG_SOURCE_PATH} system property (default
     * {@code src/main/resources/configs/}) followed by the {@value ConfigKey#CONFIG_FILE_NAME}
     * system property. When that isn't set, the file is chosen from the platform: the
     * {@value ConfigKey#PLATFORM} system property's {@link IPlatform#getDefaultConfigFile()}, or
     * {@code chrome.json} if no platform is set either. So {@code -Dplatform=firefox} loads
     * {@code firefox.json}, with its own browser-specific {@code capabilities}.
     *
     * <p>If that file doesn't exist on disk (e.g. a project using this framework as a dependency
     * hasn't created its own), the framework's bundled default with the same name is loaded from
     * the classpath ({@code configs/<file name>}) instead.
     *
     * @return the resolved {@link Configuration}
     * @throws IllegalArgumentException if the {@value ConfigKey#PLATFORM} system property names an
     *         unregistered platform
     * @throws RuntimeException if the file exists neither on disk nor on the classpath
     */
    public static Configuration loadConfig() {
        String platformName = System.getProperty(ConfigKey.PLATFORM);
        IPlatform platform = platformName != null ? IPlatform.fromString(platformName) : Platform.CHROME;
        String fileName = System.getProperty(ConfigKey.CONFIG_FILE_NAME, platform.getDefaultConfigFile());
        String configFilePath = CONFIG_SOURCE_PATH + fileName;
        String resource = CLASSPATH_CONFIG_DIR + fileName;

        Configuration config;
        if (Files.exists(Path.of(configFilePath))) {
            config = readConfigFromJsonFile(configFilePath);
        } else if (ConfigLoader.class.getClassLoader().getResource(resource) != null) {
            config = readConfigFromClasspath(resource);
        } else {
            throw new RuntimeException("Config file '" + fileName + "' not found: looked for '" + configFilePath
                    + "' on disk and '" + resource + "' on the classpath. Create one of them, or set -D"
                    + ConfigKey.CONFIG_FILE_NAME + " / -D" + ConfigKey.CONFIG_SOURCE_PATH + ".");
        }
        config.updateFromSystemProperties();
        return config;
    }

    /**
     * Reads a {@link Configuration} from a JSON resource on the classpath, e.g. one of the
     * framework's bundled defaults.
     *
     * @param resource the resource path, e.g. {@code configs/chrome.json}
     * @return the deserialized {@link Configuration}
     * @throws RuntimeException if the resource isn't on the classpath
     * @throws UncheckedIOException if the resource can't be read
     */
    public static Configuration readConfigFromClasspath(String resource) {
        URL url = ConfigLoader.class.getClassLoader().getResource(resource);
        if (url == null) {
            throw new RuntimeException("Config file not found on the classpath: " + resource);
        }
        // The URL shows where it came from: the framework's jar, or the project's own resources.
        log.info("Using config from the classpath: {}", url);
        try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, Configuration.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read config file: " + url, e);
        }
    }

    /**
     * Reads a {@link Configuration} from a JSON file.
     *
     * @param jsonFile path to the JSON configuration file
     * @return the deserialized {@link Configuration}
     */
    public static Configuration readConfigFromJsonFile(String jsonFile) {
        return fromJsonFile(jsonFile, Configuration.class);
    }

    /**
     * Deserializes a JSON file into an instance of the given class.
     *
     * @param jsonFile path to the JSON file
     * @param clazz the target type to deserialize into
     * @param <T> the target type
     * @return the deserialized instance
     * @throws RuntimeException if the JSON file is not found
     */
    private static <T> T fromJsonFile(String jsonFile, Class<T> clazz) {
        log.info("Json file: {}", jsonFile);
        JsonReader jsonReader = null;

        try {
            jsonReader = new JsonReader(new FileReader(jsonFile));
        } catch (FileNotFoundException e) {
            log.error("Json file is not found: {}", jsonFile);
            throw new RuntimeException("Json file is not found");
        }

        return gson.fromJson(jsonReader, clazz);
    }
}
