package com.capstone.pethouse.infra.mqtt.handler;

public interface MqttMessageHandler {

    boolean supports(String category);

    void handle(Long houseId, String category, String payload);
}
