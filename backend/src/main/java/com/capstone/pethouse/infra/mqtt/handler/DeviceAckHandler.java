package com.capstone.pethouse.infra.mqtt.handler;

import com.capstone.pethouse.infra.mqtt.ack.MqttAckManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeviceAckHandler implements MqttMessageHandler {

    private final MqttAckManager ackManager;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String category) {
        return "device/ack".equals(category);
    }

    @Override
    public void handle(Long houseId, String category, String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String commandId = node.path("commandId").asText(null);
            boolean success = node.path("success").asBoolean(false);
            String message = node.path("message").asText("");

            if (commandId == null) {
                log.warn("ACK received without commandId — houseId={}, payload={}", houseId, payload);
                return;
            }

            log.info("ACK received — houseId={}, commandId={}, success={}, message={}",
                    houseId, commandId, success, message);

            ackManager.complete(commandId, success, message);
        } catch (Exception e) {
            log.error("Failed to parse ACK payload — houseId={}, payload={}", houseId, payload, e);
        }
    }
}
