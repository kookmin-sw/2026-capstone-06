package com.capstone.pethouse.domain.iot.service;

import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.iot.dto.IotDataRequest;
import com.capstone.pethouse.domain.sensor.entity.Sensor;
import com.capstone.pethouse.domain.sensor.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 디바이스 명세 v0.3 - POST /api/data
 *
 * 흐름:
 *   1) SN(시리얼 번호)로 Device 매핑하여 deviceId 획득
 *   2) HouseDataService.create()에 위임 → RDB 저장 + InfluxDB write + WebSocket push
 *   3) 알림 처리(임계값 초과 시 등)는 추후 확장
 *
 * 사용자 시나리오: IoT 디바이스가 직접 호출.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class IotDataService {

    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;

    @Transactional
    public void registerEnvironmentData(IotDataRequest request) {
        if (request.sn() == null || request.sn().isBlank()) {
            throw new IllegalArgumentException("SN은 필수입니다.");
        }

        Device device = deviceRepository.findBySerialNum(request.sn())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 시리얼: " + request.sn()));

        // 임시로 Sensor entity에 직접 저장
        Sensor sensor = Sensor.of(
                device.getDeviceId(),
                request.t(),
                request.h(),
                request.co()
        );
        sensorRepository.save(sensor);

        // TODO: 알림 임계값 초과 시 FCM 발송 — B 도메인 (Notifications) 구현 시 연동
        log.debug("IoT data registered — SN={}, deviceId={}", request.sn(), device.getDeviceId());
    }
}
