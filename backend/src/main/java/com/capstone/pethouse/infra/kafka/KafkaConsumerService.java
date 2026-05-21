package com.capstone.pethouse.infra.kafka;

import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.domain.sensor.service.HouseDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final HouseDataService houseDataService;

    /**
     * Kafka 'pet-house-sensor-data' 토픽 구독.
     * SensorDataHandler가 발행한 센서 데이터를 비동기로 수신하여
     * InfluxDB 저장 및 WebSocket 실시간 푸시를 처리합니다.
     */
    @KafkaListener(
            topics = KafkaTopicConfig.SENSOR_TOPIC,
            groupId = "pet-house-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSensorData(HouseDataRequest request) {
        log.info("Kafka Consumer 수신: topic='{}', deviceId='{}'",
                KafkaTopicConfig.SENSOR_TOPIC, request.deviceId());
        try {
            // InfluxDB 저장 + WebSocket 실시간 푸시
            houseDataService.create(request);
            log.info("센서 데이터 처리 완료: deviceId='{}'", request.deviceId());
        } catch (Exception e) {
            log.error("센서 데이터 처리 실패: deviceId='{}', error='{}'",
                    request.deviceId(), e.getMessage(), e);
        }
    }
}
