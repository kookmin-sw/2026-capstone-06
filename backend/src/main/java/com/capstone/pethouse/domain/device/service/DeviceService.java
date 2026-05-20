package com.capstone.pethouse.domain.device.service;

import com.capstone.pethouse.domain.User.repository.UserRepository;
import com.capstone.pethouse.domain.User.entity.User;
import com.capstone.pethouse.domain.code.entity.Code;
import com.capstone.pethouse.domain.code.repository.CodeRepository;
import com.capstone.pethouse.domain.device.dto.DevicePopupResponse;
import com.capstone.pethouse.domain.device.dto.DeviceRequest;
import com.capstone.pethouse.domain.device.dto.DeviceResponse;
import com.capstone.pethouse.domain.device.entity.Device;
import com.capstone.pethouse.domain.device.entity.PetHouse;
import com.capstone.pethouse.domain.device.repository.DeviceRepository;
import com.capstone.pethouse.domain.device.repository.PetHouseRepository;
import com.capstone.pethouse.domain.serial.entity.Serial;
import com.capstone.pethouse.domain.serial.repository.SerialRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SerialRepository serialRepository;
    private final UserRepository userRepository;
    private final PetHouseRepository petHouseRepository;
    private final CodeRepository codeRepository;

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

        // 펫 정보가 있으면 PetHouse 생성 후 연결
        if (hasPetInfo(request)) {
            Code objectCode = resolveObjectCode(request.objectCode());
            LocalDate objectBirth = parseBirth(request.objectBirth());
            String houseNickname = (request.nickname() != null && !request.nickname().isBlank()) 
                    ? request.nickname() 
                    : request.deviceId();
            PetHouse petHouse = PetHouse.createDefault(user, houseNickname, objectCode,
                    request.objectName(), objectBirth);
            PetHouse savedPetHouse = petHouseRepository.save(petHouse);
            device.assignToPetHouse(savedPetHouse);
        }

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

        // 2. 회원 존재 여부 체크
        User user = device.getUser();
        if (request.memberId() != null
                && (user == null || !request.memberId().equals(user.getMemberId()))) {
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

        // 5. 펫 정보 업데이트
        if (hasPetInfo(request)) {
            Code objectCode = resolveObjectCode(request.objectCode());
            LocalDate objectBirth = parseBirth(request.objectBirth());
            if (device.getPetHouse() != null) {
                device.getPetHouse().updatePetInfo(objectCode, request.objectName(), objectBirth);
            } else {
                String houseNickname = (request.nickname() != null && !request.nickname().isBlank()) 
                        ? request.nickname() 
                        : device.getDeviceId();
                PetHouse petHouse = PetHouse.createDefault(user, houseNickname, objectCode,
                        request.objectName(), objectBirth);
                PetHouse savedPetHouse = petHouseRepository.save(petHouse);
                device.assignToPetHouse(savedPetHouse);
            }
        }

        return DeviceResponse.from(device);
    }

    @Transactional
    public void deleteDevice(Long seq) {
        Device device = deviceRepository.findById(seq)
                .orElseThrow(() -> new EntityNotFoundException("장치를 찾을 수 없습니다."));
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

    // ── 헬퍼 메서드 ──────────────────────────────────────────────────

    private boolean hasPetInfo(DeviceRequest request) {
        return (request.objectName() != null && !request.objectName().isBlank())
                || (request.objectBirth() != null && !request.objectBirth().isBlank())
                || (request.objectCode() != null && !request.objectCode().isBlank());
    }

    private Code resolveObjectCode(String objectCode) {
        if (objectCode == null || objectCode.isBlank()) return null;
        return codeRepository.findByCode(objectCode).orElse(null);
    }

    private LocalDate parseBirth(String objectBirth) {
        if (objectBirth == null || objectBirth.isBlank()) return null;
        try {
            return LocalDate.parse(objectBirth);
        } catch (Exception e) {
            return null;
        }
    }
}
