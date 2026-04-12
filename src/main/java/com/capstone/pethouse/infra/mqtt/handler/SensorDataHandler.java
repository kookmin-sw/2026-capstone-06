package com.capstone.pethouse.infra.mqtt.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SensorDataHandler implements MqttMessageHandler {

    @Override
    public boolean supports(String category) {
        return "sensor/data".equals(category);
    }

    @Override
    public void handle(Long houseId, String category, String payload) {
        log.info("Sensor data received — houseId={}, payload={}", houseId, payload);
        // TASK-4에서 InfluxDB 저장 + WebSocket 푸시 구현 예정
    }
}
