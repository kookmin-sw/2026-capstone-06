package com.capstone.pethouse.infra.mqtt.handler;

import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.domain.sensor.service.HouseDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SensorDataHandler implements MqttMessageHandler {

    private final ObjectMapper objectMapper;
    private final HouseDataService houseDataService;

    @Override
    public boolean supports(String category) {
        return "sensor/data".equals(category);
    }

    /**
     * MQTT payload 예시:
     * {"deviceId":"DEV001","temVal":25.3,"humVal":60.2,"coVal":412.5}
     */
    @Override
    public void handle(Long houseId, String category, String payload) {
        log.debug("Sensor data received — houseId={}, payload={}", houseId, payload);

        try {
            JsonNode node = objectMapper.readTree(payload);
            String deviceId = node.path("deviceId").asText(null);
            if (deviceId == null) {
                deviceId = node.path("device_id").asText(null);
            }
            if (deviceId == null) {
                log.warn("Sensor data missing deviceId — houseId={}, payload={}", houseId, payload);
                return;
            }

            Double temVal = readDouble(node, "temVal", "tem_val");
            Double humVal = readDouble(node, "humVal", "hum_val");
            Double coVal = readDouble(node, "coVal", "co_val");

            houseDataService.create(new HouseDataRequest(deviceId, temVal, humVal, coVal));
        } catch (Exception e) {
            log.error("Failed to process sensor data — houseId={}, payload={}, error={}",
                    houseId, payload, e.getMessage(), e);
        }
    }

    private Double readDouble(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode v = node.get(key);
            if (v != null && !v.isNull() && v.isNumber()) {
                return v.asDouble();
            }
        }
        return null;
    }
}
