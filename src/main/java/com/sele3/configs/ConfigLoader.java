package com.sele3.configs;


import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigLoader {

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

        Gson gson = new Gson();
        return gson.fromJson(jsonReader, clazz);
    }
}
