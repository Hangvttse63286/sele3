package com.sele3.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

    /**
     * Reads the test data array for a given test name from a JSON data file.
     *
     * @param testName the key in the JSON file whose array holds this test's data sets
     * @param dataFilePath path to the JSON data file
     * @return a {@code Object[][]} with one row per data set, each row holding a single
     *         {@code Map<String, String>} entry; an empty array if the test name is not found
     */
    public static Object[][] getDataFromJson(String testName, String dataFilePath) {

        Object[][] data = new Object[0][1];

        //Read json file data using Gson library
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(dataFilePath));
        } catch (FileNotFoundException e) {
            log.error("Json file is not found: {}", dataFilePath);
        }
        JsonElement jsonElement = JsonParser.parseReader(br);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        //Check for the test name in the json file
        boolean blnTCExist = jsonObject.has(testName);
        if (!blnTCExist) {
            log.error("{} is not present in the data.json file - {}", testName, dataFilePath);
            return data;
        }

        //Get test data for the specific test case
        JsonArray jsonArray = jsonObject.getAsJsonArray(testName);
        data = jsonArrayToObjectArray(jsonArray);
        return data;
    }

    /**
     * Converts a {@link JsonArray} of objects into a TestNG-style {@code Object[][]} data provider,
     * with each element deserialized as a {@code Map<String, String>}.
     *
     * @param jsonArray the JSON array to convert
     * @return a {@code Object[][]} with one row per element of {@code jsonArray}
     */
    public static Object[][] jsonArrayToObjectArray(JsonArray jsonArray) {

        Object[][] data = new Object[0][1];
        int index = 0;
        Gson gson = new Gson();

        if (!jsonArray.isEmpty()) {
            data = new Object[jsonArray.size()][1];
            for (JsonElement obj : jsonArray) {
                HashMap<String, String> hashMap = new LinkedHashMap<>();
                data[index][0] = gson.fromJson(obj, hashMap.getClass());
                index++;
            }
        }
        return data;
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
    public static <T> T fromJsonFile(String jsonFile, Class<T> clazz) {
        log.info("Json file: {}", jsonFile);
        JsonReader jsonReader = null;

        try {
            jsonReader = new JsonReader(new FileReader(jsonFile));
        } catch (FileNotFoundException e) {
            log.error("Json file is not found: {}", jsonFile);
        }

        if (jsonReader == null) {
            throw new RuntimeException("Json file is not found");
        } else {
            Gson gson = new Gson();
            return gson.fromJson(jsonReader, clazz);
        }
    }
}
