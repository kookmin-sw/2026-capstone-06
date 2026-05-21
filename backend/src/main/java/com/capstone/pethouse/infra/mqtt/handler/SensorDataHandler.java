package com.capstone.pethouse.infra.mqtt.handler;

import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.infra.kafka.KafkaProducerService;
import com.capstone.pethouse.infra.kafka.KafkaTopicConfig;
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
    private final KafkaProducerService kafkaProducerService;

    @Override
    public boolean supports(String category) {
        return "sensor/data".equals(category);
    }

    /**
     * MQTT payload 예시:
     * {"deviceId":"DEV001","temVal":25.3,"humVal":60.2,"coVal":412.5}
     *
     * MQTT로 수신한 센서 데이터를 Kafka 토픽으로 발행합니다.
     * 실제 InfluxDB 저장 및 WebSocket 푸시는 KafkaConsumerService에서 처리됩니다.
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

            HouseDataRequest request = new HouseDataRequest(deviceId, temVal, humVal, coVal);

            // InfluxDB 직접 저장 대신 Kafka 토픽으로 발행 (비동기 처리)
            kafkaProducerService.sendMessage(KafkaTopicConfig.SENSOR_TOPIC, request);
            log.info("Sensor data published to Kafka — deviceId={}, houseId={}", deviceId, houseId);

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
