package com.strateknia.talkie;

import java.time.Instant;
import java.util.function.BiConsumer;

public interface Client<T> {

    void subscribe(String topic, BiConsumer<Instant,T> callback);

    void unsubscribe(String topic);

    void publish(String topic, T message);

    void close();
}
