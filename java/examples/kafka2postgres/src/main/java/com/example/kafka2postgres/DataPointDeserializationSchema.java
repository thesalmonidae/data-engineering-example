package com.example.kafka2postgres;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;

/**
 * Simple deserilization schema for the JSON data that
 * is ingested from the Kafka topic.
 */
public class DataPointDeserializationSchema extends AbstractDeserializationSchema<DataPoint> {
    private static final Gson gson = new GsonBuilder()
    .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        }
    })
    .create();

    @Override
    public DataPoint deserialize(byte[] message) throws IOException {
        return gson.fromJson(new String(message), DataPoint.class);
    }
}