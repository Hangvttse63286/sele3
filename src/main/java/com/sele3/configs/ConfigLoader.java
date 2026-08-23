package com.sele3.configs;


import com.sele3.utils.JsonUtils;

public class ConfigLoader {

    /**
     * Reads a {@link Configuration} from a JSON file.
     *
     * @param jsonFile path to the JSON configuration file
     * @return the deserialized {@link Configuration}
     */
    public static Configuration readConfigFromJsonFile(String jsonFile) {
        return JsonUtils.fromJsonFile(jsonFile, Configuration.class);
    }
}
