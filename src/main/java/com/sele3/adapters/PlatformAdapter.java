package com.sele3.adapters;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sele3.drivers.IPlatform;

/**
 * Gson type adapter for {@link IPlatform}. Gson can't instantiate an interface on its own —
 * deserializing a {@code "platform": "chrome"} field without this adapter throws
 * {@code JsonIOException} immediately. This adapter reads/writes it as a plain name string,
 * resolved via {@link IPlatform#fromString(String)}.
 */
public class PlatformAdapter implements JsonSerializer<IPlatform>, JsonDeserializer<IPlatform> {

    @Override
    public JsonElement serialize(IPlatform src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.name());
    }

    @Override
    public IPlatform deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return IPlatform.fromString(json.getAsString());
    }
}
