package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.sensor.dto.SensorResponse;
import com.capstone.pethouse.domain.sensor.dto.HouseDataRequest;
import com.capstone.pethouse.domain.sensor.influx.InfluxWriter;
import com.capstone.pethouse.domain.sensor.websocket.SensorPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class HouseDataService {

    private final InfluxWriter influxWriter;
    private final SensorPushService sensorPushService;

    /**
     * HTTP/MQTT 양쪽에서 호출. InfluxDB write + WebSocket push.
     */
    @Transactional
    public SensorResponse create(HouseDataRequest request) {
        if (request.deviceId() == null || request.deviceId().isBlank()) {
            throw new IllegalArgumentException("device_id는 필수입니다.");
        }

        SensorResponse sensorResponse = new SensorResponse(
                null,
                request.deviceId(),
                request.temVal(),
                request.humVal(),
                request.coVal(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );

        // InfluxDB 시계열 저장
        influxWriter.writeHouse(request.deviceId(), request.temVal(), request.humVal(), request.coVal());

        // WebSocket 실시간 푸시
        sensorPushService.pushHouse(sensorResponse);

        return sensorResponse;
    }
}
