package com.strateknia.talkie.kakfa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strateknia.talkie.TalkieMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class TalkieMessageKafkaSerDe implements Serializer<TalkieMessage>, Deserializer<TalkieMessage> {
    private static final Logger logger = LoggerFactory.getLogger(TalkieMessageKafkaSerDe.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

   @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public TalkieMessage deserialize(String topic, byte[] data) {
//        try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
//            ObjectInputStream ois = new ObjectInputStream(bais)) {
//            logger.info("OBJECT: {}", ois.readObject());
//            return (TalkieMessage) ois.readObject();
//        } catch (Exception e) {
//            return null;
//        }

        try {
            return objectMapper.readValue(data, TalkieMessage.class);
        } catch (IOException e) {
            logger.error("CAUGHT:", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] serialize(String topic, TalkieMessage data) {
        if(null == data) {
            return null;
        }

//        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(baos)) {
//            oos.writeObject(data);
//
//            return baos.toByteArray();
//        } catch (Exception e) {
//            return null;
//        }

        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public void close() {

    }
}
