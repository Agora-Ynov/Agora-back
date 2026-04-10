package com.agora.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlexibleLocalTimeDeserializerTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalTime.class, new FlexibleLocalTimeDeserializer());
        mapper.registerModule(module);
    }

    @Test
    void deserialize_stringIso8601() throws Exception {
        LocalTime t = mapper.readValue("\"14:30:00\"", LocalTime.class);
        assertThat(t).isEqualTo(LocalTime.of(14, 30, 0));
    }

    @Test
    void deserialize_null() throws Exception {
        assertThat(mapper.readValue("null", LocalTime.class)).isNull();
    }

    @Test
    void deserialize_objectOpenApiShape() throws Exception {
        String json = "{\"hour\":9,\"minute\":15,\"second\":30,\"nano\":0}";
        LocalTime t = mapper.readValue(json, LocalTime.class);
        assertThat(t).isEqualTo(LocalTime.of(9, 15, 30, 0));
    }

    @Test
    void deserialize_invalidIsoString_shouldFail() {
        assertThatThrownBy(() -> mapper.readValue("\"pas-une-heure\"", LocalTime.class))
                .isInstanceOf(Exception.class);
    }

    @Test
    void deserialize_numberToken_shouldReject() {
        assertThatThrownBy(() -> mapper.readValue("42", LocalTime.class))
                .isInstanceOf(MismatchedInputException.class);
    }
}
