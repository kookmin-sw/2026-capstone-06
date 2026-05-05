package com.capstone.pethouse.infra.mqtt.handler;

import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.domain.sensor.dto.NeckDataRequest;
import com.capstone.pethouse.domain.sensor.service.HouseDataService;
import com.capstone.pethouse.domain.sensor.service.NeckDataService;
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
    private final NeckDataService neckDataService;
    private final DeviceRepository deviceRepository;

    @Override
    public boolean supports(String category) {
        return "sensor/data".equals(category);
    }

    /**
     * MQTT payload 예시:
     * {"deviceId":"DEV001","temVal":25.3,"humVal":60.2,"coVal":412.5}
     * {"deviceId":"DEV002","temVal":18.0,"heartVal":64.0,"coVal":404.0}
     *
     * deviceType 별로 분기:
     * - HOUSE → HouseDataService.create()
     * - COLLAR/NECK → NeckDataService.create()
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
            Double coVal = readDouble(node, "coVal", "co_val");

            Device device = deviceRepository.findByDeviceId(deviceId).orElse(null);
            String type = device != null ? device.getDeviceType() : null;

            // heartVal이 있으면 NECK, 없으면 HOUSE로 추론 (type이 null인 경우)
            Double heartVal = readDouble(node, "heartVal", "heart_val");
            Double humVal = readDouble(node, "humVal", "hum_val");

            boolean isNeck = "COLLAR".equalsIgnoreCase(type) || "NECK".equalsIgnoreCase(type)
                    || (type == null && heartVal != null);

            if (isNeck) {
                neckDataService.create(new NeckDataRequest(deviceId, temVal, heartVal, coVal));
            } else {
                houseDataService.create(new HouseDataRequest(deviceId, temVal, humVal, coVal));
            }
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
