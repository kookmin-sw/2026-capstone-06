package com.capstone.pethouse.domain.sensor.websocket;

import com.capstone.pethouse.domain.sensor.dto.DataVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SensorPushService {

    private static final String HOUSE_TOPIC_PREFIX = "/topic/sensor/house/";
    private static final String NECK_TOPIC_PREFIX = "/topic/sensor/neck/";

    private final SimpMessagingTemplate messagingTemplate;

    public void pushHouse(DataVo data) {
        try {
            messagingTemplate.convertAndSend(HOUSE_TOPIC_PREFIX + data.deviceId(), data);
        } catch (Exception e) {
            log.warn("WebSocket push failed (house) for {}: {}", data.deviceId(), e.getMessage());
        }
    }

    public void pushNeck(DataVo data) {
        try {
            messagingTemplate.convertAndSend(NECK_TOPIC_PREFIX + data.deviceId(), data);
        } catch (Exception e) {
            log.warn("WebSocket push failed (neck) for {}: {}", data.deviceId(), e.getMessage());
        }
    }
}
