package com.capstone.pethouse.infra.mqtt;

import com.capstone.pethouse.infra.mqtt.config.MqttProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MqttPublisher {

    private final MessageChannel mqttOutboundChannel;
    private final MqttProperties mqttProperties;
    private final ObjectMapper objectMapper;

    public void publish(String topic, String payload) {
        publish(topic, payload, mqttProperties.getDefaultQos());
    }

    public void publish(String topic, String payload, int qos) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, qos)
                .setHeader(MqttHeaders.RETAINED, false)
                .build();

        mqttOutboundChannel.send(message);
        log.debug("MQTT Published → topic={}, payload={}", topic, payload);
    }

    public void publishJson(String topic, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            publish(topic, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize MQTT payload to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("MQTT payload serialization failed", e);
        }
    }

    public void publishJson(String topic, Object payload, int qos) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            publish(topic, json, qos);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize MQTT payload to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("MQTT payload serialization failed", e);
        }
    }

    // === 편의 메서드 ===

    public void sendDeviceCommand(Long houseId, Object command) {
        publishJson(MqttTopicManager.deviceCommand(houseId), command);
    }

    public void sendSensorAlert(Long houseId, Object alert) {
        publishJson(MqttTopicManager.sensorAlert(houseId), alert);
    }

    public void sendCamPtz(Long houseId, Object ptzCommand) {
        publishJson(MqttTopicManager.camPtz(houseId), ptzCommand);
    }

    public void sendCamStream(Long houseId, Object streamCommand) {
        publishJson(MqttTopicManager.camStream(houseId), streamCommand);
    }
}
