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
public class SensorPushConsumer {

    private final HouseDataService houseDataService;

    /**
     * Kafka 'pet-house-sensor-data' 토픽 구독 (WebSocket 실시간 푸시 전담).
     * 신규 그룹 ID(pet-house-push)를 부여하고 실시간 푸시는 동시성 1로 충분합니다.
     * 푸시 실패 시에는 저장 흐름에 영향을 주지 않고 로그만 남기며 지나갑니다.
     */
    @KafkaListener(
            topics = KafkaTopicConfig.SENSOR_TOPIC,
            groupId = "pet-house-push",
            concurrency = "1",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(HouseDataRequest request) {
        log.debug("SensorPushConsumer 수신: deviceId='{}'", request.deviceId());
        try {
            houseDataService.push(request);
            log.debug("SensorPushConsumer 푸시 완료: deviceId='{}'", request.deviceId());
        } catch (Exception e) {
            log.warn("SensorPushConsumer 푸시 실패 (건너뜀): deviceId='{}', error='{}'",
                    request.deviceId(), e.getMessage());
        }
    }
}

