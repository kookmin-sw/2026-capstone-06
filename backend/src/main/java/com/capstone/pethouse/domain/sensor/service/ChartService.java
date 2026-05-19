package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.sensor.dto.SensorResponse;
import com.capstone.pethouse.domain.sensor.repository.ChartSensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChartService {

    private final DeviceRepository deviceRepository;
    private final ChartSensorRepository chartSensorRepository;

    @Transactional(readOnly = true)
    public List<SensorResponse> getChartData(String serialNum, String range, String interval) {
        if (serialNum == null || serialNum.isBlank()) {
            return Collections.emptyList();
        }

        Device device = deviceRepository.findBySerialNum(serialNum).orElse(null);
        if (device == null || !"HOUSE".equalsIgnoreCase(device.getDeviceType())) {
            return Collections.emptyList();
        }

        String deviceId = device.getDeviceId();
        return chartSensorRepository.getChartData(deviceId, range, interval);
    }
}
