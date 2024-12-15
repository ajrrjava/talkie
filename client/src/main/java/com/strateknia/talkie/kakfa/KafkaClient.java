package com.strateknia.talkie.kakfa;

import com.strateknia.talkie.Client;
import lombok.Builder;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.Uuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class KafkaClient<T> implements Client<T> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaClient.class);

    private final Consumer<String, T> consumer;
    private final Producer<String, T> producer;
    private final ExecutorService service = Executors.newFixedThreadPool(5);

    private final AtomicBoolean isSubscribed = new AtomicBoolean(false);

    @Builder
    public KafkaClient(Properties properties) {
        this.consumer = new KafkaConsumer<>(properties);
        this.producer = new KafkaProducer<>(properties);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> service.submit(this::close)));
    }

    @Override
    public void subscribe(String topic, BiConsumer<Instant, T> callback) {
        if(isSubscribed.get()) {
            return;
        }

        isSubscribed.set(true);

        logger.info("Subscribing to topic: {}", topic);

        Uuid clientId = consumer.clientInstanceId(Duration.ZERO);
        consumer.subscribe(Collections.singleton(topic));

        service.submit(() -> {
            Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
            int messageProcessed = 0;
            while(isSubscribed.get()) {
                ConsumerRecords<String, T> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, T> r : records) {
                   logger.info("Topic: {}   Value: {}", r.topic(), r.value());
                   callback.accept(Instant.ofEpochMilli(r.timestamp()), r.value());
                    messageProcessed++;
//                    currentOffsets.put(
//                            new TopicPartition(r.topic(), r.partition()),
//                            new OffsetAndMetadata(r.offset() + 1));
//                    if (messageProcessed % 10 == 0) {
//                        consumer.commitSync(currentOffsets);
//                    }
                }
            }
        });
    }

    @Override
    public void unsubscribe(String topic) {
        service.submit(() -> {
            consumer.unsubscribe();
            isSubscribed.set(false);
        });
    }

    @Override
    public void publish(String topic, T message) {
        service.submit(() -> {
            ProducerRecord<String, T> record = new ProducerRecord<>(topic, message);
            producer.send(record);

            logger.info("Published message {}", message);
        });
    }

    @Override
    public void close() {
        isSubscribed.set(false);

        this.consumer.unsubscribe();
        this.consumer.close();
        this.producer.close();
        this.service.shutdown();
    }
}
