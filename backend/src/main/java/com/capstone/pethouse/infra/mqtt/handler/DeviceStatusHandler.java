package com.capstone.pethouse.infra.mqtt.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeviceStatusHandler implements MqttMessageHandler {

    @Override
    public boolean supports(String category) {
        return "device/status".equals(category);
    }

    @Override
    public void handle(Long houseId, String category, String payload) {
        log.info("Device status update — houseId={}, status={}", houseId, payload);
        // TASK-3b에서 PetHouse 상태(ONLINE/OFFLINE) 업데이트 구현 예정
    }
}
