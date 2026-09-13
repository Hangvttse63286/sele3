package com.sele3.adapters;

import java.lang.reflect.Type;
import java.time.Duration;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson type adapter for {@link Duration}, read/written as a plain JSON number of milliseconds.
 * Gson's default reflective (de)serialization can't handle {@link Duration} on its own — it has
 * no no-arg constructor and stores its value across two private fields ({@code seconds}/
 * {@code nanos}), not the single numeric value a config file naturally expresses.
 */
public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

    @Override
    public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toMillis());
    }

    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Duration.ofMillis(json.getAsLong());
    }
}
