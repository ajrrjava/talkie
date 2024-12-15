package com.strateknia.talkie;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Jacksonized
@ToString
@EqualsAndHashCode
@Getter
@Builder
public class TalkieMessage {
    private final Long timestamp;
    private final String topic;
    private final String fromUser;
    private final String toUser;
    private final String text;
}
