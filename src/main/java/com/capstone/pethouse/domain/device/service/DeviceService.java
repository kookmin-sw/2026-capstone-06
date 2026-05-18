package com.capstone.pethouse.domain.device.service;

import com.capstone.pethouse.domain.User.repository.UserRepository;
import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.device.dto.DevicePopupResponse;
import com.capstone.pethouse.domain.device.dto.DeviceRequest;
import com.capstone.pethouse.domain.device.dto.DeviceResponse;
import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.serial.entity.Serial;
import com.capstone.pethouse.domain.serial.repository.SerialRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SerialRepository serialRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getDevices(String searchType, String searchQuery, Pageable pageable) {
        String cleanedQuery = (searchQuery != null && !searchQuery.isBlank()) ? searchQuery : null;

        return deviceRepository.findAllWithSearch(searchType, cleanedQuery, pageable).map(DeviceResponse::from);
    }

    @Transactional(readOnly = true)
    public DeviceResponse getDevice(Long seq) {
        Device device = deviceRepository.findById(seq)
                .orElseThrow(() -> new EntityNotFoundException("장치를 찾을 수 없습니다."));

        return DeviceResponse.from(device);
    }

    @Transactional
    public DeviceResponse createDevice(DeviceRequest request) {
        Serial serial = serialRepository.findBySerialNum(request.serialNum())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시리얼 번호입니다."));

        if (serial.isUse()) {
            throw new IllegalStateException("이미 사용 중인 시리얼 번호입니다.");
        }

        if (deviceRepository.existsByDeviceId(request.deviceId())) {
            throw new IllegalStateException("이미 등록된 장치 ID입니다.");
        }

        User user = userRepository.findByMemberId(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Device device = Device.of(request.deviceId(), user, request.serialNum(), request.deviceType());

        Device savedDevice = deviceRepository.save(device);
        serial.markUsed();
        return DeviceResponse.from(savedDevice);
    }

    @Transactional
    public DeviceResponse updateDevice(DeviceRequest request) {
        Device device = deviceRepository.findById(request.seq())
                .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다."));

        // 1. 장치 ID 중복 체크 (변경될 경우에만)
        if (request.deviceId() != null && !request.deviceId().equals(device.getDeviceId())) {
            if (deviceRepository.existsByDeviceId(request.deviceId())) {
                throw new IllegalStateException("이미 사용 중인 장치 ID입니다.");
            }
        }

        // 2. 회원 존재 여부 체크 (새로운 회원에게 기기를 양도하거나, 기존 회원이 탈퇴한 경우)
        User user = device.getUser(); // 이미 존재하는 device에서 user 찾음
        if (request.memberId() != null
                && (user == null || !request.memberId().equals(user.getMemberId()))) { // request의 user와 기존 device의
                                                                                       // user가 다르면
            user = userRepository.findByMemberId(request.memberId())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        }

        // 3. 시리얼 번호 변경 처리
        if (request.serialNum() != null && !request.serialNum().equals(device.getSerialNum())) {
            String oldSerialNum = request.oldSerialNum() != null ? request.oldSerialNum() : device.getSerialNum();
            serialRepository.findBySerialNum(oldSerialNum).ifPresent(Serial::markUnused);
            Serial newSerial = serialRepository.findBySerialNum(request.serialNum())
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시리얼 번호입니다."));
            if (newSerial.isUse()) {
                throw new IllegalStateException("이미 사용 중인 시리얼 번호입니다.");
            }
            newSerial.markUsed();
        }

        // 4. 기본 정보 업데이트
        device.update(request.deviceId(), user, request.serialNum(), request.deviceType());

        return DeviceResponse.from(device);
    }

    @Transactional
    public void deleteDevice(Long seq) {
        Device device = deviceRepository.findById(seq)
                .orElseThrow(() -> new EntityNotFoundException("장치를 찾을 수 없습니다."));

        // 연결된 시리얼 미사용 처리
        serialRepository.findBySerialNum(device.getSerialNum()).ifPresent(Serial::markUnused);

        deviceRepository.delete(device);
    }

    @Transactional(readOnly = true)
    public List<DevicePopupResponse> getPopupList() {
        return deviceRepository.findAllPopupList().stream()
                .map(DevicePopupResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DevicePopupResponse> getPopupListByType(String deviceType) {
        return deviceRepository.findByDeviceType(deviceType).stream()
                .map(DevicePopupResponse::from)
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
