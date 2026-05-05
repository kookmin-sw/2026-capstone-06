package com.capstone.pethouse.domain.sensor.service;

import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.sensor.dto.DataVo;
import com.capstone.pethouse.domain.sensor.repository.HouseDataRepository;
import com.capstone.pethouse.domain.sensor.repository.NeckDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChartService {

    private static final int CHART_LIMIT = 1000;

    private final DeviceRepository deviceRepository;
    private final HouseDataRepository houseDataRepository;
    private final NeckDataRepository neckDataRepository;

    @Transactional(readOnly = true)
    public List<DataVo> getChartData(String serialNum) {
        if (serialNum == null || serialNum.isBlank()) {
            return Collections.emptyList();
        }

        Device device = deviceRepository.findBySerialNum(serialNum).orElse(null);
        if (device == null) {
            return Collections.emptyList();
        }

        String deviceId = device.getDeviceId();
        String type = device.getDeviceType();

        PageRequest pageRequest = PageRequest.of(0, CHART_LIMIT);

        if ("HOUSE".equalsIgnoreCase(type)) {
            return houseDataRepository.findAllWithSearch(deviceId, pageRequest)
                    .map(DataVo::fromHouse)
                    .getContent();
        } else if ("COLLAR".equalsIgnoreCase(type) || "NECK".equalsIgnoreCase(type)) {
            return neckDataRepository.findAllWithSearch(deviceId, pageRequest)
                    .map(DataVo::fromNeck)
                    .getContent();
        }

        return Collections.emptyList();
    }
}
