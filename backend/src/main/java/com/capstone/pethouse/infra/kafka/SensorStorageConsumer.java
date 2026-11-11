package com.capstone.pethouse.infra.kafka;

import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.domain.sensor.service.HouseDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorStorageConsumer {

    private final HouseDataService houseDataService;

    /**
     * Kafka 'pet-house-sensor-data' 토픽 구독 (InfluxDB 시계열 저장 전담).
     * 기존 그룹 ID(pet-house-group) 및 파티션 수에 맞춘 동시성(concurrency 3)을 유지합니다.
     * 실패 시 예외를 던져 재시도/DLT 정책이 동작할 수 있도록 합니다.
     */
    @KafkaListener(
            topics = KafkaTopicConfig.SENSOR_TOPIC,
            groupId = "pet-house-group",
            concurrency = "3",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(HouseDataRequest request) {
        log.debug("SensorStorageConsumer 수신: deviceId='{}'", request.deviceId());
        houseDataService.save(request);
        log.debug("SensorStorageConsumer 저장 완료: deviceId='{}'", request.deviceId());
    }
}

