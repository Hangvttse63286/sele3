package com.sele3.configs;

import java.lang.reflect.Type;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson type adapter for {@link MutableCapabilities}. Gson's default reflective (de)serialization
 * can't handle it: the class stores capabilities in an internal map under a field name that
 * doesn't match any capability key, so deserializing a JSON object like
 * {@code {"acceptInsecureCerts": true}} directly would silently produce an empty capabilities
 * instance instead of an error. This adapter reads/writes it as a plain JSON object instead,
 * going through the same {@code Map}-based constructor {@link Configuration} already uses for
 * capabilities supplied via a system property.
 */
public class CapabilitiesAdapter implements JsonSerializer<MutableCapabilities>, JsonDeserializer<MutableCapabilities> {

    @Override
    public JsonElement serialize(MutableCapabilities src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.asMap());
    }

    @Override
    public MutableCapabilities deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> rawCapabilities = context.deserialize(json, Map.class);
        return new MutableCapabilities(rawCapabilities);
    }
}
