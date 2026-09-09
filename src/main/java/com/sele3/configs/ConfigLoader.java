package com.sele3.configs;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;

import org.openqa.selenium.MutableCapabilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sele3.drivers.IPlatform;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigLoader {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(MutableCapabilities.class, new CapabilitiesAdapter())
            .registerTypeAdapter(IPlatform.class, new PlatformAdapter())
            .create();

    private static final String CONFIG_FILE_PATH = System.getProperty(ConfigKey.CONFIG_SOURCE_PATH, "src/main/resources/configs/") + System.getProperty(ConfigKey.CONFIG_FILE_NAME, "chrome.json");

    /**
     * Loads a {@link Configuration} from the {@value ConfigKey#CONFIG_SOURCE_PATH}/
     * {@value ConfigKey#CONFIG_FILE_NAME} JSON file (default: {@code src/main/resources/configs/chrome.json}),
     * then applies any explicitly-set system properties on top via
     * {@link Configuration#updateFromSystemProperties()} — a system property overrides the file
     * for that one field, but every other field the file set is left as-is.
     *
     * @return the resolved {@link Configuration}
     */
    public static Configuration loadConfig() {
        Configuration config = readConfigFromJsonFile(CONFIG_FILE_PATH);
        config.updateFromSystemProperties();
        return config;
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
