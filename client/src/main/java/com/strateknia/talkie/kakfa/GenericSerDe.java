package com.strateknia.talkie.kakfa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class GenericSerDe<T> implements Serializer<T>, Deserializer<T> {

    private final ObjectMapper mapper;

    private final Class<T> type;

    public GenericSerDe(Class<T> type) {
        this.type = type;
        mapper = new ObjectMapper();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, type);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
