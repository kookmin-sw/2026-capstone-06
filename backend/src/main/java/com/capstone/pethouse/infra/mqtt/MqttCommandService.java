package com.capstone.pethouse.infra.mqtt;

import com.capstone.pethouse.infra.mqtt.ack.AckResult;
import com.capstone.pethouse.infra.mqtt.ack.MqttAckManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class MqttCommandService {

    private final MqttPublisher publisher;
    private final MqttAckManager ackManager;

    public CompletableFuture<AckResult> sendCommand(Long houseId, String action, Map<String, Object> params) {
        String commandId = ackManager.generateCommandId();

        Map<String, Object> command = Map.of(
                "commandId", commandId,
                "action", action,
                "params", params
        );

        CompletableFuture<AckResult> future = ackManager.register(commandId);
        publisher.sendDeviceCommand(houseId, command);

        log.info("Command sent — houseId={}, action={}, commandId={}", houseId, action, commandId);
        return future;
    }

    public CompletableFuture<AckResult> sendCommand(Long houseId, String action, Map<String, Object> params, long timeoutSeconds) {
        String commandId = ackManager.generateCommandId();

        Map<String, Object> command = Map.of(
                "commandId", commandId,
                "action", action,
                "params", params
        );

        CompletableFuture<AckResult> future = ackManager.register(commandId, timeoutSeconds);
        publisher.sendDeviceCommand(houseId, command);

        log.info("Command sent — houseId={}, action={}, commandId={}, timeout={}s",
                houseId, action, commandId, timeoutSeconds);
        return future;
    }
}
