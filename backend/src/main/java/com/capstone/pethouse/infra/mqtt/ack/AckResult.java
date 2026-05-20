package com.capstone.pethouse.infra.mqtt.ack;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AckResult {

    private final boolean success;
    private final String message;

    public static AckResult success(String message) {
        return new AckResult(true, message);
    }

    public static AckResult failure(String message) {
        return new AckResult(false, message);
    }

    public static AckResult timeout() {
        return new AckResult(false, "ACK timeout");
    }
}
