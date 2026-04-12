package com.capstone.pethouse.infra.mqtt.controller;

import com.capstone.pethouse.infra.mqtt.MqttCommandService;
import com.capstone.pethouse.infra.mqtt.MqttPublisher;
import com.capstone.pethouse.infra.mqtt.MqttTopicManager;
import com.capstone.pethouse.infra.mqtt.ack.AckResult;
import com.capstone.pethouse.infra.mqtt.ack.MqttAckManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RequestMapping("/mqtt/test")
@RestController
public class MqttTestController {

    private final MqttPublisher publisher;
    private final MqttCommandService commandService;
    private final MqttAckManager ackManager;

    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publish(
            @RequestParam String topic,
            @RequestBody String payload
    ) {
        publisher.publish(topic, payload);
        return ResponseEntity.ok(Map.of(
                "status", "sent",
                "topic", topic
        ));
    }

    @PostMapping("/command/{houseId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendCommand(
            @PathVariable Long houseId,
            @RequestParam String action,
            @RequestBody(required = false) Map<String, Object> params
    ) {
        if (params == null) params = Map.of();

        return commandService.sendCommand(houseId, action, params)
                .thenApply(ackResult -> ResponseEntity.ok(Map.of(
                        "houseId", houseId,
                        "action", action,
                        "ackSuccess", ackResult.isSuccess(),
                        "ackMessage", ackResult.getMessage()
                )));
    }

    @GetMapping("/ack/pending")
    public ResponseEntity<Map<String, Integer>> pendingAcks() {
        return ResponseEntity.ok(Map.of("pendingAckCount", ackManager.pendingCount()));
    }

    @GetMapping("/topics/{houseId}")
    public ResponseEntity<Map<String, String>> getTopics(@PathVariable Long houseId) {
        return ResponseEntity.ok(Map.of(
                "sensorData", MqttTopicManager.sensorData(houseId),
                "sensorAlert", MqttTopicManager.sensorAlert(houseId),
                "deviceStatus", MqttTopicManager.deviceStatus(houseId),
                "deviceCommand", MqttTopicManager.deviceCommand(houseId),
                "deviceAck", MqttTopicManager.deviceAck(houseId),
                "camStream", MqttTopicManager.camStream(houseId),
                "camPtz", MqttTopicManager.camPtz(houseId)
        ));
    }
}
