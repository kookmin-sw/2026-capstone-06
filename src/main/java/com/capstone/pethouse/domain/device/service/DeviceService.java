package com.capstone.pethouse.domain.device.service;

import com.capstone.pethouse.domain.User.repository.UserRepository;
import com.capstone.pethouse.domain.device.dto.DeviceRequest;
import com.capstone.pethouse.domain.device.dto.DeviceVo;
import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.serial.entity.Serial;
import com.capstone.pethouse.domain.serial.repository.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SerialRepository serialRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<DeviceVo> getDevices(String searchType, String searchQuery, Pageable pageable) {
        String cleanedQuery = (searchQuery != null && !searchQuery.isBlank()) ? searchQuery : null;

        return deviceRepository.findAllWithSearch(searchType, cleanedQuery, pageable).map(DeviceVo::from);
    }

    @Transactional(readOnly = true)
    public DeviceVo getDevice(Long seq) {
        Device device = deviceRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));
        return DeviceVo.from(device);
    }

    @Transactional
    public DeviceVo createDevice(DeviceRequest request) {
        Serial serial = serialRepository.findBySerialNum(request.serialNum())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시리얼 번호입니다."));
        if (serial.isUse()) {
            throw new IllegalStateException("이미 사용 중인 시리얼 번호입니다.");
        }

        LocalDate objectBirth = request.objectBirth() != null ? LocalDate.parse(request.objectBirth()) : null;
        Device device = Device.of(
                request.deviceId(), request.memberId(), request.serialNum(),
                request.objectCode(), objectBirth, request.deviceType()
        );

        Device saved = deviceRepository.save(device);
        serial.markUsed();
        return DeviceVo.from(saved);
    }

    @Transactional
    public DeviceVo updateDevice(DeviceRequest request) {
        Device device = deviceRepository.findById(request.seq())
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));

        // 시리얼 변경 처리
        if (request.serialNum() != null && !request.serialNum().equals(device.getSerialNum())) {
            // 기존 시리얼 미사용 처리
            String oldSerialNum = request.oldSerialNum() != null ? request.oldSerialNum() : device.getSerialNum();
            serialRepository.findBySerialNum(oldSerialNum).ifPresent(Serial::markUnused);

            // 새 시리얼 검증 및 사용 처리
            Serial newSerial = serialRepository.findBySerialNum(request.serialNum())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시리얼 번호입니다."));
            if (newSerial.isUse()) {
                throw new IllegalStateException("이미 사용 중인 시리얼 번호입니다.");
            }
            newSerial.markUsed();
        }

        LocalDate objectBirth = request.objectBirth() != null ? LocalDate.parse(request.objectBirth()) : null;
        device.update(request.deviceId(), request.memberId(), request.serialNum(),
                request.objectCode(), objectBirth, request.deviceType());

        return DeviceVo.from(device);
    }

    @Transactional
    public void deleteDevice(Long seq) {
        Device device = deviceRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));

        // 연결된 시리얼 미사용 처리
        serialRepository.findBySerialNum(device.getSerialNum()).ifPresent(Serial::markUnused);

        deviceRepository.delete(device);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPopupList() {
        return deviceRepository.findAllPopupList().stream()
                .map(d -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("member_id", d.getMemberId());
                    map.put("device_id", d.getDeviceId());
                    return map;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPopupListByType(String deviceType) {
        return deviceRepository.findByDeviceType(deviceType).stream()
                .map(d -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("member_id", d.getMemberId());
                    map.put("device_id", d.getDeviceId());
                    return map;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, String> checkMember(String memberId) {
        boolean exists = userRepository.existsByMemberId(memberId);
        return Map.of("status", exists ? "ok" : "not_exist");
    }

    @Transactional(readOnly = true)
    public Map<String, String> checkSerial(String serialNum) {
        return serialRepository.findBySerialNum(serialNum)
                .map(serial -> Map.of("status", serial.isUse() ? "in_use" : "ok"))
                .orElse(Map.of("status", "not_exist"));
    }
}
