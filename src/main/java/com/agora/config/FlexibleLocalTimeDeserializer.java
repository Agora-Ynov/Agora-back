package com.agora.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Accepte les payloads {@link LocalTime} en chaine ISO-8601 ou en objet OpenAPI
 * (champs {@code hour}, {@code minute}, {@code second}, {@code nano}).
 */
public class FlexibleLocalTimeDeserializer extends JsonDeserializer<LocalTime> {

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        if (t == JsonToken.VALUE_STRING) {
            try {
                return LocalTime.parse(p.getText());
            } catch (DateTimeParseException e) {
                return (LocalTime) ctxt.handleWeirdStringValue(LocalTime.class, p.getText(), e.getMessage());
            }
        }
        if (t == JsonToken.START_OBJECT) {
            JsonNode node = ctxt.readTree(p);
            int hour = node.path("hour").asInt(0);
            int minute = node.path("minute").asInt(0);
            int second = node.path("second").asInt(0);
            int nano = node.path("nano").asInt(0);
            return LocalTime.of(hour, minute, second, nano);
        }
        return (LocalTime) ctxt.handleUnexpectedToken(LocalTime.class, p);
    }
}
