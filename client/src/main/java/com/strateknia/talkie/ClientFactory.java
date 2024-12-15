package com.strateknia.talkie;

import com.strateknia.talkie.kakfa.KafkaClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class ClientFactory {

    public static <T> Client<T> getKakfaClient(String filename) {
        Properties properties = readProperties(filename);

        if(!properties.containsKey(ConsumerConfig.GROUP_ID_CONFIG)) {
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        }

        return KafkaClient.<T>builder()
                .properties(properties)
                .build();
    }

    public static Properties readProperties(String filename) {
        try{
            Properties properties = new Properties();
            properties.load(new FileInputStream(filename));
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
